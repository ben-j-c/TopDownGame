package game;

import geo.Dijkstra;
import geo.Map;
import geo.Triangle;
import geo.Triangle.BlockingVector;
import geo.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;



/**
 * 
 * @author Ben
 *
 */
final class KeyList
{
	public boolean UP = false;
	public boolean DOWN = false;
	public boolean LEFT = false;
	public boolean RIGHT = false;
	public boolean MOUSE_LEFT = false;
	public boolean MOUSE_RIGHT = false;
	public double  MOUSE_X = 0;
	public double  MOUSE_Y = 0;
}
/**
 * 
 * @author Ben
 * The game class includes all the rendering mouse handling
 */
public class Shoot extends JFrame implements GLEventListener, MouseListener, KeyListener, Runnable
{
	private static final long serialVersionUID = 1L;
	public static final double SNAP_DISTANCE = 0.025;
	public static final double PLAYER_SPEED = 0.005;
	public static final double PARTICLE_SPEED = 0.02;
	public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
	java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	private ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
	private FPSAnimator animator;
	private GLCanvas canvas;
	Thread runningGame;
	
	private Map gameMap = new Map();
	private ArrayList<Vector> points = new ArrayList<Vector>();
	private Dijkstra.Description descWithPlayer = new Dijkstra.Description();
	
	public boolean GAME_STARTED = false;
	public boolean PLACE_GRAPH = false;
	public long gametime = 0 ;
	public Entity player = new Entity(Entity.SOLID);
	public Vector offset = new Vector(0,0);
	
	private ArrayList<Entity> ents = new ArrayList<Entity>();
	private ArrayList<Entity> toRemove = new ArrayList<Entity>();
	
	private KeyList keys = new KeyList();
	
	boolean weapon = false;
	
	Shoot()
	{
		
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		canvas = new GLCanvas(caps);
		canvas.setSize(1000, 1000);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000,1000);
		add(canvas);
		setVisible(true);
		canvas.addGLEventListener(this);
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		this.addKeyListener(this);
		
		animator = new FPSAnimator(canvas, 50);
		
	}
	
	public void startGame()
	{
		animator.start();
	}
	
	public Vector insideGeometry(Vector t)
	{	
		for(Triangle tria : gameMap.geo)
		{
			Vector at = t.sub(tria.a);
			Vector bt = t.sub(tria.b);
			Vector ct = t.sub(tria.c);
			
			
			double a = tria.ab.cross(at);
			double b = tria.bc.cross(bt);
			double c = tria.ca.cross(ct);
			
			if(a > 0 && b > 0 && c > 0)
			{
				a /= tria.ab.mag();
				b /= tria.bc.mag();
				c /= tria.ca.mag();
				
				if(a <= b && a <= c)
				{
					return tria.ab.copy();
				}
				else if(b <= c)
				{
					return tria.bc.copy();
				}
				else
				{
					return tria.ca.copy();
				}
			}
			
			
		}
		
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////RENDER FUNCTIONS/////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void display(GLAutoDrawable drawable)

	{
		stepGame();
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glLoadIdentity();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		gl.glPushMatrix();
		
		gl.glTranslated(-(player.pos.x + offset.x), -(player.pos.y + offset.y), 0);
		
		drawMapGeometry(gl);
		drawEnts(gl);
		drawGraph(gl);
		drawPlayerInfo(gl);
		
		gl.glPopMatrix();
		
	}

	public void drawMapGeometry(GL2 gl)
	{
		gl.glColor3d(0.75,0.75,0.75);
		gl.glBegin(GL2.GL_TRIANGLES);
		
		
		for(Triangle t : gameMap.geo)
		{
			gl.glVertex2d(t.a.x, t.a.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.c.x, t.c.y);
		}
		
		gl.glEnd();
		
		gl.glBegin(gl.GL_LINES);
		
		for(Triangle t : gameMap.geo)
		{
			gl.glVertex2d(t.a.x, t.a.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.c.x, t.c.y);
			gl.glVertex2d(t.c.x, t.c.y);
			gl.glVertex2d(t.a.x, t.a.y);
		}
		
		gl.glEnd();
	}
	
	public void drawGraph(GL2 gl)
	{
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : gameMap.desc.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : gameMap.desc.getNodes())
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();
		
		gl.glLineWidth(1.0f);
		
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : descWithPlayer.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : descWithPlayer.getNodes())
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();
		
		gl.glLineWidth(2.0f);
		
		gl.glPopMatrix();
	}
	
	public void drawPlayerInfo(GL2 gl)
	{
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glColor4d(0.75, 0.25, 0.25, 0.5);
		
		gl.glVertex2d(-1, 1);
		gl.glVertex2d(-1, 0.95);
		gl.glVertex2d((player.life - 5.0)/5.0 , 0.95);
		gl.glVertex2d((player.life - 5.0)/5.0 , 1);
		
		
		gl.glEnd();
		
		{
			java.awt.Point pos = canvas.getMousePosition();
			if(pos != null)
			{
				
				
				gl.glBegin(GL2.GL_POINTS);
				gl.glColor3d(Math.random(), Math.random(), Math.random());
				Vector pos2 = translateToReal(pos.x, pos.y);
				gl.glVertex2d(pos.x, pos.y);
				gl.glEnd();
			}
		}
	}
	
	public void drawEnts(GL2 gl)
	{
		if(GAME_STARTED)
		{
			//gl.glColor3d(1, 0, 0);
			gl.glBegin(gl.GL_POINTS);
			for(Entity e : ents)
			{
				if((e.TYPE & Entity.PROJECTILE) != 0)
				{
					gl.glColor3d(Math.random()*0.5 +0.5, Math.random()*0.5, 0);
					gl.glVertex2d(e.pos.x, e.pos.y);
					
				}
				
				if((e.TYPE & Entity.BODY) != 0)
				{
					gl.glColor3d(1,1,1);
					gl.glVertex2d(e.pos.x, e.pos.y);
				}
				
			}
			
			gl.glColor3d(0, 1, 0);
			gl.glVertex2d(player.pos.x, player.pos.y);
			gl.glEnd();
			
			gl.glBegin(gl.GL_LINES);
			
			for(Entity e : ents)
			{
				if((e.TYPE & Entity.LASER) != 0)
				{
					
					gl.glColor3d(Math.random()*0.5 +0.5, Math.random()*0.5, 0);
					gl.glVertex2d(e.pos.x,e.pos.y);
					gl.glVertex2d(e.v.x,e.v.y);
				}
			}
			
			
			gl.glEnd();
		}
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		
	}
	
	@Override
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		gl.glPointSize(3.0f);
		gl.glLineWidth(2.0f);
		
		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_POLYGON_SMOOTH);
		
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int w, int h, int arg3,
			int arg4)
	{
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////RUN FUNCTION/////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void run()
	{	
		while(counter.get() < ents.size())
		{
			Entity e = null;
			synchronized(counter)
			{
				if(counter.get() < ents.size())
				{
					e = ents.get(counter.getAndIncrement());
				}
				else
				{
					break;
				}
			}
			
			e.headTo = getNextMoveTo(e);
			
			synchronized(progress)
			{
				progress.incrementAndGet();
			}
		}
		
		
		
		synchronized(progress)
		{
			if(progress.get() >= ents.size())
			{
				progress.notify();
			}
			
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////MAIN FUNCTION////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		Shoot shoot = new Shoot();
		shoot.startGame();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////KEYB LOGIC///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void mouseClicked(MouseEvent e)
	{
		
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		if(!GAME_STARTED)
		{
			if(PLACE_GRAPH)
			{
				Vector temp = translateToReal(e.getX(), e.getY());
				
				Vector temp2 = gameMap.desc.getSkewed(temp, SNAP_DISTANCE*SNAP_DISTANCE);
				if(temp2 == null)
				{
					points.add(temp);
					gameMap.desc.addNode(temp);
				}
				else
				{
					points.add(temp2);
					gameMap.desc.addNode(temp2);
				}
				
				if(points.size() == 2)
				{
					gameMap.desc.addEdge(points.remove(0), points.remove(0));
					points.clear();
				}
			}
			else
			{
				Vector temp = translateToReal(e.getX(), e.getY());
				
				for(Triangle t: gameMap.geo)
				{
					Vector temp1 = t.skew(temp, SNAP_DISTANCE);
					if(temp1 != null)
					{
						temp = temp1;
						break;
					}
				}
				
				points.add(temp);
				
				if(points.size()%3 == 0)
				{
					int size = points.size();
					Triangle t = new Triangle(points.get( size - 3 ), points.get( size - 2 ), points.get( size - 1 ) );
					if(!t.isFlat())
					{
						gameMap.geo.add(t);
					}
				}
			}
		}
		else
		{
			switch(e.getButton())
			{
				case MouseEvent.BUTTON1:
				{
					fireWeapon(e.getX(), e.getY());
					break;
				}
				case MouseEvent.BUTTON2:
				{
					keys.MOUSE_RIGHT = true;
					keys.MOUSE_X = e.getX();
					keys.MOUSE_Y = e.getY();
					break;
				}
				case MouseEvent.BUTTON3:
				{
					keys.MOUSE_X = e.getX();
					keys.MOUSE_Y = e.getY();
					break;
				}
			}
		}
		
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if(!GAME_STARTED)
		{
			;
		}
		else
		{
			switch(e.getButton())
			{
				case MouseEvent.BUTTON1:
				{
					keys.MOUSE_LEFT = false;
					break;
				}
				case MouseEvent.BUTTON2:
				{
					keys.MOUSE_RIGHT = false;
					break;
				}
				case MouseEvent.BUTTON3:
				{
					break;
				}
			}
		}
	}
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(!GAME_STARTED)
		{
			if(e.getKeyChar() == 'l')
			{	
				ents = new ArrayList<Entity>();
				player = new Entity(Entity.BODY);
				offset = new Vector(0,0);
				
				
				
				do 
				{
					player.pos = new Vector(Math.random()*2 - 1, Math.random()*2 - 1);
				}while(insideGeometry(player.pos) != null);
				
				GAME_STARTED = true;
			}
			else if(e.getKeyChar() == 'k')
			{
				PLACE_GRAPH = !PLACE_GRAPH;
				
				points.clear();
			}
			else if(e.getKeyChar() == 'm')
			{
				points.clear();
				gameMap.opendialog();
				offset = new Vector(0,0);
			}
			else if(e.getKeyChar() == 'w')
				offset.addset(new Vector(0,1));
			else if(e.getKeyChar() == 's')
				offset.addset(new Vector(0,-1));
			else if(e.getKeyChar() == 'a')
				offset.addset(new Vector(-1,0));
			else if(e.getKeyChar() == 'd')
				offset.addset(new Vector(1,0));
		}
		
		char key = e.getKeyChar();
		
		switch (key)
		{
			case 'w':
			{
				keys.UP = true;
				keys.DOWN = false;
				break;
			}
			case 's':
			{
				keys.DOWN = true;
				keys.UP = false;
				break;
			}
			case 'a':
			{
				keys.LEFT = true;
				keys.RIGHT = false;
				break;
			}
			case 'd':
			{
				keys.RIGHT = true;
				keys.LEFT = false;
				break;
			}
			case 'q':
			{
				weapon = !weapon;
				break;
			}
			default:
			{
				break;
			}
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e)
	{
		
		char key = e.getKeyChar();
		
		switch (key)
		{
			case 'w':
			{
				keys.UP = false;
				break;
			}
			case 's':
			{
				keys.DOWN = false;
				break;
			}
			case 'a':
			{
				keys.LEFT = false;
				break;
			}
			case 'd':
			{
				keys.RIGHT = false;
				break;
			}
			default:
			{
				break;
			}
		}
		
		
	}
	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////GAME LOGIC///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean isGameContinuing()
	{
		if(player.life <= 0)
		{
			ents.clear();
			descWithPlayer = new Dijkstra.Description();
			player.pos.set(0,0);
			
			return false;
		}
		
		return true;
	}
	/**
	 * Set the player's velocity
	 */
	public void stepPlayer()
	{
		player.v.set(0,0);
		
		if(keys.UP)
		{
			player.v.y += 1;	
		}
		else if(keys.DOWN)
		{
			player.v.y += -1;
		}
		if(keys.RIGHT)
		{
			player.v.x += 1;
		}
		else if(keys.LEFT)
		{
			player.v.x += -1;
		}
		player.v.unitize();
		player.v.scaleset(PLAYER_SPEED);
		
		for(Entity e : ents)
		{
			if((e.TYPE & Entity.BODY) != 0)
			{
				if(e.pos.skew(player.pos) <= 0.02)
				{
					player.life -= 0.01;
				}
			}
		}
	}
	
	/**
	 * Move the player in the map
	 */
	public void stepPlayerPos()
	{
		Vector nl = player.pos.add(player.v);
		
		BlockingVector block = Triangle.calcIntersect(player.pos, nl, gameMap.geo);
		if(block.block == null || block.t > 1)
		{
			player.pos.addset(player.v);
		}
		else
		{	
			//Keep projecting onto blocking vector until you are no longer being blocked
			do
			{
				nl = player.pos.add(nl.sub(player.pos).projectOnto(block.block));
				block = Triangle.calcIntersect(player.pos, nl, gameMap.geo);				
			}while(block.block != null && block.t < 1);
			player.pos.set(nl);
		}
	}
	/**
	 * set the graph with the player to a new graph
	 */
	public void stepPlayerGraph()
	{
		ArrayList<Vector> extraNodes = new ArrayList<Vector>();
		ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
		extraNodes.add(player.pos);
		
		for(Vector v: gameMap.desc.getNodes())
		{
			boolean canSee = Triangle.clearline(player.pos, v, gameMap.geo);
			
			if(canSee)
			{
				extraEdges.add(new Dijkstra.Edge(player.pos, v));
			}
		}
		
		descWithPlayer = new Dijkstra.Description(gameMap.desc, extraNodes, extraEdges);
	}
	/**
	 * Move all projetiles 
	 */
	public void stepCalculateProjectileHits()
	{
		for(Entity e : ents)
		{
			if(e.is(Entity.PROJECTILE))
			{
				Vector nl = e.pos.add(e.v);
				double t = Triangle.calcIntersect(e.pos, nl, gameMap.geo).t;
				
				if(t > 1)
				{
					e.pos.addset(e.v);
				}
				else
				{
					toRemove.add(e);
				}
			}
			
			if(e.is(Entity.LASER))
			{
				if(e.life <= 0)
				{
					toRemove.add(e);
				}
				else
				{
					e.life--;
				}
			}
		}
		
		for(Entity e : ents)
		{
			if(e.is(Entity.BODY))
			{
				for(Entity f : ents)
				{	
					double skew = e.pos.skew(f.pos);
					if(f.is(Entity.PROJECTILE) && f != e && skew < 0.01)
					{
							toRemove.add(f);
							toRemove.add(e);
							f.TYPE = Entity.NULL;
						
					}
				}
			}
		}
	}
	
	public void removeEntities()
	{
		ents.removeAll(toRemove);
	}
	/**
	 * Spawns a monster in the game area
	 */
	public void stepSpawnMonster()
	{
		if(countMonsters() < 500 && Math.random() < 0.50)
		{
			Vector pos = new Vector(Math.random()*2 - 1, Math.random()*2 - 1);
			
			if(insideGeometry(pos) == null)
			{
				Entity monster = new Entity(Entity.BODY);
				monster.pos = pos;
				monster.v = new Vector();
				
				ents.add(monster);
				
			}
		}
	}
	
	/**
	 * Go through each entity and set its headTo to the next location to get to the player
	 */
	public void stepPathMonsters()
	{
		counter.set(0);
		progress.set(0);
		
		synchronized(progress)
		{
			for(int i = 0 ; i < THREAD_COUNT ; i++)
				es.execute(this);
			
			while(progress.get() < ents.size())
			{
				try
				{
					progress.wait();	
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
				
			}
			
		}
	}
	
	/**
	 * Look at each entity that is of type Entity.BODY, and move it to e.headTo, without intersecting with any other entity.
	 */
	public void stepMoveMonsters()
	{
		for(Entity e : ents)
		{
			if(e.is(Entity.BODY))
			{	
				e.v = e.headTo.sub(e.pos).unitize();
				e.v.scaleset(PLAYER_SPEED*0.9);
				Vector nl = e.pos.add(e.v);
				Vector nv = new Vector(e.v);
				
				for(Entity f : ents)
				{	
					double skew = e.pos.skew(f.pos);
					if(f != e && skew < 0.01)
					{
						if(f.is(Entity.BODY))
						{
							Vector dir = nl.sub(f.pos);	
							nv.addset((dir.scale(0.5/(skew*skew))).scale(PLAYER_SPEED*0.9));
						}
					}
				}
				nl = e.pos.add(nv.unit().scale(PLAYER_SPEED*0.9));
				BlockingVector block = Triangle.calcIntersect(e.pos, nl, gameMap.geo);
				
				if(block.block == null || block.t > 1)
				{
					e.pos.set(nl);
				}
				else
				{		
					do
					{
						nl = e.pos.add(nl.sub(e.pos).projectOnto(block.block));
						block = Triangle.calcIntersect(e.pos, nl, gameMap.geo);				
					}while(block.block != null && block.t < 1);
					
					e.pos.set(nl);
				}
			}
		}
	}
	/**
	 * Incrementally alter the games state.
	 */
	public void stepGame()
	{
		GAME_STARTED = isGameContinuing() && GAME_STARTED;
		if(GAME_STARTED)
		{
			this.stepPlayer();
			this.stepPlayerPos();
			this.stepPlayerGraph();
			
			this.stepCalculateProjectileHits();
			this.removeEntities();
			
			this.stepSpawnMonster();
			this.stepPathMonsters();
			this.stepMoveMonsters();
		}
	}
	
	/**
	 * Calculates by Dijkstra's algorithm, the best route a entity should follow to get to the player.
	 * @param e The entity in question
	 * @return The point that the entity in question should move to
	 */
	private Vector getNextMoveTo(Entity e)
	{
		Vector pos = player.pos;
		
		//BlockingVector bv = Triangle.calcIntersect(e.pos, player.pos, gameMap.geo);
		
		boolean canSee = Triangle.clearline(e.pos, pos, gameMap.geo);
		
		if(!canSee)
		{
			
			ArrayList<Vector> extraNodes = new ArrayList<Vector>();
			ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
			extraNodes.add(e.pos);
			
			for(Vector v: gameMap.desc.getNodes())
			{
				canSee = Triangle.clearline(e.pos, v, gameMap.geo);
				
				if(canSee)
				{
					extraEdges.add(new Dijkstra.Edge(e.pos, v));
				}
			}
			
			Dijkstra.Description temp = new Dijkstra.Description(descWithPlayer, extraNodes, extraEdges);
			
			ArrayList<Vector> v = Dijkstra.getShortestPath(e.pos, player.pos, temp);
			
			return v.get(v.size() - 2);
			
			
		}
		
		return pos;
		
		
	}
	/**
	 * 
	 * @param x the location of the mouse on screen
	 * @param y the location of the mouse on screen
	 */
	public void fireWeapon(double x, double y)
	{
		if(weapon)
		{
			Entity temp = new Entity(Entity.LASER);
			
			temp.pos.set(player.pos);
			temp.v.set(translateToReal(x, y));//(2*x - canvas.getWidth())/canvas.getWidth() + player.pos.x, -(2*y - canvas.getHeight())/canvas.getHeight() + player.pos.y); //for laser, the velocity is actually just another point
			double t = Triangle.calcIntersect(temp.pos, temp.v, gameMap.geo).t; // find the closest intersection of pos -> v
			temp.v.set(temp.pos.add(temp.v.sub(temp.pos).scale(t))); //v := (v-pos)*t + pos
			
			ArrayList<Entity> to_remove = new ArrayList<Entity>();
			
			for(Entity e : ents)
			{
				if((e.TYPE & Entity.BODY) != 0)
				{
					double cos = e.pos.sub(temp.pos).cos(temp.v.sub(temp.pos));
					
					Vector ab = temp.v.sub(temp.pos);
					
					if(e.pos.distance(temp.pos, temp.v) < 0.01 && cos >= 0  && e.pos.sub(temp.pos).projectOnto(ab).magsqr() <= ab.magsqr())
					{
						to_remove.add(e);
					}
				}
			}
			ents.removeAll(to_remove);
			
			ents.add(temp);
		}
		else	
		{
			Entity temp = new Entity(Entity.PROJECTILE);
			temp.v.set(translateToReal(x,y).sub(player.pos));
			temp.v.unitize();
			temp.v.scaleset(0.01);
			temp.pos.set(player.pos);
			ents.add(temp);
		}
		
	}
	
	public int countMonsters()
	{
		int count = 0;
		for(Entity e: ents)
		{
			if((e.TYPE & Entity.BODY) != 0)
				count++;
		}
		return count;
	}
	
	
	/**
	 * Translates screen space to game space.  
	 * 
	 * An example of a use would be to translate where a user clicked to where its position is in game
	 * @param x the x-coordinate of for example the cursor
	 * @param y the y-coordinate of for example the cursor
	 * @return the equivalent position in game space
	 */
	public Vector translateToReal(double x, double y)
	{
		return new Vector(
				(2*x - canvas.getWidth())/canvas.getWidth(),
				(2*(canvas.getHeight() - y) - canvas.getHeight())/canvas.getHeight()
				).add(player.pos).add(offset);
	}
}

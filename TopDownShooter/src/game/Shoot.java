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

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * 
 * @author Ben
 * The game class includes all the rendering mouse handling
 */
public class Shoot implements Runnable
{
	//Constants
	public static final boolean DEBUG = false;
	private static final long serialVersionUID = 1L;
	public static final double SNAP_DISTANCE = 0.025;
	public static final double PLAYER_SPEED = 0.005;
	public static final double PARTICLE_SPEED = 0.02;
	public static final int MAX_MONST = 500;
	public static final double MONST_SPEED = 0.8;
	public static final double MONST_SIZE = 0.01;
	public static final double SPAWN_PROB = 1;
	public static final double SPAWN_DIST = 1.5;
	public static final double SPAWN_DIST_VARIATION = 1.5;
	public static final double MONST_R = 0.2;
	public static final double MONST_G = 0.2;
	public static final double MONST_B = 0.2;
	public static final double MONST_R_OFFSET = 0;
	public static final double MONST_G_OFFSET = 0.2;
	public static final double MONST_B_OFFSET = 0;
	public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
	
	//Service objects
	private ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
	private FPSAnimator animator;
	protected GLCanvas canvas;
	private Display disp;
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();

	//Control objects
	private ArrayList<Entity> toRemove = new ArrayList<Entity>();
	
	//Control variables
	protected boolean GAME_STARTED = false;
	protected boolean PLACE_GRAPH = false;
	private long gametime = 0;
	protected Vector offset = new Vector(0,0);
	
	//Game objects
	protected Entity player = new Entity(Entity.SOLID);
	protected ArrayList<Entity> ents = new ArrayList<Entity>();
	KeyList keys = new KeyList();
	
	protected ArrayList<Vector> points = new ArrayList<Vector>();	
	MapWrapper mw = new MapWrapper();
	
	//Interface objects
	protected ControlMouse mcontrol = new ControlMouse(this);
	protected ControlKeyboard kbcontrol = new ControlKeyboard(this);
	
	//Game variables
	boolean weapon = false;
	
	Shoot()
	{
		
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setSampleBuffers(true);
		caps.setNumSamples(8);
		
		canvas = new GLCanvas(caps);
		canvas.setSize(1000, 1000);
		canvas.addMouseListener(mcontrol);
		canvas.addKeyListener(kbcontrol);
		
		disp = new Display(this);
		disp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		disp.setSize(1000,1000);
		disp.add(canvas);
		disp.setVisible(true);
		disp.addKeyListener(kbcontrol);
		
		canvas.addGLEventListener(disp);
		animator = new FPSAnimator(canvas, 50);
		
	}
	
	public boolean isGameRunning()
	{
		return this.GAME_STARTED;
	}
	
	/**
	 * @return the player
	 */
	public Vector getPlayerPos()
	{
		return player.pos.copy();
	}
	
	/**
	 * @return the offset
	 */
	public Vector getOffset()
	{
		return offset.copy();
	}
	
	public void startGame()
	{
		animator.start();
	}
	
	public Vector insideGeometry(Vector t)
	{	
		for(Triangle tria : mw.gameMap.geo)
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
	//////////////////////////////////////////GAME LOGIC///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean isGameContinuing()
	{
		if(player.life <= 0)
		{
			ents.clear();
			mw.descWithPlayer = new Dijkstra.Description();
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
				if(!DEBUG && e.pos.skew(player.pos) <= MONST_SIZE)
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
		
		BlockingVector block = Triangle.calcIntersect(player.pos, nl, mw.gameMap.geo);
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
				block = Triangle.calcIntersect(player.pos, nl, mw.gameMap.geo);				
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
		
		for(Vector v: mw.gameMap.desc.getNodes())
		{
			boolean canSee = Triangle.clearline(player.pos, v, mw.gameMap.geo);
			
			if(canSee)
			{
				extraEdges.add(new Dijkstra.Edge(player.pos, v));
			}
		}
		
		mw.descWithPlayer = new Dijkstra.Description(mw.gameMap.desc, extraNodes, extraEdges);
	}
	/**
	 * Move all projectiles
	 */
	public void stepCalculateProjectileHits()
	{
		for(Entity e : ents)
		{
			if(e.is(Entity.PROJECTILE))
			{
				Vector nl = e.pos.add(e.v);
				double t = Triangle.calcIntersect(e.pos, nl, mw.gameMap.geo).t;
				
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
					if(f.is(Entity.PROJECTILE) && f != e && skew < MONST_SIZE)
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
		if(countMonsters() < MAX_MONST && Math.random() < SPAWN_PROB)
		{
			double theta = Math.random()*Math.PI*2;
			double r = SPAWN_DIST + Math.random()*SPAWN_DIST_VARIATION;
			
			Vector pos = new Vector(player.pos.x + r*Math.cos(theta), player.pos.y + r*Math.sin(theta));
			
			if(insideGeometry(pos) == null)
			{
				Entity monster = new Entity(Entity.BODY);
				monster.pos = pos;
				monster.r = Math.random()*MONST_R+MONST_R_OFFSET;
				monster.g = Math.random()*MONST_G+MONST_G_OFFSET;
				monster.b = Math.random()*MONST_B+MONST_B_OFFSET;
				
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
		
		for(Entity e: ents)
		{
			e.headTo = null;
		}
		
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
				e.v.scaleset(PLAYER_SPEED*MONST_SPEED);
				Vector nl = e.pos.add(e.v);
				Vector nv = new Vector(e.v);
				
				for(Entity f : ents)
				{	
					double skew = e.pos.skew(f.pos);
					if(f != e && skew < MONST_SIZE)
					{
						if(f.is(Entity.BODY))
						{
							Vector dir = nl.sub(f.pos);	
							nv.addset((dir.scale(0.5/(skew*skew))).scale(PLAYER_SPEED*MONST_SPEED));
						}
					}
				}
				nl = e.pos.add(nv.unit().scale(PLAYER_SPEED*MONST_SPEED));
				BlockingVector block = Triangle.calcIntersect(e.pos, nl, mw.gameMap.geo);
				
				if(block.block == null || block.t > 1)
				{
					e.pos.set(nl);
				}
				else
				{		
					do
					{
						nl = e.pos.add(nl.sub(e.pos).projectOnto(block.block));
						block = Triangle.calcIntersect(e.pos, nl, mw.gameMap.geo);				
					}while(block.block != null && block.t < 1 && !Triangle.tooClose(e.pos, MONST_SIZE*0.1, mw.gameMap.geo));
					
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
		
		boolean canSee = Triangle.clearline(e.pos, pos, mw.gameMap.geo);
		
		if(canSee)
		{
			return pos.copy();
		}
		
		
		for(Entity other :ents)
		{
			if(other.headTo != null && other.pos.sub(e.pos).magsqr() < 4*MONST_SIZE*MONST_SIZE && Triangle.clearline(e.pos, other.headTo, mw.gameMap.geo))
			{
				return other.headTo.copy();
			}
		}
		
		
		ArrayList<Vector> extraNodes = new ArrayList<Vector>();
		ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
		extraNodes.add(e.pos);
		
		for(Vector v: mw.gameMap.desc.getNodes())
		{
			canSee = Triangle.clearline(e.pos, v, mw.gameMap.geo);
			
			if(canSee)
			{
				extraEdges.add(new Dijkstra.Edge(e.pos, v));
			}
		}
		
		Dijkstra.Description temp = new Dijkstra.Description(mw.descWithPlayer, extraNodes, extraEdges);
		
		ArrayList<Vector> v = Dijkstra.getShortestPath(e.pos, player.pos, temp);
		
		if(v.size() > 1)
			return v.get(v.size() - 2);
		else
			return v.get(0);
				
		
		
		
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
			double t = Triangle.calcIntersect(temp.pos, temp.v, mw.gameMap.geo).t; // find the closest intersection of pos -> v
			temp.v.set(temp.pos.add(temp.v.sub(temp.pos).scale(t))); //v := (v-pos)*t + pos
			
			ArrayList<Entity> to_remove = new ArrayList<Entity>();
			
			for(Entity e : ents)
			{
				if((e.TYPE & Entity.BODY) != 0)
				{
					double cos = e.pos.sub(temp.pos).cos(temp.v.sub(temp.pos));
					
					Vector ab = temp.v.sub(temp.pos);
					
					if(e.pos.distance(temp.pos, temp.v) < MONST_SIZE && cos >= 0  && e.pos.sub(temp.pos).projectOnto(ab).magsqr() <= ab.magsqr())
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
			temp.v.scaleset(PARTICLE_SPEED);
			temp.v.addset(player.v);
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
	
	/**
	 * Prepares for the core game to begin; this means it clears entities, resets player, resets offset, places the player, and sets GAME_STARTED flag to true.
	 */
	protected void midGameReset()
	{
		ents.clear();
		player = new Entity(Entity.BODY);
		offset = new Vector(0,0);
		
		do 
		{
			player.pos = new Vector(Math.random()*2 - 1, Math.random()*2 - 1);
		}while(insideGeometry(player.pos) != null);
		
		GAME_STARTED = true;
	}
	
	/**
	 * Alternate the PLACE_GRAPH flag, clears points.
	 */
	protected void switchMappingMode()
	{
		PLACE_GRAPH = !PLACE_GRAPH;
		
		points.clear();
	}
	
	/**
	 * Open the map load/save dialog, clears points, resets offset.
	 */
	protected void openMapDialog()
	{
		points.clear();
		mw.gameMap.opendialog();
		offset = new Vector(0,0);
	}
	
	/**
	 * Increment the offset of the screen.  Takes in a key which indicates the direction to move the offset
	 * 
	 * @param e the key event containing the direction key
	 */
	protected void changeOffset(KeyEvent e)
	{
		if(e.getKeyChar() == 'w')
			offset.addset(new Vector(0,1));
		else if(e.getKeyChar() == 's')
			offset.addset(new Vector(0,-1));
		else if(e.getKeyChar() == 'a')
			offset.addset(new Vector(-1,0));
		else if(e.getKeyChar() == 'd')
			offset.addset(new Vector(1,0));
	}
	
	public static boolean charIsOneOf(char x, char ... elements)
	{
		for(char c: elements)
			if(x == c)
				return true;
		return false;
						
	}
}

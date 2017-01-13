package game;

import geo.Dijkstra;
import geo.Triangle;
import geo.Triangle.BlockingVector;
import geo.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;


/**
 * 
 * @author Ben
 * 
 * A class describing a generic entity
 * It contains data describing:
 * Position
 * Velocity
 * Hitbox
 * Life and
 * Type of entity
 *
 */
class Entity
{
	public static final int NULL		= 0;
	public static final int PROJECTILE	= 1;
	public static final int SOLID 		= 2;
	public static final int HITBOX 		= 4;
	public static final int BODY 		= 16;
	public static final int LASER 		= 32;
	
	
	Vector pos;
	Vector v;
	Triangle collide;
	double life = 10;
	int TYPE;
	Entity(int t)
	{
		TYPE = t;
		pos = new Vector();
		v = new Vector();
	}
}
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
public class Shoot extends JFrame implements GLEventListener, MouseListener, KeyListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FPSAnimator animator;
	private GLCanvas canvas;
	Thread runningGame;
	
	private ArrayList<Vector> points = new ArrayList<Vector>();
	private Dijkstra.Description desc = new Dijkstra.Description();
	private Dijkstra.Description descWithPlayer = new Dijkstra.Description();
	private ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	
	public boolean GAME_STARTED = false;
	public boolean PLACE_GRAPH = false;
	public long gametime = 0 ;
	public Entity player = new Entity(Entity.SOLID);
	private ArrayList<Entity> ents = new ArrayList<Entity>();
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
		
		animator = new FPSAnimator(canvas, 100);
		
	}
	public void start()
	{
		animator.start();
	}
	
	public Vector insideGeometry(Vector t)
	{	
		for(Triangle tria : triangles)
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
	
	@Override
	public void display(GLAutoDrawable drawable)
	{
		stepGame();
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glLoadIdentity();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor3d(0,0,0);
		
		gl.glVertex2d(-1, 1);
		gl.glVertex2d(-1, -1);
		gl.glVertex2d(1, -1);
		gl.glVertex2d(1, 1);
		
		gl.glEnd();
		gl.glColor3d(1,1,1);
		/*gl.glBegin(gl.GL_POINTS);
		for(Vector v: points)
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();*/
		gl.glColor3d(0.75,0.75,0.75);
		gl.glBegin(gl.GL_TRIANGLES);
		
		for(Triangle t : triangles)
		{
			gl.glVertex2d(t.a.x, t.a.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.c.x, t.c.y);
		}
		
		gl.glEnd();
		
		gl.glBegin(gl.GL_LINES);
		
		for(Triangle t : triangles)
		{
			gl.glVertex2d(t.a.x, t.a.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.c.x, t.c.y);
			gl.glVertex2d(t.c.x, t.c.y);
			gl.glVertex2d(t.a.x, t.a.y);
		}
		
		gl.glEnd();
		
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
		
		
		if(canvas.getMousePosition() != null)
		{
			java.awt.Point pos = canvas.getMousePosition();
			
			gl.glBegin(gl.GL_POINTS);
			gl.glColor3d(Math.random(), Math.random(), Math.random());
			gl.glVertex2d((2*(double)pos.getX() - (double) canvas.getSize().getWidth())/(double)canvas.getSize().getWidth(),
					-(2*(double)pos.getY() - (double)canvas.getSize().getHeight())/(double)canvas.getSize().getHeight());
			gl.glEnd();
		}
		
		
		
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : desc.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : desc.getNodes())
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();
		
		
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
		
		
		/*
		gl.glColor4d(1,0,0,0.5);
		gl.glBegin(GL.GL_TRIANGLES);
		
		gl.glVertex2d(0, 0);
		gl.glVertex2d(1, 1);
		gl.glVertex2d(0, 1);
		
		gl.glEnd();
		 */
		
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
	//////////////////////////////////////////START OF GAME LOGIC//////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		Shoot shoot = new Shoot();
		shoot.start();
	}
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
				Vector temp = new Vector();
				temp.x =(double) 2*e.getX()/ (double)canvas.getWidth() - 1.0;
				temp.y =(double) 2*(1 - e.getY()/(double) canvas.getHeight()) - 1.0;
				
				Vector temp2 = desc.getSkewed(temp, 0.05*0.05);
				if(temp2 == null)
				{
					points.add(temp);
					desc.addNode(temp);
				}
				else
				{
					points.add(temp2);
					desc.addNode(temp2);
				}
				
				if(points.size() == 2)
				{
					desc.addEdge(points.remove(0), points.remove(0));
					points.clear();
				}
			}
			else
			{
				Vector temp = new Vector();
				temp.x =(double) 2*e.getX()/ (double)canvas.getWidth() - 1.0;
				temp.y =(double) 2*(1 - e.getY()/(double) canvas.getHeight()) - 1.0;
				
				for(Triangle t: triangles)
				{
					Vector temp1 = t.skew(temp, 0.05);
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
					triangles.add(t);
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
					keys.MOUSE_LEFT = true;
					break;
				}
				case MouseEvent.BUTTON2:
				{
					keys.MOUSE_RIGHT = true;
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
				GAME_STARTED = true;
			}
			else if(e.getKeyChar() == 'k')
			{
				PLACE_GRAPH = !PLACE_GRAPH;
				
				points.clear();
			}
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
		if(GAME_STARTED)
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
		
	}
	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
	
	public void stepGame()
	{
		if(GAME_STARTED)
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
			player.v.scaleset(0.0025);
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////SPAWNING MONSTER//////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			if(countMonsters() < 50 && Math.random() < 0.20)
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
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			{
				Vector nl = player.pos.add(player.v);
				
				BlockingVector block = Triangle.calcIntersect(player.pos, nl, triangles);
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
						block = Triangle.calcIntersect(player.pos, nl, triangles);				
					}while(block.block != null && block.t < 1);
					player.pos.set(nl);
				}
				
				ArrayList<Vector> extraNodes = new ArrayList<Vector>();
				ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
				extraNodes.add(player.pos);
				
				for(Vector v: desc.getNodes())
				{
					BlockingVector canSee = Triangle.calcIntersect(player.pos, v, triangles);
					
					if(canSee.block == null || canSee.t >= 1)
					{
						extraEdges.add(new Dijkstra.Edge(player.pos, v));
					}
				}
				
				descWithPlayer = new Dijkstra.Description(desc, extraNodes, extraEdges);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			{
				ArrayList<Entity> toRemove = new ArrayList<Entity>();
				
				for(Entity e : ents)
				{
					Vector headTo = getNextMoveTo(e);
					
					if((e.TYPE & Entity.BODY) == Entity.BODY)
					{
						e.v = headTo.sub(e.pos).unitize();
						e.v.scaleset(0.002);
						
						Vector nl = e.pos.add(e.v);
						
						Vector nv = new Vector(e.v);
						for(Entity f : ents)
						{
							double skew = e.pos.skew(f.pos);
							if(f != e && skew < 0.01)
							{
								if(f.TYPE == Entity.PROJECTILE)
								{
									toRemove.add(f);
									toRemove.add(e);
									f.TYPE = Entity.NULL;
								}
								else
								{
									
									Vector dir = nl.sub(f.pos);
									
									nv.addset((dir.scale(0.5/(skew*skew))).scale(0.002));
								}
								
							}
						}
						
						nl = e.pos.add(nv.unit().scale(0.002));
						BlockingVector block = Triangle.calcIntersect(e.pos, nl, triangles);
						
						if(block.block == null || block.t > 1)
						{
							e.pos.set(nl);
						}
						else
						{		
							do
							{
								nl = e.pos.add(nl.sub(e.pos).projectOnto(block.block));
								block = Triangle.calcIntersect(e.pos, nl, triangles);				
							}while(block.block != null && block.t < 1);
							
							e.pos.set(nl);
						}
						
					}
				}
				
				ents.removeAll(toRemove);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			{
				int size = ents.size();
				ArrayList<Entity> ents_to_remove = new ArrayList<Entity>();
				for(int i = 0; i < size ; i++)
				{
					Entity e = ents.get(i);
					if((e.TYPE & Entity.PROJECTILE) != 0)
					{
						
						
						Vector nl = e.pos.add(e.v);
						double t = Triangle.calcIntersect(e.pos, nl, triangles).t;
						
						if(t > 1)
						{
							e.pos.addset(e.v);
						}
						else
						{
							ents_to_remove.add(e);
						}
					}
					
					if((e.TYPE & Entity.LASER) != 0)
					{
						if(e.life <= 0)
						{
							ents_to_remove.add(e);
						}
						else
						{
							e.life--;
						}
					}
				}
				ents.removeAll(ents_to_remove);
			}
		}
	}
	
	private Vector getNextMoveTo(Entity e)
	{
		Vector pos = player.pos;
		
		BlockingVector bv = Triangle.calcIntersect(e.pos, player.pos, triangles);
		
		if(bv.block != null && bv.t <= 1)
		{
			
			ArrayList<Vector> extraNodes = new ArrayList<Vector>();
			ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
			extraNodes.add(e.pos);
			
			for(Vector v: desc.getNodes())
			{
				BlockingVector canSee = Triangle.calcIntersect(e.pos, v, triangles);
				
				if(canSee.block == null || canSee.t >= 1)
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
			temp.v.set((2*x - canvas.getWidth())/canvas.getWidth(), -(2*y - canvas.getHeight())/canvas.getHeight()); //for laser, the velocity is actually just another point
			double t = Triangle.calcIntersect(temp.pos, temp.v, triangles).t; // find the closest intersection of pos -> v
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
			temp.v.set(2*(x)/(double)canvas.getWidth() - 1.0 - player.pos.x, (2*((canvas.getHeight() - y))/(double)canvas.getHeight()) - 1.0 - player.pos.y);
			temp.v.unitize();
			temp.v.scaleset(0.005);
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
}

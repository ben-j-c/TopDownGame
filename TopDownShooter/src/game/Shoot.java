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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import game.multithread.MT_EntMovement;
import game.multithread.MT_WeaponHit;

/**
 * 
 * @author Ben
 * The game class includes all the rendering mouse handling
 */
public class Shoot
{
	//Constants
	public static final boolean DEBUG = true;
	private static final long serialVersionUID = 1L;
	public static final double SNAP_DISTANCE = 0.025;
	public static final double PLAYER_SPEED = 0.009;
	public static final double PARTICLE_SPEED = 0.02;
	public static final int MAX_MONST = 1000;
	public static final double MONST_SPEED = 0.8;
	public static final double MONST_SIZE = 0.025;
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
	public static final Random r = new Random(); 
	private ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
	private FPSAnimator animator;
	protected GLCanvas canvas;
	private Display disp;
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	//MT_Pathing mtPathing;
	MT_EntMovement mtEntMov;
	MT_WeaponHit mtWepHit;
	
	//Control objects
	private ArrayList<Entity> toRemove = new ArrayList<Entity>();
	
	//Control variables
	protected boolean GAME_STARTED = false;
	protected boolean PLACE_GRAPH = false;
	private long gametime = 0;
	protected Vector offset = new Vector(0,0);
	
	//Game objects
	protected Entity player = new Entity(Entity.BODY);
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
		System.out.printf("THREAD_COUNT:%4d\n", THREAD_COUNT);
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
		
		mtWepHit = new MT_WeaponHit(ents, mw, es, toRemove);
		mtEntMov = new MT_EntMovement(this, ents, mw, es);
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
		Vector nv = new Vector();//new velocity
		
		player.v.set(0,0);
		
		if(keys.UP)
		{
			nv.y += 1;	
		}
		else if(keys.DOWN)
		{
			nv.y += -1;
		}
		if(keys.RIGHT)
		{
			nv.x += 1;
		}
		else if(keys.LEFT)
		{
			nv.x += -1;
		}
		
		
		
		
		
		
		nv.unitize();
		nv.scaleset(PLAYER_SPEED);
		for(int i = 0 ; i < ents.size() ; i++)
		{	
			Entity f = ents.get(i);
			double skew = player.pos.skew(f.pos);
			if(f != player && f.is(Entity.BODY))
			{
				if(skew < MONST_SIZE*0.5)//if the player is too close, push them
				{
					
					Vector dir = player.pos.sub(f.pos);
					nv.addset((
							dir.scale((MONST_SIZE*MONST_SIZE)/(skew*skew)))
							.scale(PLAYER_SPEED*MONST_SPEED*MONST_SPEED));
					
				}
				else if(skew < MONST_SIZE)//if they are within reach, grab them
				{
					/*Vector dir = f.pos.sub(player.pos);
					nv.addset((
							dir.scale((MONST_SIZE*MONST_SIZE)/(skew*skew)))
							.scale(PLAYER_SPEED*MONST_SPEED));*/
					nv.scaleset(0.99);
				}
			}
		}
		
		
		//player velocity is the sum of the pushing velocity and default velocity
		player.v.set(nv);
		
		for(Entity e : ents)
		{
			if(e.is(Entity.MONST))
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
		player.pos.set(Triangle.findClosestPos(player.pos, player.v, mw.gameMap.geo));
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
		mtWepHit.doCycle();
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
		for(;countMonsters() < MAX_MONST && Math.random() < SPAWN_PROB;)
		{
			double theta = Math.random()*Math.PI*2;
			double r = SPAWN_DIST + Math.random()*SPAWN_DIST_VARIATION;
			
			Vector pos = new Vector(player.pos.x + r*Math.cos(theta), player.pos.y + r*Math.sin(theta));
			
			if(!Triangle.tooClose(pos, Triangle.DEFAULT_ERROR, mw.gameMap.geo))
			{
				Entity monster = new Entity(Entity.MONST | Entity.BODY);
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
		//mtPathing.doCycle();
	}
	
	/**
	 * Look at each entity that is of type Entity.MONST, and move it to e.headTo, without intersecting with any other entity.
	 */
	public void stepMoveMonsters()
	{
		mtEntMov.doCycle();
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
			this.stepMoveMonsters();
		}
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
				if(e.is(Entity.MONST))
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
			if(e.is(Entity.MONST))
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
		}while(Triangle.tooClose(player.pos, Triangle.DEFAULT_ERROR, mw.gameMap.geo));
		
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

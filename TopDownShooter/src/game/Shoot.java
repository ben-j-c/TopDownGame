package game;

import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import game.Entities.Body;
import game.Entities.Dynamic;
import game.Entities.InventoryItem;
import game.Entities.Monster;
import game.Entities.Pathable;
import game.Entities.Projectile;
import game.Entities.Weapon;
import game.Entities.Monsters.BlobMonster;
import game.Entities.Monsters.GMonster;
import game.Entities.Monsters.Player;
import game.Entities.Weapons.Flamethrower;
import game.multithread.MT_EntMovement;
import game.multithread.MT_Generic;

/**
 * 
 * @author Ben
 * The game class includes all the rendering mouse handling
 */
public class Shoot implements Runnable
{
	//Constants
	public static boolean DEBUG = true;
	public static final double SNAP_DISTANCE = 0.025;
	public static final double PLAYER_SPEED = 0.005;
	public static final int INVENTORY_SIZE = 5;
	public static final double PARTICLE_SPEED = 0.02;
	public static final int MAX_MONST = 6000;
	public static final double MONST_SPEED = 0.5;
	public static final double MONST_SIZE = 0.005;
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
	private static Shoot inst; 
	private final ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
	private final ScheduledExecutorService gameThread = Executors.newSingleThreadScheduledExecutor();
	private FPSAnimator animator;
	protected GLCanvas canvas;
	private Display disp;
	//MT_Pathing mtPathing;
	MT_EntMovement mtEntMov;//Use of redundant class
	MT_Generic<Projectile> mtProjectiles;
	MT_Generic<Dynamic> mtDynamics;
	
	//Control variables
	protected boolean GAME_STARTED = false;
	protected boolean PLACE_GRAPH = false;
	protected long gameTime = 0;
	
	//Game objects
	protected Player player = new Player();
	public final EntityWrapper entityWrapper = new EntityWrapper();
	protected final Inventory inv = new Inventory(INVENTORY_SIZE); public int invIdx = 0;
	public KeyList keys = new KeyList();int mouseX = 0, mouseY = 0;
	protected Vector offset = new Vector(0,0);
	private Entity[] cycleData;
	
	protected ArrayList<Vector> points = new ArrayList<Vector>();	
	public final MapWrapper mw = new MapWrapper();
	
	//Interface objects
	protected final ControlMouse mcontrol = new ControlMouse(this);
	protected final ControlKeyboard kbcontrol = new ControlKeyboard(this);
	
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
		
		mtProjectiles = new MT_Generic<Projectile>(entityWrapper.projectiles, es);
		mtDynamics = new MT_Generic<Dynamic>(entityWrapper.dynamics, es);
		mtEntMov = new MT_EntMovement(entityWrapper, es);
		
		inst = this;
	}
	
	public boolean isGameRunning()
	{
		return this.GAME_STARTED;
	}
	
	/**
	 * 
	 * @return the player's position
	 */
	public Vector getPlayerPos()
	{
		return player.pos.copy();
	}
	
	public Vector getPlayerV()
	{
		return player.v.copy();
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
		gameThread.scheduleAtFixedRate(this, 1, 20, TimeUnit.MILLISECONDS);
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
			entityWrapper.clear();
			mw.descWithPlayer = new Dijkstra.Description();
			player.pos.set(0,0);
			
			return false;
		}
		
		return true;
	}
	
	///////////////////////////////////////GAME STEPS/////////////////////////////////////
	///////////////////////////////////////GAME STEPS/////////////////////////////////////
	///////////////////////////////////////GAME STEPS/////////////////////////////////////
	
	/**
	 * Set the player's velocity
	 */
	private void stepPlayer()
	{
		java.awt.Point p = canvas.getMousePosition(); 
		
		cycleData = entityWrapper.ents.toArray(new Entity[0]);
		
		if(p != null)
		{
			mouseX = p.x;
			mouseY = p.y;
		}
		
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
		
		//push the player around
		nv.unitize();
		nv.scaleset(PLAYER_SPEED);
		for(int i = 0 ; i < entityWrapper.ents.size() ; i++)
		{	
			Entity f = (Entity) cycleData[i];
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
					nv.scaleset(0.95);
				}
			}
		}
		
		
		//player velocity is the sum of the pushing velocity and default velocity
		player.v.set(nv);
		
		for(Entity e : entityWrapper.ents)
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
	private void stepPlayerPos()
	{
		player.pos.set(mw.gameMap.findClosestPos(player.pos, player.v));
	}
	/**
	 * set the graph with the player to a new graph
	 */
	private void stepPlayerGraph()
	{
		ArrayList<Vector> extraNodes = new ArrayList<Vector>();
		ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
		extraNodes.add(player.pos);
		
		for(Vector v: mw.gameMap.desc.getNodes())
		{
			boolean canSee = inst.mw.gameMap.clearLine(player.pos,v);//Triangle.clearline(player.pos, v, mw.gameMap.geo);
			
			if(canSee)
			{
				extraEdges.add(new Dijkstra.Edge(player.pos, v));
			}
		}
		
		mw.descWithPlayer = new Dijkstra.Description(mw.gameMap.desc, extraNodes, extraEdges);
		mw.descWithPlayer.calculateCost(player.pos);
	}
	/**
	 * Move all projectiles
	 */
	private void stepCalculateDynamics()
	{
		if(entityWrapper.projectiles.size() > THREAD_COUNT*100)
			mtProjectiles.doCycle();
		else
			for(Projectile p: entityWrapper.projectiles)
			{
				p.doStep();
			}
		
		if(entityWrapper.dynamics.size() > THREAD_COUNT*100)
			mtDynamics.doCycle();
		else
			for(Dynamic d: entityWrapper.dynamics)
			{
				d.doStep();
			}
		
	}
	
	private void removeEntities()
	{
		entityWrapper.removeAllToRemove();
	}
	/**
	 * Spawns a monster in the game area
	 */
	private void stepSpawnMonster()
	{
		for(;countMonsters() < MAX_MONST && Math.random() < SPAWN_PROB;)
		{
			double theta = Math.random()*Math.PI*2;
			double r = SPAWN_DIST + Math.random()*SPAWN_DIST_VARIATION;
			
			Vector pos = new Vector(player.pos.x + r*Math.cos(theta), player.pos.y + r*Math.sin(theta));
			
			if(!Triangle.tooClose(pos, Triangle.DEFAULT_ERROR, mw.gameMap.geo))
			{
				Entity monster;
				
				if(this.r.nextDouble() < 0.8)
					monster = new GMonster(pos);
				else
					monster = new BlobMonster(pos);
				
				entityWrapper.autoAdd(monster);
			}
		}
	}	
	/**
	 * Look at each entity that is of type Entity.MONST, and move it to e.headTo, without intersecting with any other entity.
	 */
	private void stepMoveMonsters()
	{
		mtEntMov.doCycle();
	}
	/**
	 * Incrementally alter the games state.
	 */
	protected void stepGame()
	{
		long[] a = new long[8];
		
		GAME_STARTED = isGameContinuing() && GAME_STARTED;
		if(GAME_STARTED)
		{
			a[0] = System.currentTimeMillis();
			this.stepPlayer();
			a[1] = System.currentTimeMillis();
			this.stepPlayerPos();
			a[2] = System.currentTimeMillis();
			this.stepPlayerGraph();
			a[3] = System.currentTimeMillis();
			this.stepCalculateDynamics();
			a[4] = System.currentTimeMillis();
			this.removeEntities();
			a[5] = System.currentTimeMillis();
			this.stepSpawnMonster();
			a[6] = System.currentTimeMillis();
			this.stepMoveMonsters();
			a[7] = System.currentTimeMillis();
			gameTime++;
			
			if(DEBUG && gameTime%25 == 0)
			{
				System.out.printf("SP:%d SPP:%d SPG:%d CD:%d RE:%d SM:%d MM:%d = %d\n",
						a[1] - a[0],
						a[2] - a[1],
						a[3] - a[2],
						a[4] - a[3],
						a[5] - a[4],
						a[6] - a[5],
						a[7] - a[6],
						a[7] - a[0]);
				
				System.out.printf("ec:%d pa:%d dy:%d pr:%d\n", entityWrapper.ents.size(), entityWrapper.pathEnts.size(), entityWrapper.dynamics.size() , entityWrapper.projectiles.size());
			}
		}
	}
	
	public void run() 
	{
		synchronized(this)
		{
			stepGame();
		}
	}
	
	/////////////////////////////////////END OF GAME STEPS///////////////////////////////////
	/////////////////////////////////////END OF GAME STEPS///////////////////////////////////
	/////////////////////////////////////END OF GAME STEPS///////////////////////////////////
	
	/**
	 * 
	 * @param x the location of the mouse on screen
	 * @param y the location of the mouse on screen
	 */
	public void fireWeapon(int x, int y)
	{
		InventoryItem i = inv.getSelected();
		
		if(i == null)
			return;
		
		if(i instanceof Weapon)
		{
			Weapon w = (Weapon) i;
			w.fire(x, y);
		}
	}
	
	public void altFireWeapon(int x, int y)
	{
		InventoryItem i = inv.getSelected();
		
		if(i == null)
			return;
		
		if(i instanceof Weapon)
		{
			Weapon w = (Weapon) i;
			w.altFire(x, y);
		}
		
	}
	
	public int countMonsters()
	{
		int count = 0;
		for(Entity e: entityWrapper.ents)
		{
			if(e instanceof Monster)
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
		entityWrapper.clear();
		
		player = new Player();
		offset = new Vector(0,0);
		inv.setDefaultLoadout();
		if(!mw.gameMap.isBSPCalculated())
			mw.gameMap.calculateBSP();
		
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
	
	public static Shoot getInstance()
	{
		return inst;
	}
	
	/**
	 * inv.add(item)
	 * @param item
	 * @return
	 */
	public boolean addToInventory(InventoryItem item)
	{
		return inv.add(item);
	}
	
	/**
	 * inv.get(c)
	 * @param c
	 * @return
	 */
	public InventoryItem getInventoryItem(Class<? extends InventoryItem> c)
	{
		return inv.get(c);
	}
	
	/**
	 * Check to see if the line ab intersects any of the map geometry
	 * @param a
	 * @param b
	 * @return
	 */
	public Triangle.BlockingVector calcIntersect(Vector a, Vector b)
	{
		return Triangle.calcIntersect(a, b, mw.gameMap.geo);
	}
	
	/**
	 * Get a list of the entities that are within the radius size
	 * @param pos
	 * @param size
	 * @return
	 */
	public ArrayList<Entity> getAdjacentEnts(Vector pos, double size)
	{
		ArrayList<Entity> ret = new ArrayList<Entity>();
		
		for(int i = 0 ; i < entityWrapper.ents.size() ; i++)
		{
			Entity e = cycleData[i];
			if(e.pos.skew(pos) < size)
			{
				ret.add(e);
			}
		}
		return ret;
	}
	
	public ArrayList<Entity> getAdjacentEnts(Vector pos)
	{
		ArrayList<Entity> ret = new ArrayList<Entity>();
		
		for(int i = 0 ; i < entityWrapper.ents.size() ; i++)
		{
			Entity e = cycleData[i];
			if(e.pos.skew(pos) < e.getSize())
			{
				ret.add(e);
			}
		}
		return ret;
	}
	
	/**
	 * Get the first entity within the radius size
	 * @param pos
	 * @param size
	 * @return
	 */
	public Entity getAdjacentEnt(Vector pos, double size)
	{	
		for(int i = 0 ; i < entityWrapper.ents.size() ; i++)
		{
			Entity e = cycleData[i];
			if(e.pos.skew(pos) < size)
			{
				return e;
			}
		}
		return null;
	}
	
	public Entity getAdjacentEnt(Vector pos)
	{	
		for(int i = 0 ; i < entityWrapper.ents.size() ; i++)
		{
			Entity e = cycleData[i];
			if(e.pos.skew(pos) < e.getSize())
			{
				return e;
			}
		}
		return null;
	}
	
	public int getMouseX()
	{
		
		return mouseX;
	}
	
	public int getMouseY()
	{
		// TODO Auto-generated method stub
		return mouseY;
	}
	
	public long getGameTime()
	{
		return gameTime;
	}
}

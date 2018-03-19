package game;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;

import game.Entities.Body;
import game.Entities.Monster;
import game.Entities.Pathable;
import game.Entities.Monsters.GMonster;
import game.multithread.MT_EntMovement;
import geo.AStar;
import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;

/**
 * 
 * @author Benjamin Correia
 * 
 * A class describing a generic entity. It contains data describing various important values.
 * contains methods for pathing which can be overridden
 */
public class Entity implements Comparable<Entity>
{
	public static final int NULL		= 0;
	public static final int PROJECTILE	= 1;
	public static final int SOLID 		= 2;
	public static final int HITBOX 		= 4;
	public static final int MONST 		= 8;
	public static final int BODY 		= 16;
	public static final int LASER 		= 32;
	
	public final long id;
	private static long idIdx = 0; 
	
	public Vector pos;
	public Vector newPos;
	public Vector headTo;
	public Vector lastHeadTo;
	public Vector v;
	public double life = 100;
	
	public double r, g, b;
	public int xLow, xHigh, yLow, yHigh;
	
	int TYPE;
	
	public Entity()
	{
		synchronized(Entity.class)
		{
			id = idIdx++;
		}
		pos = new Vector();
		newPos = new Vector();
		v = new Vector();
		headTo = new Vector();
	}
	
	public Entity(int t)
	{
		this();
		TYPE = t;
	}
	
	public Entity(Entity e)
	{
		this();
		pos = new Vector(e.pos);
		v = new Vector(e.v);
		TYPE = e.TYPE;
	}
	
	public boolean is(int TYPE)
	{
		return (this.TYPE & TYPE) != 0;
	}
	
	public void applyDamage(double damage)
	{
		life -= damage;
		
		if(life <= 0)
		{
			Shoot inst = Shoot.getInstance();
			
			if(this instanceof Monster)
				((Monster) this).death();
			
			inst.entityWrapper.autoRemove(this);
		}
	}
	
	@Override
	public int compareTo(Entity e)
	{
		return (int) (this.id - e.id);
	}
	
	public void render(GL2 gl)
	{
		gl.glColor3d(r,g,b);
		Display.drawCube(gl, getSize(), pos.x, pos.y, 0.01);
	}
	
	/**
	 * Looks for the next node to move to to get to the player
	 * @return The next node that gets this entity closest to the player
	 */
	public Vector getPrecalcMove()
	{
		Shoot inst = Shoot.getInstance();
		Vector pos = inst.getPlayerPos();
		
		//BlockingVector bv = Triangle.calcIntersect(e.pos, player.pos, gameMap.geo);
		
		boolean canSee = inst.mw.gameMap.clearLine(this.pos,  pos);//Triangle.clearline(this.pos, pos, inst.mw.gameMap.geo);
		
		if(canSee)//if you can see the player, no traversal needed
		{
			return pos.copy();
		}
		else if(lastHeadTo == null || ((inst.getGameTime() + id) & 0b1111) == 0 )//every 16 ticks or if you havn't pathed
		{
			
			Vector next = this.pos;
			double cost = Triangle.LARGE_VALUE;
			for(Vector v: inst.mw.gameMap.desc.getNodes())
			{
				if( inst.mw.gameMap.clearLine(this.pos, v))
				{
					double newCost = this.pos.sub(v).mag() + inst.mw.descWithPlayer.getCost(v, pos);
					if(newCost < cost)
					{
						next = v;
						cost = newCost;
					}
				}
			}
			return next;
		}
		else
		{
			return lastHeadTo;
		}
		
	}
	
	public Vector getNextMoveTo()
	{
		Shoot inst = Shoot.getInstance();
		Vector pos = inst.getPlayerPos();
		
		//BlockingVector bv = Triangle.calcIntersect(e.pos, player.pos, gameMap.geo);
		
		boolean canSee = inst.mw.gameMap.clearLine(this.pos, pos);// Triangle.clearline(this.pos, pos, inst.mw.gameMap.geo);
		
		if(canSee)
		{
			return pos.copy();
		}
		
		
		for(Pathable p : inst.entityWrapper.pathEnts)
		{
			if(p instanceof GMonster)
			{
				GMonster other = (GMonster) p;
				if(other.headTo != null
						&& other.pos.sub(this.pos).magsqr() < 20*Shoot.MONST_SIZE
						&&  inst.mw.gameMap.clearLine(this.pos, other.headTo))//Triangle.clearline(this.pos, other.headTo, inst.mw.gameMap.geo))
				{
					return other.headTo.copy();
				}
			}
		}
		
		
		ArrayList<Vector> extraNodes = new ArrayList<Vector>();
		ArrayList<Dijkstra.Edge> extraEdges = new ArrayList<Dijkstra.Edge>();
		extraNodes.add(this.pos);
		
		for(Vector v: inst.mw.gameMap.desc.getNodes())
		{
			canSee = inst.mw.gameMap.clearLine(this.pos, v);//clearline(this.pos, v, inst.mw.gameMap.geo);
			
			if(canSee)
			{
				extraEdges.add(new Dijkstra.Edge(this.pos, v));
			}
		}
		
		Dijkstra.Description temp = new Dijkstra.Description(inst.mw.descWithPlayer, extraNodes, extraEdges);
		
		ArrayList<Vector> v = AStar.getShortestPath(this.pos, inst.getPlayerPos(), temp);
		
		if(v.size() > 1)
			return v.get(v.size() - 2);
		else
			return v.get(0);
	}
	
	public void stepMoveMonster()
	{
		Shoot inst = Shoot.getInstance();
		this.v = this.headTo.sub( this.pos).unitize();
		
		this.v.scaleset(Shoot.PLAYER_SPEED*getSpeed());
		Vector nv = new Vector(this.v);
		
		if(this instanceof Body)
		{	
			
			for( int x = xLow ; x <= xHigh ; x++)
			{
				for( int y = yLow ; y <= yHigh ; y++)
				{
					ArrayList<Entity> block = null;
					try {
						block = inst.entityWrapper.grid.get(x).get(y);}
					catch (NullPointerException e) {
						synchronized(inst.entityWrapper.grid)
						{
							System.out.println(inst.entityWrapper.grid);
							System.out.printf("%d : (%d, %d) -> (%d, %d)\n", id, xLow, yLow, xHigh, yHigh);
							System.out.printf("%d : %d, %d\n", id, x, y);
							System.exit(3);
						}
					}
					for( Entity f : block)
					{
						double skew = this.pos.skew(f.pos);
						if(f != this && skew < 2.5*getSize())
						{
							if(skew < getSize())
							{
								((Body) this).contact((Body) f);
								((Body) f).contact((Body) this);
							}
							Vector dir = this.pos.sub(f.pos).unitize();
							nv.addset(
									(dir.scale((Shoot.MONST_SIZE*getSize()/(skew*skew))))
									.scale(Shoot.PLAYER_SPEED*getSpeed()));
						}
					}
				}
			}
			
			//apply a random velocity of 5% to the current velocity to allow for more realistic movement
			nv.addset(new Vector((Shoot.r.nextDouble() - 0.5)*Shoot.MONST_SPEED*0.005, (Shoot.r.nextDouble() - 0.5)*Shoot.MONST_SPEED*0.005));
			
			//The player is also solid, so do the same as before
			double skew = this.pos.skew(inst.getPlayerPos());
			nv.addset(this.pos.sub(inst.getPlayerPos()).unit().scale((getSize()/skew)*(getSize()/skew)).scale(Shoot.PLAYER_SPEED*getSpeed()));
			this.v.set(nv.clamp(Shoot.PLAYER_SPEED*getSpeed()*1.5));
		}
		//Try to place the entity at a location closest to pos+v without intersecting geometry
		//this.newPos.set(Triangle.findClosestPos(this.pos, this.v, inst.mw.gameMap.geo));
		this.newPos.set(inst.mw.gameMap.findClosestPos(this.pos, this.v));
	}
	
	public double getSize()
	{
		return Shoot.MONST_SIZE;
	}
	
	public double getSpeed()
	{
		return Shoot.MONST_SPEED;
	}
}

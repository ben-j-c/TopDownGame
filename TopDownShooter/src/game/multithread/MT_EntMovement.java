package game.multithread;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import game.Entity;
import game.MapWrapper;
import game.Shoot;
import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;
import geo.Triangle.BlockingVector;

public class MT_EntMovement implements Runnable
{
	private ExecutorService es;
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	private ArrayList<Entity> ents;
	private MapWrapper mw;
	private Shoot inst;
	
	public MT_EntMovement(Shoot inst, ArrayList<Entity> ents, MapWrapper mw, ExecutorService es)
	{
		this.ents = ents;
		this.mw = mw;
		this.es = es;
		this.inst = inst;
	}
	
	public void doCycle()
	{
		counter.set(0);
		progress.set(0);
		
		for(Entity e : ents)
		{
			e.headTo = null;
		}
		
		synchronized(progress)
		{
			for(int i = 0 ; i < Shoot.THREAD_COUNT ; i++)
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
		
		
		for(Entity e: ents)
		{
			if(e.is(Entity.BODY))
				e.pos = e.newPos.copy();
		}
		
	}
	
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
			stepMoveMonster(e);
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
	
	/**
	 * Look at each entity that is of type Entity.BODY, and move it to e.headTo, without intersecting with any other entity.
	 */
	public void stepMoveMonster(Entity e)
	{
		if(e.is(Entity.MONST))
		{	
			e.v = e.headTo.sub(e.pos).unitize();
			
			e.v.scaleset(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED);
			Vector nl = e.pos.add(e.v);
			Vector nv = new Vector(e.v);
			
			for(int i = 0 ; i < ents.size() ; i++)
			{	
				Entity f = ents.get(i);
				double skew = e.pos.skew(f.pos);
				if(f != e && skew < Shoot.MONST_SIZE*4)
				{
					if(f.is(Entity.BODY))
					{
						Vector dir = nl.sub(f.pos);	
						nv.addset((dir.scale(Shoot.MONST_SIZE*Shoot.MONST_SIZE/(skew*skew))).scale(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED));
					}
				}
			}
			
			e.v.set(nv.clamp(Shoot.PLAYER_SPEED*1.5));
			e.newPos.set(Triangle.findClosestPos(e.pos, e.v, mw.gameMap.geo));
		}
	}
	
	private Vector getNextMoveTo(Entity e)
	{
		Vector pos = inst.getPlayerPos();
		
		//BlockingVector bv = Triangle.calcIntersect(e.pos, player.pos, gameMap.geo);
		
		boolean canSee = Triangle.clearline(e.pos, pos, mw.gameMap.geo);
		
		if(canSee)
		{
			return pos.copy();
		}
		
		
		for(Entity other :ents)
		{
			if(other.headTo != null
					&& other.pos.sub(e.pos).magsqr() < 4*Shoot.MONST_SIZE*Shoot.MONST_SIZE
					&& Triangle.clearline(e.pos, other.headTo, mw.gameMap.geo))
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
		
		ArrayList<Vector> v = Dijkstra.getShortestPath(e.pos, inst.getPlayerPos(), temp);
		
		if(v.size() > 1)
			return v.get(v.size() - 2);
		else
			return v.get(0);
				
		
		
		
	}
	
	public void stepAllMonsters()
	{
		for(Entity e : ents)
		{
			e.pos = e.newPos.copy();
		}
	}
	
	
}

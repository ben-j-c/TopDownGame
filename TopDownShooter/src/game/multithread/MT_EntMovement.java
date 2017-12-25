package game.multithread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import game.Entity;
import game.MapWrapper;
import game.Shoot;
import game.Entities.Pathable;
import geo.AStar;
import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;
import geo.Triangle.BlockingVector;

public class MT_EntMovement implements Runnable
{
	private ExecutorService es;
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	private Collection<Pathable> paths;
	private Object[] cycleData;
	
	public MT_EntMovement(Collection<Pathable> paths, ExecutorService es)
	{
		this.paths = paths;
		this.es = es;
	}
	
	public void doCycle()
	{
		counter.set(0);
		progress.set(0);
		
		////Prepath 
		for(Pathable e : paths)
		{
			e.prePath();
		}
		
		cycleData = paths.toArray();
		
		////Path
		//wait for the progress counter to reach the length of the list
		synchronized(progress)
		{
			for(int i = 0 ; i < Shoot.THREAD_COUNT ; i++)
				es.execute(this); //Path
			
			while(progress.get() < paths.size()){
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
		
		////Postpath
		for(Pathable p: paths)
		{
			p.postPath();
		}
		
	}
	
	public void run() //Path
	{
		while(counter.get() < paths.size())
		{
			Pathable p = null;
			synchronized(counter)
			{
				if(counter.get() < paths.size())
				{
					p = (Pathable) cycleData[counter.getAndIncrement()];
				}
				else
				{
					break;
				}
			}
			
			p.path();
			
			//e.headTo = getNextMoveTo(e);
			//stepMoveMonster(e);
			synchronized(progress)
			{
				progress.incrementAndGet();
			}
		}
		
		
		
		synchronized(progress)
		{
			if(progress.get() >= paths.size())
			{
				progress.notify();
			}
		}
	}
	
	/**
	 * Look at each entity that is of type Entity.BODY, and move it to e.headTo, without intersecting with any other entity.
	 */
	
	/*
	public void stepMoveMonster(Entity e)
	{
		if(e.is(Entity.MONST))
		{
			
				e.v = e.headTo.sub(e.pos).unitize();
				
				e.v.scaleset(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED);
				Vector nl = e.pos.add(e.v);
				Vector nv = new Vector(e.v);
				
				for(int i = 0 ; i < paths.size() ; i++)
				{	
					Entity f = cycleData[i];
					double skew = e.pos.skew(f.pos);
					if(f != e && skew < Shoot.MONST_SIZE)
					{
						if(f.is(Entity.BODY))
						{
							Vector dir = e.pos.sub(f.pos);
							nv.addset((
									dir.scale((Shoot.MONST_SIZE*Shoot.MONST_SIZE/(skew*skew))))
									.scale(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED));
						}
					}
				}
				
				//apply a random velocity of 5% to the current velocity to allow for more realistic movement
				nv.addset(new Vector((Shoot.r.nextDouble() - 0.5)*Shoot.MONST_SPEED*0.005, (Shoot.r.nextDouble() - 0.5)*Shoot.MONST_SPEED*0.005));
				
				//The player is also solid, so do the same as before
				double skew = e.pos.skew(inst.getPlayerPos());
				nv.addset(e.pos.sub(inst.getPlayerPos()).unit().scale((Shoot.MONST_SIZE/skew)*(Shoot.MONST_SIZE/skew)).scale(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED));
				e.v.set(nv.clamp(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED*1.5));
				
				//Try to place the entity at a location closest to pos+v without intersecting geometry
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
		
		
		for(Entity other :paths)
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
		
		ArrayList<Vector> v = AStar.getShortestPath(e.pos, inst.getPlayerPos(), temp);
		
		if(v.size() > 1)
			return v.get(v.size() - 2);
		else
			return v.get(0);
		
		
		
		
	}
	
	public void stepAllMonsters()
	{
		for(Entity e : paths)
		{
			e.pos = e.newPos.copy();
		}
	}
	*/
}

package game.multithread;

import java.util.ArrayList;

import game.Entity;
import game.MapWrapper;
import game.Shoot;
import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;



public class MT_Pathing implements Runnable
{
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	private ArrayList<Entity> ents;
	private MapWrapper mw;
	private Shoot inst;
	
	MT_Pathing(Shoot inst, ArrayList<Entity> ents, MapWrapper mw)
	{
		this.ents = ents;
		this.mw = mw;
		this.inst = inst;
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
	
}

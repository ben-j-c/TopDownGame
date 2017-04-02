package game.multithread;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import game.Entity;
import game.MapWrapper;
import game.Shoot;
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
	
	public MT_EntMovement(ArrayList<Entity> ents, MapWrapper mw, ExecutorService es)
	{
		this.ents = ents;
		this.mw = mw;
		this.es = es;
	}
	
	public void doCycle()
	{
		counter.set(0);
		progress.set(0);
		
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
		if(e.is(Entity.BODY))
		{	
			e.v = e.headTo.sub(e.pos).unitize();
			
			e.v.scaleset(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED);
			Vector nl = e.pos.add(e.v);
			Vector nv = new Vector(e.v);
			
			for(int i = 0 ; i < ents.size() ; i++)
			{	
				Entity f = ents.get(i);
				double skew = e.pos.skew(f.pos);
				if(f != e && skew < Shoot.MONST_SIZE)
				{
					if(f.is(Entity.BODY))
					{
						Vector dir = nl.sub(f.pos);	
						nv.addset((dir.scale(0.5/(skew*skew))).scale(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED));
					}
				}
			}
			nl = e.pos.add(nv.unit().scale(Shoot.PLAYER_SPEED*Shoot.MONST_SPEED));
			BlockingVector block = Triangle.calcIntersect(e.pos, nl, mw.gameMap.geo);
			
			if(block.block == null || block.t > 1)
			{
				try
				{	
					e.newPos.set(nl);
				}
				catch(NullPointerException ex)
				{
					System.out.println(e.pos);
					System.out.println(e.v);
					System.out.println(e.headTo);
					System.out.println(e);
					System.exit(1);
				}
			}
			else
			{		
				do
				{
					nl = e.pos.add(nl.sub(e.pos).projectOnto(block.block));
					block = Triangle.calcIntersect(e.pos, nl, mw.gameMap.geo);				
				}while(block.block != null && block.t < 1 && !Triangle.tooClose(e.pos, Shoot.MONST_SIZE*0.1, mw.gameMap.geo));
				
				e.newPos.set(nl);
			}
		}
	}
	public void stepAllMonsters()
	{
		for(Entity e : ents)
		{
			e.pos = e.newPos.copy();
		}
	}
}

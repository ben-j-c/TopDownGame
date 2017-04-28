package game.multithread;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import game.Entity;
import game.MapWrapper;
import game.Shoot;
import geo.Triangle;
import geo.Vector;

public class MT_WeaponHit implements Runnable
{
	private ExecutorService es;
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	private ArrayList<Entity> ents;
	private ArrayList<Entity> toRemove;
	MapWrapper mw;
	
	public MT_WeaponHit(ArrayList<Entity> ents, MapWrapper mw, ExecutorService es, ArrayList<Entity> toRemove)
	{
		this.ents = ents;
		this.mw = mw;
		this.es = es;
		this.toRemove = toRemove;
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
			
			calculateHits(e);
			
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
	
	private void calculateHits(Entity e)
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
				e.life -= 1;
			}
		}
		
		if(e.is(Entity.MONST))
		{
			for(int i = 0 ; i < ents.size() ; i++)
			{	
				Entity f = ents.get(i);
				double skew = e.pos.skew(f.pos);
				if(f.is(Entity.PROJECTILE) && f != e && skew < Shoot.MONST_SIZE)
				{
					if(toRemove == null)
						System.out.println("toRemove");
					
					synchronized(toRemove)
					{
						toRemove.add(f);
						toRemove.add(e);
					}
				}
			}
		}
		
	}
	
}

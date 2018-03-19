package game.multithread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import game.Entity;
import game.EntityWrapper;
import game.MapWrapper;
import game.Shoot;
import game.Entities.Body;
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
	private java.util.concurrent.atomic.AtomicInteger counter_sort = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger phase1 = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger phase2 = new java.util.concurrent.atomic.AtomicInteger();
	
	private EntityWrapper ew;
	private Object[] cycleData, cycleData_sort;
	
	public static final double LAMBDA = 2.5*Shoot.MONST_SIZE;
	
	public MT_EntMovement(EntityWrapper ew, ExecutorService es)
	{
		this.ew = ew;
		this.es = es;
	}
	
	public void doCycle()
	{
		counter.set(0);
		counter_sort.set(0);
		phase1.set(0);
		phase2.set(0);
		ew.grid.clear();
		
		long[] a = new long[4];
		
		////Prepath
		a[0] = System.nanoTime();
		for(Pathable e : ew.pathEnts)
		{
			e.prePath();
		}
		a[1] = System.nanoTime();
		
		cycleData = ew.pathEnts.toArray();
		cycleData_sort = ew.bodyEnts.toArray();
		
		
		////Path
		//wait for the progress counter to reach the length of the list
		for(int i = 0 ; i < Shoot.THREAD_COUNT ; i++)
			es.execute(this); //Path
		
		synchronized(phase2)
		{
			while(phase2.get() < Shoot.THREAD_COUNT) 
			{
				try
				{
					phase2.wait();
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}
		}
		
		a[2] = System.nanoTime();
		////Postpath
		for(Pathable p: ew.pathEnts)
		{
			p.postPath();
		}
		a[3] = System.nanoTime();
		
		if(Shoot.DEBUG && Shoot.getInstance().getGameTime()%25 == 0)
		{	
			System.out.printf("\tMM: pre:%d + calc:%d + post:%d = %d\n\n", a[1] - a[0], a[2] - a[1], a[3] - a[2], a[3] - a[0]);
		}
		
	}
	
	public void run() //Path
	{
		buildGrid();
		
		phase1.incrementAndGet();
		synchronized(phase1)
		{
			if(phase1.get() >= Shoot.THREAD_COUNT)
				phase1.notifyAll();
			while(phase1.get() < Shoot.THREAD_COUNT)
			{
				try
				{
					phase1.wait();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		while(counter.get() < ew.pathEnts.size())
		{
			Pathable p = null;
			synchronized(counter)
			{
				if(counter.get() < ew.pathEnts.size())
				{
					p = (Pathable) cycleData[counter.getAndIncrement()];
				}
				else
				{
					break;
				}
			}
			
			p.path();
		}
		
		synchronized(phase2)
		{
			int phase2_count = phase2.incrementAndGet();
			if(phase2_count >= Shoot.THREAD_COUNT)
			{
				phase2.notifyAll();
			}
		}
	}
	/*
	 * Add all the entities to the grid.  The grid is a 2d hash map with an array for each block 
	 */
	private void buildGrid()
	{
		while(counter_sort.get() < ew.bodyEnts.size()) //continually grab ents from this list
		{
			Entity b = null;
			synchronized(counter_sort)
			{
				if(counter_sort.get() < ew.bodyEnts.size())
				{
					b = (Entity) cycleData_sort[counter_sort.getAndIncrement()];
				}
				else
				{
					break;
				}
			}
			
			//What blocks should include this ent
			b.xLow  = (int) ((b.pos.x + LAMBDA - b.getSize())/(2*LAMBDA));
			b.xHigh = (int) ((b.pos.x + LAMBDA + b.getSize())/(2*LAMBDA));
			b.yLow  = (int) ((b.pos.y + LAMBDA - b.getSize())/(2*LAMBDA));
			b.yHigh = (int) ((b.pos.y + LAMBDA + b.getSize())/(2*LAMBDA));
			
			
			synchronized(ew.grid) //Add the blocks to the grid
			{
				for(int x = b.xLow ; x <= b.xHigh ; x++)
				{
					for(int y = b.yLow ; y <= b.yHigh ; y++)
					{
						HashMap<Integer, ArrayList<Entity>> xCol = ew.grid.get(x);
						if(xCol == null)
						{
							xCol = new HashMap<Integer, ArrayList<Entity>>(Shoot.MAX_MONST);
							ArrayList<Entity> block = new ArrayList<Entity>();
							block.add(b);
							xCol.put(y, block);
							ew.grid.put(x, xCol);
						}
						else
						{
							ArrayList<Entity> block = xCol.get(y);
							if(block == null)
							{
								block = new ArrayList<Entity>();
								block.add(b);
								xCol.put(y, block);
							}
							else
							{
								block.add(b);
							}
						}
					}
				}
			}
		}
	}
}

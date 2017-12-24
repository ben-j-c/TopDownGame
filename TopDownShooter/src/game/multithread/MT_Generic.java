package game.multithread;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import game.Shoot;
import game.Entities.Dynamic;;

public final class MT_Generic<E extends Dynamic> implements Runnable
{
	private ExecutorService es;
	
	//Index for current next job
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	//Progress counter for completed jobs
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	public boolean debug = false;
	
	Collection<E> elements;
	private E[] cycleData;
	
	public MT_Generic(Collection<E> elements, ExecutorService es)
	{
		this.elements = elements;
		this.es = es;
	}
	
	public void doCycle()
	{
		if(debug == true)
			System.out.println("doCylce@"+this);
		
		counter.set(0);
		progress.set(0);
		
		cycleData = (E[]) elements.toArray();
		
		synchronized(progress)
		{
			for(int i = 0 ; i < Shoot.THREAD_COUNT ; i++)
				es.execute(this);
			
			while(progress.get() < elements.size())
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

	@Override
	public void run()
	{
		if(debug == true)
			System.out.println("\trun@"+this);
		
		if(debug == true)
			System.out.println( "\t" + counter.get() +" & " + elements);
		while(counter.get() < elements.size())
		{
			E e = null;
			synchronized(counter)
			{
				
				if(counter.get() < elements.size())
				{
					
					e = cycleData[counter.getAndIncrement()];
				}
				else
				{
					break;
				}
			}
			
			if(debug)
				System.out.println("\t\tdoStep@"+this);
				
			
			e.doStep();
			
			synchronized(progress)
			{
				progress.incrementAndGet();
			}
		}
		
		synchronized(progress)
		{
			if(progress.get() >= elements.size())
			{
				progress.notify();
			}
			
		}
	}
}

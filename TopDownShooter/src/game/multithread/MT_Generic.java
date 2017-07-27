package game.multithread;

import java.util.List;
import java.util.concurrent.ExecutorService;

import game.Entity;
import game.Shoot;

public final class MT_Generic<E extends MT_Interface> implements Runnable
{
	private ExecutorService es;
	private java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
	private java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger();
	
	List<E> elements;
	
	public MT_Generic(List<E> elements, ExecutorService es)
	{
		this.elements = elements;
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
		while(counter.get() < elements.size())
		{
			E e = null;
			synchronized(counter)
			{
				if(counter.get() < elements.size())
				{
					e = elements.get(counter.getAndIncrement());
				}
				else
				{
					break;
				}
			}
			
			e.timeIndependentFunction();
			
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

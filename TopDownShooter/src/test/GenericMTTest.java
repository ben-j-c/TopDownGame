package test;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import game.Entities.Dynamic;
import game.multithread.MT_Generic;

public class GenericMTTest implements Dynamic
{
	double value;
	private static ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	static int size = 8000;
	
	public static void main(String [] args)
	{
		ArrayList<GenericMTTest> l = new ArrayList<GenericMTTest>();
		MT_Generic<GenericMTTest> mt = new MT_Generic<GenericMTTest>(l, es); 
		
		for(int i = 0 ; i < size ; i++)
		{
			l.add(new GenericMTTest(i));
		}
		
		
		long a1 = System.nanoTime();
		mt.doCycle();
		long b1 = System.nanoTime();
		
		l.clear();
		for(int i = 0 ; i < size ; i++)
		{
			l.add(new GenericMTTest(i));
		}
		
		
		long a2 = System.nanoTime();
		for(int i = 0 ; i < l.size(); i++)
		{
			l.get(i).doStep();
		}
		long b2 = System.nanoTime();
		
		System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXX\n\n");
		
		System.out.printf("AB: %d\nCD: %d\nCD/AB: %f\n", b1-a1, b2-a2, ((double) b2- (double) a2)/ ((double) b1-(double) a1));
		
		es.shutdown();
	}
	
	public GenericMTTest(int i)
	{
		value = i;
	}
	
	@Override
	public void doStep()
	{
		try
		{
			Thread.sleep(1);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

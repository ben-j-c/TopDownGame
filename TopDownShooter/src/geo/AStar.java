package geo;

import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

public class AStar
{
	private static class FVWrapper implements Comparable<FVWrapper>
	{
		Vector v;
		Double fScore;
		
		FVWrapper(Vector v, Double fScore)
		{
			this.v = v;
			this.fScore = fScore;
		}
		
		@Override
		public int compareTo(FVWrapper o)
		{
			if(this.fScore == o.fScore && v.equals(o.v))
				return 0;
			else if(o.fScore == null)
				return -1;
			else if(this.fScore == null)
				return 1;
			else if(this.fScore < o.fScore)
				return -1;
			else
				return 1;
		}
		
		public String toString()
		{
			return v.toString() + " " + fScore + " ";
		}
		
		
	}
	
	public static java.util.ArrayList<Vector> getShortestPath(Vector start, Vector end, Dijkstra.Description desc)
	{
		TreeSet<Vector> closedSet = new TreeSet<Vector>();
		TreeSet<FVWrapper> openSet = new TreeSet<FVWrapper>();
		openSet.add(new FVWrapper(start, heuristic(start, end)));
		
		TreeMap<Vector, Vector> cameFrom = new TreeMap<Vector, Vector>();
		TreeMap<Vector, Double> gScore = new TreeMap<Vector, Double>();
		gScore.put(start, 0.0);
		
		TreeMap<Vector, Double> fScore = new TreeMap<Vector, Double>();
		fScore.put(start, heuristic(start, end));
		
		while(!openSet.isEmpty())
		{
			FVWrapper current = openSet.first();
			openSet.remove(current);
			
			if(current.v.equals(end))
				return getReturn(cameFrom, current.v);
			
			closedSet.add(current.v);
			
			java.util.List<Vector> neighbor = desc.getNeighbor(current.v);
			
			for(int i = 0 ; i < neighbor.size() ; i++)
			{
				Vector nei = neighbor.get(i); 
				if(!closedSet.contains(nei))
				{
					FVWrapper neiOpen = new FVWrapper(nei, null); 
					
					double tenative = gScore.get(current.v) + current.v.skew(nei);
					
					Double g = gScore.get(nei);
					g = g == null? Triangle.LARGE_VALUE : g;
					if(tenative < g)
					{
						cameFrom.put(nei, current.v);
						gScore.put(nei, tenative);
						double f = g + heuristic(nei, end);
						fScore.put(nei, f);
						
						neiOpen.fScore = f;
						
						
					}
					
					openSet.add(neiOpen);
				}
			}
		}
		
		return null;
	}
	
	private static java.util.ArrayList<Vector> getReturn(TreeMap<Vector, Vector> cameFrom, Vector current)
	{
		java.util.ArrayList<Vector> path = new java.util.ArrayList<Vector>();
		while(current != null)
		{
			path.add(current);
			current = cameFrom.get(current);
		}
		
		return path;
	}
	
	private static double heuristic(Vector a, Vector b)
	{
		return a.skew(b);
	}
}

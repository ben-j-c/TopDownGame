package geo;

import java.util.TreeMap;
import java.util.TreeSet;

public class AStar
{
	public static java.util.ArrayList<Vector> getShortestPath(Vector start, Vector end, Dijkstra.Description desc)
	{
		TreeSet<Vector> closedSet = new TreeSet<Vector>();
		TreeSet<Vector> openSet = new TreeSet<Vector>();
		openSet.add(start);
		
		TreeMap<Vector, Vector> cameFrom = new TreeMap<Vector, Vector>();
		TreeMap<Vector, Double> gScore = new TreeMap<Vector, Double>();
		gScore.put(start, 0.0);
		
		TreeMap<Vector, Double> fScore = new TreeMap<Vector, Double>();
		fScore.put(start, heuristic(start, end));
		
		while(!openSet.isEmpty())
		{
			Vector current = openSet.first();
			
			for(Vector test : openSet)
			{
				Double f = fScore.get(test); 
				if(f != null && f < fScore.get(current))
					current = test;
			}
			
			if(current.equals(end))
				return getReturn(cameFrom, current);
			
			openSet.remove(current);
			closedSet.add(current);
			
			java.util.List<Vector> neighbor = desc.getNeighbor(current);
			
			for(int i = 0 ; i < neighbor.size() ; i++)
			{
				Vector nei = neighbor.get(i); 
				if(!closedSet.contains(nei))
				{
					openSet.add(nei);
					
					double tenative = gScore.get(current) + current.skew(nei);
					
					Double g = gScore.get(nei);
					g = g == null? Triangle.LARGE_VALUE : g;
					if(tenative < g)
					{
						cameFrom.put(nei, current);
						gScore.put(nei, tenative);
						fScore.put(nei, g + heuristic(nei, end));
					}
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

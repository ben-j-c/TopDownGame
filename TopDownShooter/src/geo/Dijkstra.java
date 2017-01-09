package geo;

/**
 * 
 * @author Ben
 * 
 */
public final class Dijkstra
{	
	/**
	 * A description of a graph (A set of Vectors and a set of Edges [a unordered pair of Vectors])
	 * @author Ben
	 *
	 */
	public static class Description
	{
		private java.util.TreeSet<Vector> nodes = new java.util.TreeSet<Vector>();
		private java.util.TreeSet<Edge> edges = new java.util.TreeSet<Edge>();
		
		/**
		 * Do nothing else
		 */
		public Description()
		{
			
		}
		/**
		 * Make a description based off of d, but with more nodes and more edges
		 * @param d , the base description
		 * @param nodes , extra nodes to add
		 * @param edges , extra edges to add
		 */
		public Description(Description d, java.util.ArrayList<Vector> nodes, java.util.ArrayList<Edge> edges)
		{
			this.nodes.addAll(d.getNodes());
			this.nodes.addAll(nodes);
			
			this.edges.addAll(d.getEdges());
			this.edges.addAll(edges);
		}
		/**
		 * 
		 * @param node a node in the description
		 * @return the neighbors that this node has
		 */
		public java.util.ArrayList<Vector> getNeighbor(Vector node)
		{
			java.util.ArrayList<Vector> returner = new java.util.ArrayList<Vector>();
			
			for(Edge e : getEdges())
			{
				Vector oth = e.getOther(node);
				if(oth != null)
					returner.add(oth);
			}
			
			return returner;
		}
		
		public Vector getSkewed(Vector skew, double skewSqr)
		{
			Vector ret = null;
			for(Vector v: getNodes())
			{
				if(skew.sub(v).magsqr() <= skewSqr)
				{
					ret = v;
					break;
				}
			}
			
			return ret;
		}
		
		public void addEdge(Vector a, Vector b)
		{
			edges.add(new Edge(a,b));
		}
		/**
		 * @return the nodes
		 */
		public java.util.TreeSet<Vector> getNodes()
		{
			return nodes;
		}
		/**
		 * @return the edges
		 */
		public java.util.TreeSet<Edge> getEdges()
		{
			return edges;
		}
		public void addNode(Vector node)
		{
			nodes.add(node);
			
		}
		
		@Override
		public String toString()
		{
			return "Nodes: " + nodes + "\nEdges: " + edges;
		}
		
	}
	public static class Edge implements Comparable<Edge>
	{
		public Vector a;
		public Vector	b;
		
		public Edge(Vector a, Vector b)
		{
			this.a = a;
			this.b = b;
		}
		
		public Vector getOther(Vector v)
		{
			if(a == v)
				return b;
			else if(b == v)
				return a;
			else
				return null;
		}

		@Override
		public int compareTo(Edge e)
		{
			if(e == this)
				return 0;
			else if(e.a.sub(b).magsqr() < a.sub(b).magsqr())
				return -1;
			else
				return 1;
		}
		
		@Override
		public String toString()
		{
			return "{ " + a + ", " + b + "}";
		}
	}
	
	
	/**
	 * 
	 * @param start the point of the first node
	 * @param end the point of the last node
	 * @param desc a description of a graph
	 * @return a list of the points in reverse order that they should be visited
	 */
	public static java.util.ArrayList<Vector> getShortestPath(Vector start, Vector end, Description desc)
	{
		java.util.ArrayList<Vector> nodes = new java.util.ArrayList<Vector>();
		
		nodes.addAll(desc.getNodes());
		java.util.ArrayList<Vector> path = new java.util.ArrayList<Vector>();
		java.util.HashMap<Vector, Double> dist = new java.util.HashMap<Vector, Double>();
		java.util.HashMap<Vector, Vector> prev = new java.util.HashMap<Vector, Vector>();
		for(Vector v : nodes)
		{
			dist.put(v, 1234567890.0);
			prev.put(v, null);
		}
		dist.put(start, 0.0);
		
		while(!nodes.isEmpty())
		{
			Vector u;
			{
				double low = 1234567890.0;
				Vector lowv = null;
				for(Vector temp : nodes)
				{
					if(dist.get(temp) < low)
					{
						lowv = temp;
						low = dist.get(temp);
					}
				}
				u = lowv;
			}
			
			java.util.List<Vector> neighbor = desc.getNeighbor(u);
			
			nodes.remove(u);
			
			for(Vector v : neighbor)
			{
				double alt = dist.get(u) + v.skew(u);
				
				if(alt < dist.get(v))
				{
					dist.put(v, alt);
					prev.put(v, u);
				}
				
			}
		}
		
		Vector next = end;
		while(next != null)
		{
			path.add(next);
			next = prev.get(next);
		}
		
		return path;
		
	}
	
}

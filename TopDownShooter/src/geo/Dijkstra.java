package geo;

import util.dstruct.Queue;

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
			for(Vector v : d.nodes)
			{
				this.nodes.add(v);
			}
			for(Vector v : nodes)
			{
				this.nodes.add(v);
			}
			
			for(Edge e : d.edges)
			{
				this.edges.add(e);
			}
			for(Edge e : edges)
			{
				this.edges.add(e);
			}
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
			
			nodes.add(a);
			nodes.add(b);
			
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
		public Vector a, b;
		
		public Edge(Vector a, Vector b)
		{
			this.a = a;
			this.b = b;
		}
		
		public Vector getOther(Vector v)
		{
			if(a.equals(v))
				return b;
			else if(b.equals(v))
				return a;
			else
				return null;
		}
		
		@Override
		public int compareTo(Edge e)
		{
			if(e == this || (a.equals(e.a) && b.equals(e.b)) || (a.equals(e.b) && b.equals(e.a)))
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
	
	static class Node implements Comparable<Node>
	{
		Vector pos;
		Double dist;
		
		public Node(Vector v, double d)
		{
			pos = v;
			dist = d;
		}
		
		@Override
		public int compareTo(Node n)
		{
			return (dist > n.dist)? 1: (dist == n.dist) ? 0:-1;
		}
		
		public boolean equals(Object o)
		{
			Node n = (Node) o;
			
			return pos.equals(n.pos);
		}
		
		public String toString()
		{
			return "||NODE: " + pos + "\t" + dist + "||";
		}
	}
	
	
	
	/**
	 * A (horrid) implementation of Dijkstra's shortest path algorithm with an alteration to use Vectors as nodes, and the distances between them to be the distances. 
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
		Queue<Node> distances = new Queue<Node>();
		java.util.TreeMap<Vector, Vector> prev = new java.util.TreeMap<Vector, Vector>();
		for(Vector v : nodes)
		{
			if(v != start)
			{
				distances.addData(new Node(v, 1234567890.0));
			}
			prev.put(v, null);
		}
		distances.addData(new Node(start, 0.0));
		
		while(distances.getSize() > 0)
		{	
			Node u = null;
			{
				u = distances.removeData();
			}
			
			if(u.pos.equals(end))
				break;
			
			
			java.util.List<Vector> neighbor = desc.getNeighbor(u.pos);
			
			for(Vector v : neighbor)
			{
				Node nodev = distances.getData(new Node(v, 0.0));
				
				if(nodev != null)
				{
					double alt = u.dist + v.skew(u.pos);
					if(alt < nodev.dist)
					{
						distances.removeData(new Node(v, 0.0));
						distances.addData(new Node(v, alt));
						prev.put(v, u.pos);
					}
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
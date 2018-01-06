package geo;

import game.Shoot;
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
		private java.util.HashSet<Vector> nodes = new java.util.HashSet<Vector>();
		private java.util.HashSet<Edge> edges = new java.util.HashSet<Edge>();
		private java.util.HashMap<Edge, Double> cost;
		
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
		 * Calculate the cost between any two nodes and store it in a tree map
		 */
		public void calculateCost(Vector pos)
		{
			 cost = new java.util.HashMap<Edge, Double>(nodes.size());
			
			for(Vector a : nodes)
			{
				java.util.ArrayList<Vector> path = Dijkstra.getShortestPath(a, pos, this);
				double thisCost = 0.0;
				for(int i = 1 ; i < path.size() ; i++)
				{
					thisCost += path.get(i).sub(path.get(i-1)).mag();
				}
				cost.put(new Edge(a,pos), thisCost);
			}
		}
		
		/**
		 * Return the precalculated cost from the tree map
		 * @return
		 */
		public double getCost(Vector a, Vector b)
		{
			try
			{
				return cost.get(new Edge(a,b));
			}
			catch(NullPointerException e)
			{
				System.out.println("\n\n\n" + a);
				System.out.println(b);
				System.out.println(Shoot.getInstance().mw.descWithPlayer);
			}
			
			return Triangle.LARGE_VALUE;
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
		/**
		 * Looks for the first vector that lies within the squared radius, skewSqr, around the vector skew.
		 * @param skew tested vector
		 * @param skewSqr square of the skew required
		 * @return
		 */
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
		
		public void removeEdge(Edge edge)
		{
			edges.remove(edge);
		}
		
		/**
		 * @return the nodes
		 */
		public java.util.HashSet<Vector> getNodes()
		{
			return nodes;
		}
		/**
		 * @return the edges
		 */
		public java.util.HashSet<Edge> getEdges()
		{
			return edges;
		}
		public void addNode(Vector node)
		{
			nodes.add(node);
		}
		
		public void removeNode(Vector node)
		{
			java.util.ArrayList<Vector> neighbor = this.getNeighbor(node);
			
			for(Vector other: neighbor)
			{
				this.removeEdge(new Edge(node, other));
			}
			
			nodes.remove(node);
		}
		
		@Override
		public String toString()
		{
			return "Nodes: " + nodes + "\nEdges: " + edges;
		}
		
		public Edge getSkewedEdge(Vector testPos, double skew)
		{
			for(Edge e : edges)
			{
				if(testPos.isLineBounded(e.a, e.b, skew))
					return e;
			}
			return null;
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
		public boolean equals(Object o)
		{
			if(o instanceof Edge)
			{
				Edge e = (Edge) o;
				return (e.a.equals(this.a)&&e.b.equals(this.b) || e.b.equals(this.a)&&e.a.equals(this.b));
			}
						
			return false;
		}
		
		@Override
		public String toString()
		{
			return "{ " + a + ", " + b + "}";
		}
		
		@Override
		public int hashCode()
		{
			return Double.hashCode(a.x + a.y + b.x + b.y); 
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
package geo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import geo.Triangle.BlockingVector;


/**
 * A descrition of a triangle, with functions to manipulate it
 * @author Ben
 *
 */
public class Triangle implements Comparable<Triangle>
{
	public static final double LARGE_VALUE = 133769.0;
	public static final double DEFAULT_ERROR = 0.000001f; 
	
	public static class BlockingVector
	{
		public Vector block = null;
		public double t = LARGE_VALUE;
		
		public BlockingVector()
		{
			
		}
		
		public BlockingVector(Vector v, double w)
		{
			block = v.copy();
			t = w;
		}
	}
	public Vector a;
	public Vector b;
	public Vector c;
	
	public Vector ab;
	public Vector bc;
	public Vector ca;
	
	
	private boolean correct_edges = false;
	
	public Triangle()
	{
		
	}
	public Triangle(Vector a, Vector b, Vector c)
	{
		this.a = new Vector(a);
		this.b = new Vector(b);
		this.c = new Vector(c);
		
		edgeCalc();
		
		
		Vector t = a.add(b);
		t.addset(c);
		t.scaleset(0.333333333);
		
		
		double cross = ab.cross(t.sub(a));
		
		if( cross < 0)
		{	
			Vector temp = this.a;
			this.a = this.b;
			this.b = temp;
			
		}
		
		edgeCalc();
		
		
		
	}
	/**
	 * Recalculate the edges of the triangle
	 */
	public void edgeCalc()
	{
		ab =  new Vector(this.b.x - this.a.x, this.b.y - this.a.y);
		bc = new Vector(this.c.x - this.b.x, this.c.y - this.b.y);
		ca = new Vector(this.a.x - this.c.x, this.a.y - this.c.y);
		
		
		;correct_edges = true;
	}
	/**
	 * Checks if the point t is within the triangle
	 * @param t The point in question
	 * @return true if the point is within the triangle
	 */
	public boolean contains(Vector t)
	{
		Vector at = new Vector(t.x - this.a.x, t.y - this.a.y);
		Vector bt = new Vector(t.x - this.b.x, t.y - this.b.y);
		Vector ct = new Vector(t.x - this.c.x, t.y - this.c.y);
		
		if(!correct_edges)
			edgeCalc();
		
		double a = ab.cross(at);
		double b = bc.cross(bt);
		double c = ca.cross(ct);
		
		if(a > 0 && b > 0 && c > 0)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Works the same as the other contains(Vector), but useful for when checking edge points with roundoff error
	 * @param t point you are checking
	 * @param error Some small delta such that the test point can be slightly outside the triangle
	 * @return true if t is within the triangle to a given error
	 */
	public boolean contains(Vector t, double error)
	{
		Vector at = new Vector(t.x - this.a.x, t.y - this.a.y);
		Vector bt = new Vector(t.x - this.b.x, t.y - this.b.y);
		Vector ct = new Vector(t.x - this.c.x, t.y - this.c.y);
		
		if(!correct_edges)
			edgeCalc();
		
		double a = Math.abs(ab.cross(at));
		double b = Math.abs(bc.cross(bt));
		double c = Math.abs(ca.cross(ct));
		
		if(Math.abs(a + b + c - ab.cross(bc)) <= error  )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param t A point to test
	 * @return If the point is outside the triangle, the return is null.  Otherwise the direction vector of the closest edge.
	 */
	public Vector edgeIsBlocking(Vector t)
	{
		if(!correct_edges)
			edgeCalc();
		
		Vector at = new Vector(t.x - a.x, t.y - a.y);
		double al = ab.cross(at);
		if(al < 0)
			return null;
		
		Vector bt = new Vector(t.x - b.x, t.y - b.y);
		double be = bc.cross(bt);
		if(be < 0)
			return null;
		
		
		Vector ct = new Vector(t.x - c.x, t.y - c.y);
		double ga = ca.cross(ct);
		if(ga < 0)
			return null;
		
		al /= ab.mag();
		be /= bc.mag();
		ga /= ca.mag();
		
		if(al <= be && al <= ga)
		{
			return ab.copy();
		}
		else if(be <= ga)
		{
			return bc.copy();
		}
		else
		{
			return ca.copy();
		}
	}
	
	public boolean isFlat()
	{
		return a.equals(b) || a.equals(c) || b.equals(c);
	}
	
	public double area()
	{
		if(c == null)
			System.out.println("Broken");
		double x1 = b.x - a.x;
		double x2 = c.x - a.x;
		
		double y1 = b.y - a.y;
		double y2 = c.y - a.y;
		
		return Math.abs(x1*y2 - y1*x2);
	}
	/**
	 * Checks to see in a point is within the margin of error(skew)
	 * @param v point
	 * @param skew margin of error
	 * @return null if the triangle is not skewed from the point, otherwise any vertex that is within the margin of error
	 */
	public Vector skew(Vector v, double skew)
	{
		if(a.skew(v) < skew)
		{
			return a;
		}
		else if(b.skew(v) < skew)
		{
			return b;
		}
		else if(c.skew(v) < skew)
		{
			return c;
		}
		
		return null;
	}
	/**
	 * Checks if the line defined by la->lb intersects the triangle
	 * @param la Point a on a line
	 * @param lb Point b on a line
	 * @return The smallest percent from a to b that intersects the triangle
	 */
	public double intersectsJustT(Vector la, Vector lb)
	{	
		
		if(!correct_edges)
			edgeCalc();
		
		double lowt = LARGE_VALUE;
		
		{
			//t = -abXla/abXlalb
			
			double t = (ab.cross(a)-ab.cross(la))/ab.cross(lb.sub(la));
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < lowt && contains(r, DEFAULT_ERROR))
				lowt = t;
		}
		{
			//t = (abXa-abXla)/abXlalb
			
			double t = (bc.cross(b)-bc.cross(la))/bc.cross(lb.sub(la));
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < lowt && contains(r, DEFAULT_ERROR))
				lowt = t;
		}
		{
			//t = -abXla/abXlalb
			
			double t = (ca.cross(c)-ca.cross(la))/ca.cross(lb.sub(la));
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < lowt && contains(r, DEFAULT_ERROR))
				lowt = t;
		}
		
		return lowt;
	}
	
	public BlockingVector intersects(Vector la, Vector lb)
	{	
		
		if(!correct_edges)
			edgeCalc();
		
		
		BlockingVector lowt = new BlockingVector();
		
		{
			//t = (abXa-abXla)/abXlalb
			
			//double t = (ab.cross(a)-ab.cross(la))/ab.cross(lb.sub(la));
			double t = Vector.lineSegIntersectLine(a, b, ab, la, lb);
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < lowt.t && contains(r, DEFAULT_ERROR))
			{
				lowt.t = t;
				
				lowt.block = new Vector(ab);
			}
		}
		{
			//t = (abXa-abXla)/abXlalb
			
			//double t = (bc.cross(b)-bc.cross(la))/bc.cross(lb.sub(la));
			double t = Vector.lineSegIntersectLine(b, c, bc, la, lb);
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < lowt.t && contains(r, DEFAULT_ERROR))
			{
				lowt.t = t;
				
				lowt.block = new Vector(bc);
			}
		}
		{
			//t = (abXa-abXla)/abXlalb
			
			//double t = (ca.cross(c)-ca.cross(la))/ca.cross(lb.sub(la));
			double t = Vector.lineSegIntersectLine(c, a, ca, la, lb);
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < lowt.t && contains(r, DEFAULT_ERROR))
			{
				lowt.t = t;
				
				lowt.block = new Vector(ca);
			}
		}
		
		return lowt;
	}
	
	/**
	 * Checks if the line intersects this triangle
	 * @param la point A on the line AB
	 * @param lb point B on the line AB
	 * @return true if the line intersects with the edges of this triangle in between A and B
	 */
	public boolean isIntersecting(Vector la, Vector lb, double skew)
	{
		if(!correct_edges)
			edgeCalc();
		
		{
			//t = (abXa-abXla)/abXlalb
			
			double t = (ab.cross(a)-ab.cross(la))/ab.cross(lb.sub(la));
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < 1 && contains(r, skew))
			{
				return true;
			}
		}
		{
			//t = (abXa-abXla)/abXlalb
			
			double t = (bc.cross(b)-bc.cross(la))/bc.cross(lb.sub(la));
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < 1 && contains(r, skew))
			{
				return true;
			}
		}
		{
			//t = (abXa-abXla)/abXlalb
			
			double t = (ca.cross(c)-ca.cross(la))/ca.cross(lb.sub(la));
			
			Vector r = la.add(lb.sub(la).scale(t));
			
			if( t > 0 && t < 1 && contains(r, skew))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param v point
	 * @return The closest distance from the point to each vertex
	 */
	public double closest(Vector v)
	{
		double bv = b.sub(v).magsqr();
		double av = a.sub(v).magsqr();
		double cv = c.sub(v).magsqr();
		
		return av<bv?av<cv?av:cv:bv<cv?bv:cv;
	}
	
	/**
	 * 
	 * @param la point a on a line
	 * @param lb point b on a line
	 * @param geo Map geometry
	 * @return The lowest percentage from a to b that intersects with geo
	 */
	public static double calcIntersectJustT(Vector la , Vector lb, Collection<Triangle> geo)
	{
		
		double lowt = LARGE_VALUE;
		
		for(Triangle tri : geo)
		{
			double t = tri.intersects(la, lb).t;
			
			if(t < lowt)
				lowt = t;
		}
		
		return lowt;
	}
	
	/**
	 * 
	 * @param la
	 * @param lb
	 * @param geo
	 * @return A object describing a vector blocking a line from a to b
	 */
	public static BlockingVector calcIntersect(Vector la , Vector lb, Collection<Triangle> geo)
	{
		
		BlockingVector lowt = new BlockingVector();
		
		for(Triangle tri : geo)
		{
			BlockingVector rv = tri.intersects(la, lb);
			
			if(rv.t < lowt.t)
			{
				lowt = rv;
			}
		}
		
		return lowt;
	}
	
	public static boolean tooClose(Vector v, double skew, Collection<Triangle> geo)
	{
		for(Triangle tri : geo)
		{
			if(tri.contains(v, skew))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean correctEdges()
	{
		return correct_edges;
	}
	
	public String toString()
	{
		return a + " " + b + " " + c;
	}
	@Override
	public int compareTo(Triangle t)
	{
		if(a.equals(t.a) && b.equals(t.b) && c.equals(t.c))
			return 0;
		else if(a.x + b.x + c.x < t.a.x + t.b.x + t.c.x)
			return -1;
		else
			return 1;
	}
	
	public static boolean clearline(Vector pos, Vector v, TreeSet<Triangle> geo)
	{
		for(Triangle t: geo)
		{
			if(t.isIntersecting(pos, v, Triangle.DEFAULT_ERROR))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Method returns a Vector as close to pos + delta without being inside geo 
	 * @param pos initial point
	 * @param delta a direction vector
	 * @param geo a set of the map geometry
	 * @return a vector as close to pos + delta
	 */
	public static Vector findClosestPos(Vector pos, Vector delta, TreeSet<Triangle> geo)
	{
		Vector nl = pos.add(delta);
		
		BlockingVector block = Triangle.calcIntersect(pos, nl, geo);
		if(block.block == null || block.t > 1)
		{
			return pos.add(delta);
		}
		else
		{	
			//Keep projecting onto blocking vector until you are no longer being blocked
			do
			{
				nl = pos.add(nl.sub(pos).projectOnto(block.block));
				block = Triangle.calcIntersect(pos, nl, geo);				
			}while(block.block != null && block.t < 1);
			return nl;
		}
	}
}
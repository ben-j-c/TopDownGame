package geo;
public class Vector implements Comparable<Vector>
{
	public double x;
	public double y;
	
	/**
	 * Construct vector from values given
	 * @param x the x-component 
	 * @param y the y-component
	 */
	public Vector(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	/**
	 * Creates the zero vector
	 */
	public Vector()
	{
		this.x = 0;
		this.y = 0;
	}
	/**
	 * Create a new vector with the coordinates of another
	 * @param a
	 */
	public Vector(Vector a)
	{
		this.x = a.x;
		this.y = a.y;
	}
	/**
	 * Construct a new Vector based on a String.  Assumes a space in between the two co-ordinates.  Should probably not use
	 * @param s
	 */
	Vector(String s)
	{
		s = s.trim();
		String[] xy = s.split(" ");
		x = Double.parseDouble(xy[0]);
		y = Double.parseDouble(xy[1]);
		
	}
	/**
	 * Set this vector to the specified x and y
	 * @param x the x-component
	 * @param y the y-component
	 */
	public void set(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Set this vector to be the same as b
	 * @param b
	 */
	public void set(Vector b)
	{
		this.x = b.x;
		this.y = b.y;
	}
	
	/**
	 * Compute the cross-product between this and the vector b.  Note that the cross-product of 2-D vectors produce a vector only in the z direction, so the z-component is sufficient in representing the resultant vector. 
	 * @param b
	 * @return the z-component of the cross product
	 */
	public double cross(Vector b)
	{
		return this.x * b.y - this.y*b.x;
	}
	
	/**
	 * Compute the dot product
	 * @param b a vector to be used in the computation
	 * @return the dot(this, b)
	 */
	public double dot(Vector b)
	{
		return this.x*b.x + this.y*b.y;
	}
	
	/**
	 * Get the projection of this onto b
	 * @param b
	 * @return a new vector equal to the projection of this onto b
	 */
	public Vector projectOnto(Vector b)
	{
		return b.scale((this.dot(b)/b.magsqr()));
	}
	
	/**
	 * Get the cos of the angle between this and b
	 * @param b
	 * @return the angle between this and b
	 */
	public double cos(Vector b)
	{
		return this.dot(b)/(this.mag()*b.mag());
	}
	
	/**
	 * Get a vector equal to this + b 
	 * @param b
	 * @return
	 */
	public Vector add(Vector b)
	{
		Vector v = new Vector(this);
		v.x += b.x;
		v.y += b.y;
		return v;
	}
	
	/**
	 * Get a vector equal to this - b
	 * @param b
	 * @return the result of this - b
	 */
	public Vector sub(Vector b)
	{
		Vector v = new Vector(this);
		v.x -= b.x;
		v.y -= b.y;
		return v;
	}
	
	/**
	 * Get a vector with the same direction just with a scaled magnitude
	 * @param scale the scaling factor
	 * @return a new scaled vector
	 */
	public Vector scale(double scale)
	{
		Vector v = new Vector(this);
		v.x *= scale;
		v.y *= scale;
		return v;
	}
	
	/**
	 * Get the magnitude of this vector
	 * @return the magnitude of this vector
	 */
	public double mag()
	{
		return Math.sqrt(x*x + y*y);
	}
	
	/**
	 * Get the magnitude of this vector squared
	 * @return the magnitude of the vector squared
	 */
	public double magsqr()
	{
		return x*x + y*y;
	}
	
	/**
	 * Get a unit vector with the same direction as this
	 * @return the unit vector with the same direction as this
	 */
	public Vector unit()
	{
		return this.scale(1.0/this.mag());
	}
	
	/**
	 * this += b
	 * @param b
	 */
	public void addset(Vector b)
	{
		this.x += b.x;
		this.y += b.y;
	}
	
	/**
	 * this *= b
	 * @param b
	 */
	public void scaleset(double b)
	{
		this.x *= b;
		this.y *= b;
	}
	
	/**
	 * this -= b
	 * @param b
	 */
	public void subset(Vector b)
	{
		this.x -= b.x;
		this.y -= b.y;
	}
	
	/**
	 * Unitizes this vector
	 * @return the now unitized vector 
	 */
	public Vector unitize()
	{
		if(x != 0 || y != 0)
		{
			this.scaleset(1.0/Math.sqrt(x*x + y*y));
		}
		
		return this;
	}
	public double skew(Vector b)
	{
		return this.sub(b).mag();
	}
	
	/**
	 * Finds the smallest distance from this vector to AB
	 * @param a point A on line AB
	 * @param b point B on line AB
	 * @return the distance between this vector and AB
	 */
	public double distance(Vector a, Vector b)
	{
		Vector ab = b.sub(a);
		Vector at = this.sub(a);
		
		return Math.abs(at.projectOnto(ab).cross(at))/at.projectOnto(ab).mag();
	}
	
	/**
	 * Checks to see if this vector is within skew, R, of the line AB, and inside the region between A and B. 
	 * @param a point A on the line AB
	 * @param b point B on the line AB
	 * @param skew R. The max distance this can be from AB and still return true
	 * @return true if this lies within R units of the line AB and sits between A and B
	 */
	public boolean isLineBounded(Vector a, Vector b, double skew)
	{
		double abCos = this.sub(a).cos(b.sub(a)); 
		double baCos = this.sub(b).cos(a.sub(b));
		return (this.distance(a, b) < skew //if the testPos is within skew of the line
				&& abCos >= 0   //and is not behind AB
				&& baCos >= 0); //and is not behind BA 
	}
	
	/**
	 * Get a copy of this Vector
	 * @return A copy of this vector
	 */
	public Vector copy()
	{
		return new Vector(this.x, this.y);
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
	
	@Override
	public int compareTo(Vector o)
	{
		if(this == o || (this.x == o.x && this.y == o.y))
			return 0;
		else if(this.x < o.x)
			return -1;
		else
			return 1;
	}
	
	@Override
	public boolean equals(Object o)
	{
		Vector v = (Vector) o;
		if(this == v || (this.x == v.x && this.y == v.y))
			return true;
		else
			return false;
					
	}
	public Vector clamp(double max)
	{
		if(max*max <= this.magsqr())
		{
			return this.unit().scale(max);
		}
		
		return this.copy();
	}
	public  Vector rotate(double theta)
	{
		return new Vector(
				x*Math.cos(theta) - y*Math.sin(theta),
				y*Math.cos(theta) + x*Math.sin(theta));
	}
	
	
	/**
	 * Finds the value t, such that CD*t + C lies on the line AB*s + A.
	 * 
	 * i.e. solves for t in the equation:
	 * 
	 * AB x (CD*t + C) = 0 
	 * @param a
	 * @param b
	 * @param la C
	 * @param lb D
	 * @return t
	 */
	public static double lineSegIntersectLine(Vector a, Vector b, Vector la, Vector lb)
	{	
		Vector ab = b.sub(a);
		double t = (ab.cross(a)-ab.cross(la))/ab.cross(lb.sub(la));
		return t;
	}
	
	/**
	 * Finds the value t, such that CD*t + C lies on the line AB*s + A.
	 * 
	 * i.e. solves for t in the equation:
	 * 
	 * AB x (CD*t + C) = 0 
	 * @param a
	 * @param b
	 * @param ab
	 * @param la C
	 * @param lb D
	 * @return t
	 */
	public static double lineSegIntersectLine(Vector a, Vector b, Vector ab, Vector la, Vector lb)
	{
		double t = (ab.cross(a)-ab.cross(la))/ab.cross(lb.sub(la));
		return t;
	}
}

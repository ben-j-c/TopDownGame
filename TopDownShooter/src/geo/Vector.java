package geo;
public class Vector implements Comparable<Vector>
{
	public double x;
	public double y;
	public Vector(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	public Vector()
	{
		this.x = 0;
		this.y = 0;
	}
	public Vector(Vector a)
	{
		this.x = a.x;
		this.y = a.y;
	}
	Vector(String s)
	{
		s = s.trim();
		String[] xy = s.split(" ");
		x = Double.parseDouble(xy[0]);
		y = Double.parseDouble(xy[1]);
		
	}
	public void set(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	public void set(Vector b)
	{
		this.x = b.x;
		this.y = b.y;
	}
	public double cross(Vector b)
	{
		return this.x * b.y - this.y*b.x;
	}
	public double dot(Vector b)
	{
		return this.x*b.x + this.y*b.y;
	}
	
	public Vector projectOnto(Vector b)
	{
		return b.scale((this.dot(b)/b.magsqr()));
	}
	
	public double cos(Vector b)
	{
		return this.dot(b)/(this.mag()*b.mag());
	}
	
	public Vector add(Vector b)
	{
		Vector v = new Vector(this);
		v.x += b.x;
		v.y += b.y;
		return v;
	}
	public Vector sub(Vector b)
	{
		Vector v = new Vector(this);
		v.x -= b.x;
		v.y -= b.y;
		return v;
	}
	public Vector scale(double scale)
	{
		Vector v = new Vector(this);
		v.x *= scale;
		v.y *= scale;
		return v;
	}
	public double mag()
	{
		return Math.sqrt(x*x + y*y);
	}
	public double magsqr()
	{
		return x*x + y*y;
	}
	public Vector unit()
	{
		return this.scale(1.0/this.mag());
	}
	public void addset(Vector b)
	{
		this.x += b.x;
		this.y += b.y;
	}
	public void scaleset(double b)
	{
		this.x *= b;
		this.y *= b;
	}
	public void subset(Vector b)
	{
		this.x -= b.x;
		this.y -= b.y;
	}
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
	
	public double distance(Vector a, Vector b)
	{
		Vector ab = b.sub(a);
		Vector at = this.sub(a);
		
		return Math.abs(at.projectOnto(ab).cross(at))/at.projectOnto(ab).mag();
	}
	
	public Vector copy()
	{
		return new Vector(this.x, this.y);
	}
	
	public String toString()
	{
		return "{" + x + ", " + y + "}";
	}
	
	@Override
	public int compareTo(Vector o)
	{
		if(this == o || this.x == o.x && this.y == o.y)
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
		if(this == o || this.x == v.x && this.y == v.y)
			return true;
		else
			return false;
					
	}
}

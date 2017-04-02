package game;

import geo.Triangle;
import geo.Vector;

/**
 * 
 * @author Benjamin Correia
 * 
 * A class describing a generic entity
 * It contains data describing:
 * Position
 * Velocity
 * Hitbox
 * Life and
 * Type of entity
 *
 */
public class Entity
{
	public static final int NULL		= 0;
	public static final int PROJECTILE	= 1;
	public static final int SOLID 		= 2;
	public static final int HITBOX 		= 4;
	public static final int BODY 		= 16;
	public static final int LASER 		= 32;
	
	
	public Vector pos;
	public Vector newPos;
	public Vector headTo;
	public Vector v;
	Triangle collide;
	double life = 10;
	
	double r, g, b;
	
	int TYPE;
	Entity(int t)
	{
		TYPE = t;
		pos = new Vector();
		newPos = new Vector();
		v = new Vector();
		headTo = new Vector();
	}
	
	public Entity(Entity e)
	{
		pos = new Vector(e.pos);
		v = new Vector(e.v);
		collide = e.collide;
		life = e.life;
		TYPE = e.TYPE;
	}
	
	public boolean is(int TYPE)
	{
		return (this.TYPE & TYPE) != 0;
	}
	
	
}

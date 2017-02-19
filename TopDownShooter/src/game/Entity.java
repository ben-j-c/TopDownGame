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
class Entity
{
	public static final int NULL		= 0;
	public static final int PROJECTILE	= 1;
	public static final int SOLID 		= 2;
	public static final int HITBOX 		= 4;
	public static final int BODY 		= 16;
	public static final int LASER 		= 32;
	
	
	Vector pos;
	Vector headTo;
	Vector v;
	Triangle collide;
	double life = 10;
	int TYPE;
	Entity(int t)
	{
		TYPE = t;
		pos = new Vector();
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

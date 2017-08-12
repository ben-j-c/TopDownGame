package game.Entities.Projectiles;

import com.jogamp.opengl.GL2;

import game.Entity;
import game.Shoot;
import game.Entities.Projectile;
import geo.Triangle.BlockingVector;
import geo.Vector;

public class Flame extends Projectile
{
	int frames;
	Vector pos, v;
	double damage;
	
	public Flame(Vector pos, Vector v, int frames, double damage)
	{
		this.pos = pos;
		this.v = v;
		this.frames = frames;
		this.damage = damage;
	}

	@Override
	public void doStep()
	{
		Shoot inst = Shoot.getInstance();
		
		Vector nl = pos.add(v);
		
		BlockingVector bv = inst.calcIntersect(pos, nl); 
		
		Entity e = inst.getAdjacentEnt(pos, Shoot.MONST_SIZE);
		
		if( frames <= 0)
		{
			inst.entityWrapper.removeProjectile(this);
		}
		
		if(e != null)
		{
			e.applyDamage(damage);
		}
		
		if(bv.t <= 1)
		{
			v.set(v.projectOnto(bv.block));
		}
		else
		{
			pos.addset(v);
		}
		
		frames--;
		
	}
	
	@Override
	public void render(GL2 gl)
	{
		gl.glColor3d(Math.random()*0.25 +0.75, Math.random()*0.75, 0);
		
		gl.glBegin(GL2.GL_POINTS);
		gl.glNormal3d(0, 0, 1);
		gl.glVertex3d(pos.x, pos.y, 0);
		gl.glEnd();
	}
	
}
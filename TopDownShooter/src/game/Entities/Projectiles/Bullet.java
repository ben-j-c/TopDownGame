package game.Entities.Projectiles;

import com.jogamp.opengl.GL2;

import game.Entity;
import game.Shoot;
import game.Entities.Projectile;
import geo.Triangle;
import geo.Vector;

public class Bullet  extends Projectile
{
	private int frames = 0;
	private double damage = 0;
	private double size = Shoot.MONST_SIZE;
	
	public Bullet(Vector pos, Vector v, int frames, double damage)
	{
		this.pos = pos;
		this.frames = frames;
		this.v = v;
		this.damage = damage;
	}
	
	@Override
	public void doStep()
	{
		Shoot inst = Shoot.getInstance();
		
		Vector nl = pos.add(v);
		
		double t = inst.calcIntersect(pos, nl).t; 
		
		Entity e = inst.getAdjacentEnt(pos);
		
		if(t > 1 && frames > 0 && e == null)
		{
			pos.addset(v);
		}
		else
		{
			if(e != null)
			{
				e.applyDamage(damage);
			}
			
			inst.entityWrapper.autoRemove(this);
		}
		
		frames--;
	}
	
	@Override
	public void render(GL2 gl)
	{
		gl.glColor3d(Math.random()*0.25 +0.75, Math.random()*0.75, 0);
		
		gl.glBegin(GL2.GL_LINES);
		
		gl.glNormal3d(0, 0, 1);
		gl.glVertex3d(pos.x, pos.y, 0);
		Vector pos2 = pos.add(v);
		gl.glVertex3d(pos2.x, pos2.y, 0);
		gl.glEnd();
	}
}

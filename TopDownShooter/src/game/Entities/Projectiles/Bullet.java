package game.Entities.Projectiles;

import com.jogamp.opengl.GL2;
import game.Entity;
import game.Renderable;
import game.Shoot;
import game.Entities.Monster;
import game.Entities.Projectile;
import geo.Triangle;
import geo.Vector;
import geo.Triangle.BlockingVector;

public class Bullet  extends Projectile implements Renderable
{
	private int frames = 0;
	private double damage = 0;
	private double size = Shoot.MONST_SIZE;
	private double ricochet_prob = 0.5;
	private double ricochet_stddev_angle = 7.5/180.0*Math.PI;;
	private double ricochet_stddev_velocity = 0.25;
	
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
		
		BlockingVector bv = inst.calcIntersect(pos, nl); 
		
		double t = bv.t; 
		
		double cos;
		
		Entity e = inst.getAdjacentEnt(pos);
		
		if(frames <= 0)
		{	
			inst.entityWrapper.autoRemove(this);
		}
		else if(t < 1 && t > 0 && inst.r.nextDouble() < ricochet_prob*(cos =  Math.abs(bv.block.cos(v))))
		{
			double theta = inst.r.nextGaussian()*ricochet_stddev_angle*(1 -cos);
			v.set(
					v.sub(v.projectOnto(bv.block)).scale(-1)
					.add(v.projectOnto(bv.block))
					.scale(Math.min(1, cos + inst.r.nextGaussian()*ricochet_stddev_velocity))
					.rotate(theta));
		}
		else if(e != null && e instanceof Monster)
		{
			e.applyDamage(damage);
			inst.entityWrapper.autoRemove(this);
		}
		else if(t < 1 && t > 0 )
		{
			inst.entityWrapper.autoRemove(this);
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
		
		gl.glBegin(GL2.GL_LINES);
		
		gl.glNormal3d(0, 0, 1);
		gl.glVertex3d(pos.x, pos.y, 0);
		Vector pos2 = pos.add(v);
		gl.glVertex3d(pos2.x, pos2.y, 0);
		gl.glEnd();
	}
}

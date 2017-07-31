package game.Entities.Projectiles;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import game.Entity;
import game.Shoot;
import game.Entities.Projectile;
import geo.Vector;

public class Laser  extends Projectile
{
	int frames;
	
	public Laser(Vector pos, Vector headTo, int frames)
	{
		this.pos = pos;
		this.headTo = headTo;
		this.frames = frames;
	}

	@Override
	public void doStep()
	{
		Shoot inst = Shoot.getInstance();
		
		if(frames <= 0)
		{	
			inst.entityWrapper.removeProjectile(this);
		}
		
		frames--;
	}
	
	@Override
	public void render(GL2 gl)
	{
		gl.glColor3d(Math.random()*0.5 +0.5, Math.random()*0.5, 0);
		gl.glBegin(GL.GL_LINES);
		
		gl.glVertex2d(pos.x, pos.y);
		gl.glVertex2d(headTo.x, headTo.y);
	}
	
}

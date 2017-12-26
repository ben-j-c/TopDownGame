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
			inst.entityWrapper.autoRemove(this);
		}
		
		frames--;
	}
	
	@Override
	public void render(GL2 gl)
	{
		gl.glColor3d(Math.random()*0.25 +0.75, Math.random()*0.75, 0);
		gl.glBegin(GL.GL_LINES);
		
		gl.glNormal3d(0, 0, 1);
		gl.glVertex3d(pos.x, pos.y, 0);
		gl.glVertex3d(headTo.x, headTo.y, 0);
	}
	
}

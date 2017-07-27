package game.Entities;

import com.jogamp.opengl.GL2;

import game.Entity;
import game.Shoot;
import geo.Vector;

public abstract class Projectile extends Entity implements Dynamic
{
	public Projectile(Entity e)
	{
		super(e);
	}
	
	public void render(GL2 gl)
	{
		if(pos == null)
			return;
		
		gl.glColor3d(Shoot.r.nextDouble(), Shoot.r.nextDouble(), Shoot.r.nextDouble());
		
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3d(pos.x, pos.y, 0);
		Vector pos2 = pos.add(v);
		gl.glVertex3d(pos2.x, pos2.y, 0);
		gl.glEnd();
	}
}

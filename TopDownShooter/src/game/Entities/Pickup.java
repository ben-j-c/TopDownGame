package game.Entities;

import com.jogamp.opengl.GL2;

import game.Entity;
import geo.Vector;

public abstract class Pickup extends Entity implements Dynamic
{
	 public Pickup(Entity e)
	{
		super(e);
		// TODO Auto-generated constructor stub
	}

	public Pickup()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	double r, g, b;
	
	public Vector pos;
	
	public abstract void pickedUp();
	public abstract void dropped();
	
	public void render(GL2 gl)
	{
		if(pos == null)
			return;
		
		gl.glColor3d(r, g, b);
		
		gl.glBegin(GL2.GL_POINTS);
		gl.glVertex3d(pos.x, pos.y, 0);
		gl.glEnd();
	}
	
}

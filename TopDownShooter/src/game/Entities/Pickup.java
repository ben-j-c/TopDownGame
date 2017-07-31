package game.Entities;

import com.jogamp.opengl.GL2;

import game.Entity;
import game.Shoot;
import geo.Vector;

public abstract class Pickup extends Entity
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
	
	public abstract void pickedUp();
	public abstract void failedPickUp();
	
	public void render(GL2 gl)
	{
		if(pos == null)
			return;
		
		gl.glColor3d(r, g, b);
		
		gl.glBegin(GL2.GL_POINTS);
		gl.glVertex3d(pos.x, pos.y, 0);
		gl.glEnd();
	}
	
	public final boolean withinRange()
	{
		Shoot inst = Shoot.getInstance();
		
		if(this.pos.skew(inst.getPlayerPos()) < Shoot.MONST_SIZE)
		{
			if(this instanceof InventoryItem)
			{
				if(inst.addToInventory((InventoryItem) this))
				{
					pickedUp();
					return true;
				}
				else
				{
					failedPickUp();
				}
			}
			else
			{
				pickedUp();
				return true;
			}
		}
		
		return false;
	}
}

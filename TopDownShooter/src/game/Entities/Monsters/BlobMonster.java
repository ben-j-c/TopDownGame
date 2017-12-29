package game.Entities.Monsters;

import com.jogamp.opengl.GL2;

import game.Display;
import game.Shoot;
import game.Entities.Body;
import geo.Vector;

/**
 * Generic monster that (when contacted by another BlobMonster) will merge to create a larger and stronger version
 * @author Ben
 *
 */
public class BlobMonster extends GMonster
{
	public static final double MONST_R = 0.2;
	public static final double MONST_G = 0.2;
	public static final double MONST_B = 0.2;
	public static final double MONST_R_OFFSET = 0;
	public static final double MONST_G_OFFSET = 0;
	public static final double MONST_B_OFFSET = 0.4;
	public static final double MONST_SPEED = 0.5;
	public double size = 0.005;
	
	public BlobMonster(Vector spawn)
	{
		super(spawn);
		r = Math.random()*MONST_R+MONST_R_OFFSET;
		g = Math.random()*MONST_G+MONST_G_OFFSET;
		b = Math.random()*MONST_B+MONST_B_OFFSET;
	}
	
	@Override
	public void contact(Body b)
	{
		if(b instanceof BlobMonster && life > 0 && size > 0)
		{
			Shoot inst = Shoot.getInstance();
			
			BlobMonster bm = (BlobMonster) b;
			
			this.life += bm.life;
			this.size = Math.pow(size*size*size + bm.size*bm.size*bm.size, 0.33333333);
			
			((BlobMonster) b).life = 0;
			((BlobMonster) b).size = 0;
			
			inst.entityWrapper.autoRemove((BlobMonster) b);
		}
	}
	
	@Override
	public void render(GL2 gl)
	{
		gl.glColor3d(r,g,b);
		Display.drawCube(gl, size, pos.x, pos.y, 0.01);
	}
	
	@Override
	public double getSpeed()
	{
		return MONST_SPEED;
	}
	
	@Override
	public double getSize()
	{
		return size;
	}
}

package game.Entities.Monsters;

import game.Entity;
import game.Entities.Body;
import game.Entities.Monster;
import game.Entities.Pathable;
import geo.Vector;
/**
 * Generic monster that follows the player and has a random chance to drop some random ammo
 * @author Ben
 *
 */
public class GMonster extends Entity implements Pathable, Body, Monster
{
	public static final double MONST_R = 0.2;
	public static final double MONST_G = 0.2;
	public static final double MONST_B = 0.2;
	public static final double MONST_R_OFFSET = 0;
	public static final double MONST_G_OFFSET = 0.2;
	public static final double MONST_B_OFFSET = 0;
	
	public GMonster(Vector spawn)
	{
		super();
		pos = spawn.copy();
		r = Math.random()*MONST_R+MONST_R_OFFSET;
		g = Math.random()*MONST_G+MONST_G_OFFSET;
		b = Math.random()*MONST_B+MONST_B_OFFSET;
	}
	
	public void contact(Body b)
	{
		
	}
	
	@Override
	public void prePath()
	{
		headTo = null;
	}
	
	@Override
	public void path()
	{
		headTo = getPrecalcMove();
		stepMoveMonster();
	}
	
	@Override
	public void postPath()
	{
		pos = newPos.copy();
	}

	@Override
	public void death()
	{
		
	}

	@Override
	public void spawn()
	{
		
	}
}

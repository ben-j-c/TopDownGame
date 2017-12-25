package game.Entities.Weapons;

import game.Entity;
import game.Shoot;
import game.Entities.Monster;
import game.Entities.Pickup;
import game.Entities.Projectile;
import game.Entities.Weapon;
import game.Entities.Projectiles.Bullet;
import game.Entities.Projectiles.Laser;
import geo.Triangle;
import geo.Vector;

public class LaserRifle extends Pickup implements Weapon
{
	int ammo = 100;
	public static final int FRAMES = 8;
	public static final double DAMAGE = 80;
	
	
	@Override
	public void fire(int x, int y)
	{
		if(ammo > 0)
		{
			Shoot inst = Shoot.getInstance();
			
			
			
			Vector pos = inst.getPlayerPos();
			Vector headTo = inst.translateToReal(x, y);//for laser, the velocity is actually just another point
			double t = inst.calcIntersect(pos, headTo).t; // find the closest intersection of pos -> v
			headTo.set(pos.add(headTo.sub(pos).scale(t))); //v := (v-pos)*t + pos
			
			Laser round = new Laser(pos, headTo, FRAMES);
			
			for(Entity e : inst.entityWrapper.ents)
			{
				if(e instanceof Monster)
				{
					double cos = e.pos.sub(pos).cos(headTo.sub(pos));
					
					Vector ab = headTo.sub(pos);
					
					if(e.pos.distance(pos, headTo) < Shoot.MONST_SIZE && cos >= 0  && e.pos.sub(pos).projectOnto(ab).magsqr() <= ab.magsqr())
					{
						e.applyDamage(DAMAGE);
					}
				}
			}
			
			inst.entityWrapper.addProjectile((Projectile) round);
			ammo --;
		}
		
	}

	@Override
	public void altFire(int x, int y)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pickedUp()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doStep()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddToInventory()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoveFromInventory()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void failedPickUp()
	{
		Shoot inst = Shoot.getInstance();
		
		Rifle held = (Rifle) inst.getInventoryItem(Rifle.class);
		
		held.ammo += ammo;
		
	}
	
}

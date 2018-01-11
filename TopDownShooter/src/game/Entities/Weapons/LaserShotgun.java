package game.Entities.Weapons;

import game.Entity;
import game.Shoot;
import game.Entities.Monster;
import game.Entities.Pickup;
import game.Entities.Projectile;
import game.Entities.Weapon;
import game.Entities.Projectiles.Laser;
import geo.Vector;

public class LaserShotgun extends Pickup implements Weapon
{
	int ammo = 100;
	public static final int FRAMES = 8;
	public static final int ROUNDS = 8;
	public static final double DAMAGE = 20;
	public static final double ACCURACY = 5.0/180.0*Math.PI; //5 degrees converted into radians
	
	@Override
	public void doStep()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void fire(int x, int y)
	{
		if(ammo > 0)
		{
			Shoot inst = Shoot.getInstance();
			
			Vector pos = inst.getPlayerPos();
			Vector aim = inst.translateToReal(x, y);//for laser, the velocity is actually just another point
			Vector bearing = aim.sub(pos).unitize();
			
			for(int i = 0 ; i < ROUNDS ; i++)
			{
				double theta = inst.r.nextGaussian()*ACCURACY;
				Vector randomBearing = bearing.rotate(theta);
				
				double t = inst.calcIntersect(pos, randomBearing.add(pos)).t; // find the closest intersection of pos -> randomBearing
				randomBearing.scaleset(t);
				
				Laser round = new Laser(pos, pos.add(randomBearing), FRAMES);
				
				for(Entity e : inst.entityWrapper.ents)
				{
					if(e instanceof Monster)
					{
						double cos = e.pos.sub(pos).cos(randomBearing);
						
						if(e.pos.distance(pos, pos.add(randomBearing)) < e.getSize() //if the entity is close enough to the line 
								&& cos >= 0 //and it isn't behind the player  
								&& e.pos.sub(pos).projectOnto(randomBearing).magsqr() <= randomBearing.magsqr()) //and it isn't beyond the end of the line
						{
							e.applyDamage(DAMAGE);
						}
					}
				}
				
				inst.entityWrapper.addProjectile((Projectile) round);
			}
			ammo--;
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
		
		LaserShotgun held = (LaserShotgun) inst.getInventoryItem(LaserShotgun.class);
		
		held.ammo += ammo;
		
	}
	
}

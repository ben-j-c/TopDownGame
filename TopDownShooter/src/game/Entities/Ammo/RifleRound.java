package game.Entities.Ammo;

import game.Shoot;
import game.Entities.Pickup;
import game.Entities.Projectile;
import game.Entities.Weapons.Rifle;
import geo.Vector;

public class RifleRound extends Pickup
{

	Vector v, pos;
	
	@Override
	public void pickedUp()
	{
		Shoot inst = Shoot.getInstance();
		Rifle r = (Rifle) inst.getInventoryItem((Class) Rifle.class);
		
	}

	@Override
	public void failedPickUp()
	{
		// TODO Auto-generated method stub
		
	}
}

package game.Entities.Ammo;

import game.Shoot;
import game.Entities.Pickup;

public class GasTank extends Pickup
{
	@Override
	public void pickedUp()
	{
		Shoot inst = Shoot.getInstance();
		
	}

	@Override
	public void failedPickUp()
	{
		// TODO Auto-generated method stub
		
	}
}

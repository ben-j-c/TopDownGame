package game.Entities.Weapons;

import game.Entities.Weapon;
import game.Entities.Projectiles.Bullet;
import game.Entities.Projectiles.Laser;
import geo.Vector;
import game.Entity;
import game.Shoot;
import game.Entities.Monster;
import game.Entities.Pickup;
import game.Entities.Projectile;

public class Shotgun extends Pickup implements Weapon
{
	int ammo = 100;
	public static final int ROUNDS = 16;
	public static final double DAMAGE = 50;
	public static final double ACCURACY = 7.5/180.0*Math.PI; //7.5 degrees converted into radians
	public static final double GROUP_DEV = Shoot.PLAYER_SPEED*0.5;
	public static final double GROUP_VELOCITY = Shoot.PLAYER_SPEED*2.5;
	public static final int FRAMES = 15;
	public static final int FRAMES_DEV = 5;
	
	
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
			
			Vector aim = inst.translateToReal(x, y);
			Vector pos = inst.getPlayerPos();
			
			for(int i = 0;i < ROUNDS;i++)
			{
				Vector v = aim.sub(pos).unitize();
				double theta = inst.r.nextGaussian()*ACCURACY;
				int frames = (int) Math.round(FRAMES + inst.r.nextGaussian()*FRAMES_DEV);
				double velocity = GROUP_VELOCITY + inst.r.nextGaussian()*GROUP_DEV;
				
				v.rotateset(theta);
				v.scaleset(velocity);
				
				Bullet round = new Bullet(pos.add(v), v, frames, DAMAGE);
				
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
		// TODO Auto-generated method stub
		
	}
	
}

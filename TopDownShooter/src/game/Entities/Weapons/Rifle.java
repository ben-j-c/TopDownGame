package game.Entities.Weapons;

import geo.Vector;
import game.Renderable;
import game.Shoot;
import game.Entities.InventoryItem;
import game.Entities.Pickup;
import game.Entities.Projectile;
import game.Entities.Weapon;
import game.Entities.Projectiles.Bullet;

public class Rifle extends Pickup implements Weapon, Renderable
{
	int ammo = 100;
	int offset = 0;
	public static final double M_VELOCITY = Shoot.PLAYER_SPEED*5;
	public static final int FRAMES = 100;
	public static final double DAMAGE = 50;
	public static final double ACCURACY = Math.PI/100;//radians
	
	
	
	
	@Override
	public void pickedUp()
	{
		
	}
	
	@Override
	public void failedPickUp()
	{
		Shoot inst = Shoot.getInstance();
		
		Rifle held = (Rifle) inst.getInventoryItem(Rifle.class);
		
		held.ammo += ammo;
		
	}
	
	@Override
	public void fire(int x, int y)
	{
		if(ammo > 0)
		{
			Shoot inst = Shoot.getInstance();
			
			Vector aim = inst.translateToReal(x, y);
			Vector pos = inst.getPlayerPos();
			Vector v = aim.sub(pos).unitize();
			double theta = inst.r.nextGaussian()*ACCURACY;
			
			v.rotate(theta);
			v.scaleset(M_VELOCITY);
			
			Bullet round = new Bullet(pos, v, FRAMES, DAMAGE);
			
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
	public void doStep()
	{	
		Shoot inst = Shoot.getInstance();
		
		//System.out.println("Auto step");
		
		if(inst.keys.MOUSE_LEFT && (inst.getGameTime() - offset)%8 == 0)
		{
			int x = inst.getMouseX();
			int y = inst.getMouseY();
			
			fire(x, y);
		}
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
	
	
	
}

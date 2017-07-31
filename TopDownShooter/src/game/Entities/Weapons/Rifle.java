package game.Entities.Weapons;

import geo.Vector;

import game.Shoot;
import game.Entities.InventoryItem;
import game.Entities.Pickup;
import game.Entities.Projectile;
import game.Entities.Weapon;
import game.Entities.Projectiles.Bullet;

public class Rifle extends Pickup implements Weapon
{
	int ammo = 500;
	int offset = 0;
	public static final double M_VELOCITY = Shoot.PLAYER_SPEED*5;
	public static final int FRAMES = 100;
	public static final double DAMAGE = 50;
	
	
	
	
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
			double theta = (inst.r.nextDouble()*1 - 0.5)*Math.PI/180;
			
			double vx = v.x; double vy = v.y;
			v.x = Math.cos(theta)*vx - Math.sin(theta)*vy;
			v.y = Math.sin(theta)*vx + Math.cos(theta)*vy;
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

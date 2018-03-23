package game.Entities.Weapons;

import game.Renderable;
import game.Shoot;
import game.Entities.Pickup;
import game.Entities.Projectile;
import game.Entities.Weapon;
import game.Entities.Projectiles.Bullet;
import game.Entities.Projectiles.Flame;
import geo.Vector;

public class Flamethrower extends Pickup implements Weapon, Renderable
{
	int ammo = 10000;
	int offset = 0;
	public static final double M_VELOCITY = Shoot.PLAYER_SPEED;
	public static final double V_VAR = Shoot.PLAYER_SPEED*0.25;
	public static final int ROUNDS = 10;
	public static final int FRAMES = 100;
	public static final double DAMAGE = 10;
	public static final double ACCURACY = 5.0/180.0*Math.PI; //5 degrees converted into radians
	
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
	public void fire(int x, int y)
	{
		int i = ROUNDS;
		while (ammo > 0 && i > 0)
		{
			Shoot inst = Shoot.getInstance();
			
			Vector aim = inst.translateToReal(x, y);
			Vector pos = inst.getPlayerPos();
			Vector v = aim.sub(pos).unitize();
			double theta = (inst.r.nextGaussian() - 0.5)*ACCURACY;
			
			double vx = v.x; double vy = v.y;
			v.x = Math.cos(theta)*vx - Math.sin(theta)*vy;
			v.y = Math.sin(theta)*vx + Math.cos(theta)*vy;
			v.scaleset(M_VELOCITY + V_VAR*inst.r.nextGaussian());
			v.addset(inst.getPlayerV());
			
			Flame round = new Flame(pos, v, (int) (Shoot.r.nextInt((int) (FRAMES*0.9)) + FRAMES*0.1) , DAMAGE);
			
			inst.entityWrapper.addProjectile((Projectile) round);
			i--;
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
	public void failedPickUp()
	{
		Shoot inst = Shoot.getInstance();
		
		Flamethrower held = (Flamethrower) inst.getInventoryItem(Flamethrower.class);
		
		held.ammo += ammo;
		
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

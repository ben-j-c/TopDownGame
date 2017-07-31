package game;

import java.util.ArrayList;

import game.Entities.Dynamic;
import game.Entities.Projectile;
import geo.Vector;

public class EntityWrapper
{
	public ArrayList<Entity> ents = new ArrayList<Entity>();
	public ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
	public ArrayList<Dynamic> dynamics = new ArrayList<Dynamic>();
	
	public ArrayList<Entity> toRemove = new ArrayList<Entity>();
	public ArrayList<Projectile> projToRemove = new ArrayList<Projectile>();
	public ArrayList<Dynamic> dynamicToRemove = new ArrayList<Dynamic>();
	
	public void addProjectile(Projectile proj)
	{
		projectiles.add(proj);
	}
	
	public void addEntity(Entity e)
	{
		ents.add(e);
	}
	
	public void addDynamic(Dynamic d)
	{
		dynamics.add(d);
	}
	
	public void removeProjectile(Projectile p)
	{
		projToRemove.add(p);
	}
	
	public void removeEntity(Entity e)
	{
		toRemove.add(e);
	}
	
	public void removeDynamic(Dynamic d)
	{
		dynamicToRemove.add(d);
	}
	
	public void removeAllToRemove()
	{
		ents.removeAll(toRemove);
		projectiles.removeAll(projToRemove);
		//dynamics.removeAll(dynamicToRemove);
	}
	
	public void clear()
	{	
		ents.clear();
		projectiles.clear();
		dynamics.clear();
		
		toRemove.clear();
		projToRemove.clear();
		dynamicToRemove.clear();
	}
	
	public ArrayList<Entity> getAdjacentEnts(Vector pos, double size)
	{
		ArrayList<Entity> ret = new ArrayList<Entity>();
		
		for(int i = 0 ; i < ents.size() ; i++)
		{
			Entity e = ents.get(i);
			if(e.pos.skew(pos) < size)
			{
				ret.add(e);
			}
		}
		return ret;
	}
	
	public Entity getAdjacentEnt(Vector pos, double size)
	{	
		for(int i = 0 ; i < ents.size() ; i++)
		{
			Entity e = ents.get(i);
			if(e.pos.skew(pos) < size)
			{
				return e;
			}
		}
		return null;
	}
	
	public ArrayList<Entity> getAdjacentEnts(Vector a, Vector b, double size)
	{
		ArrayList<Entity> ret = new ArrayList<Entity>();
		
		return ret;
	}
	
}

package game;

import java.util.ArrayList;
import java.util.HashSet;

import game.Entities.Dynamic;
import game.Entities.Monster;
import game.Entities.Pathable;
import game.Entities.Projectile;
import geo.Vector;

public class EntityWrapper
{
	public HashSet<Entity> ents = new HashSet<Entity>(Shoot.MAX_MONST);
	public HashSet<Projectile> projectiles = new HashSet<Projectile>(Shoot.MAX_MONST);
	public HashSet<Dynamic> dynamics = new HashSet<Dynamic>(Shoot.MAX_MONST);
	public HashSet<Pathable> pathEnts = new HashSet<Pathable>(Shoot.MAX_MONST);
	
	public ArrayList<Entity> toRemove = new ArrayList<Entity>();
	public ArrayList<Projectile> projToRemove = new ArrayList<Projectile>();
	public ArrayList<Dynamic> dynamicToRemove = new ArrayList<Dynamic>();
	public ArrayList<Pathable> pathEntsToRemove = new ArrayList<Pathable>();
	
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
	
	public void autoAdd(Entity e)
	{
		if(e instanceof Projectile)
			projectiles.add((Projectile) e);
		if(e instanceof Dynamic)
			dynamics.add((Dynamic) e);
		if(e instanceof Pathable)
			pathEnts.add((Pathable) e);
		
		ents.add(e);
		
		if(e instanceof Monster)
			((Monster) e).spawn();
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
	
	public void autoRemove(Entity e)
	{
		if(e instanceof Projectile)
		{
			projToRemove.add((Projectile) e);
		}
		if(e instanceof Dynamic)
		{
			dynamicToRemove.add((Dynamic) e);
		}
		if(e instanceof Pathable)
		{
			pathEntsToRemove.add((Pathable) e);
		}
		
		toRemove.add(e);
	}
	
	public void removeAllToRemove()
	{
		ents.removeAll(toRemove);
		projectiles.removeAll(projToRemove);
		dynamics.removeAll(dynamicToRemove);
		pathEnts.removeAll(pathEntsToRemove);
		
		toRemove.clear();
		projToRemove.clear();
		dynamicToRemove.clear();
		pathEntsToRemove.clear();
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
		
		Entity[] temp = (Entity[]) ents.toArray(); 
		
		for(int i = 0 ; i < temp.length ; i++)
		{
			Entity e = temp[i];
			if(e.pos.skew(pos) < size)
			{
				ret.add(e);
			}
		}
		return ret;
	}
	
	public Entity getAdjacentEnt(Vector pos, double size)
	{	
		Entity[] temp = (Entity[]) ents.toArray(); 
		for(int i = 0 ; i < temp.length ; i++)
		{
			Entity e = temp[i];
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

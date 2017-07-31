package game.Entities;

public interface Weapon extends Dynamic, InventoryItem
{
	public void fire(int x, int y);
	public void altFire(int x, int y);
}

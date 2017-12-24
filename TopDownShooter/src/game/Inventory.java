package game;

import game.Entities.InventoryItem;

public class Inventory
{
	InventoryItem[] inv;
	
	private int size, addIndex, selectedIndex;
	
	public Inventory(int size)
	{
		this.size = size;
		this.clear();
	}
	
	public boolean add(InventoryItem e)
	{
		if(size <= addIndex)
			return false;
		
		inv[addIndex++] = e;
		
		
		e.onAddToInventory();
		return true;
	}
	
	public InventoryItem remove(int idx)
	{
		if(idx >= addIndex || idx < 0)
			return null;
		
		
		InventoryItem ret = inv[idx];
		
		for(int i = idx ; i < addIndex ; i++)
		{
			inv[i] = inv[i + 1];
		}
		
		ret.onRemoveFromInventory();
		return ret;
	}
	
	public InventoryItem get(int idx)
	{
		if(idx >= addIndex || idx < 0)
			return null;
		
		return inv[idx];
	}
	
	/**
	 * Get the first occurrence of the class
	 * @param c the class you are looking for
	 * @return the instance of the class you are looking for, or null if it is not found
	 */
	public InventoryItem get(Class<? extends InventoryItem> c)
	{
		for(int i = 0 ; i < addIndex ; i++)
		{
			if(inv[i].getClass().equals(c))
			{
				return inv[i];
			}
		}
		
		return null;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public int getAddIndex()
	{
		return addIndex;
	}
	
	public void clear()
	{
		addIndex = 0;
		
		inv = new InventoryItem[size];
	}
	
	public void setDefaultLoadout()
	{
		clear();
		
		this.add(new game.Entities.Weapons.LaserRifle());
		this.add(new game.Entities.Weapons.LaserShotgun());
		this.add(new game.Entities.Weapons.Shotgun());
		this.add(new game.Entities.Weapons.Flamethrower());
		this.add(new game.Entities.Weapons.Rifle());
	}

	public InventoryItem getSelected()
	{
		return this.get(selectedIndex);
	}
	
	public void nextItem()
	{
		selectedIndex = (selectedIndex + 1)%size;
	}
	
	public void previousItem()
	{
		selectedIndex = ((selectedIndex == 1)? size:selectedIndex) - 1;
	}
}

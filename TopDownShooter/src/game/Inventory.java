package game;

import game.Entities.InventoryItem;

public class Inventory <E extends InventoryItem>
{
	E[] inv;
	
	private int size, addIndex;
	
	public Inventory(int size)
	{
		this.size = size;
		addIndex = 0;
		
		inv = (E[]) new InventoryItem[size];
	}
	
	public boolean add(E e)
	{
		if(size <= addIndex)
			return false;
		
		inv[addIndex++] = e;
		
		
		e.onAddToInventory();
		return true;
	}
	
	public E remove(int idx)
	{
		if(idx >= addIndex || idx < 0)
			return null;
		
		
		E ret = inv[idx];
		
		for(int i = idx ; i < addIndex ; i++)
		{
			inv[i] = inv[i + 1];
		}
		
		ret.onRemoveFromInventory();
		return ret;
	}
	
	public E get(int idx)
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
	public E get(Class<? extends InventoryItem> c)
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
		
		inv = (E[]) new InventoryItem[size];
	}
}

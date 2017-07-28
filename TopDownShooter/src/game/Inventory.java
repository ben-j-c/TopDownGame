package game;

import java.util.ArrayList;

import game.Entities.InventoryItem;

public class Inventory <E extends InventoryItem>
{
	E[] inv;
	
	private int size, addIndex;
	
	public Inventory(int size)
	{
		this.size = size;
		addIndex = 0;
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
}

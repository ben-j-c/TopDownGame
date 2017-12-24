package game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import geo.Map;
import geo.Triangle;
import geo.Vector;

public class ControlMouse implements MouseListener
{
	MapWrapper mw;
	ArrayList<Vector> points;
	Shoot inst;
	KeyList keys;
	
	ControlMouse(Shoot inst)
	{
		this.inst = inst;
		this.mw = inst.mw;
		this.points = inst.points;
		this.keys = inst.keys;
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		if(!inst.GAME_STARTED)
		{
			if(inst.PLACE_GRAPH)
			{
				Vector temp = inst.translateToReal(e.getX(), e.getY());
				
				Vector temp2 = mw.gameMap.desc.getSkewed(temp, Shoot.SNAP_DISTANCE*Shoot.SNAP_DISTANCE);
				if(temp2 == null)
				{
					points.add(temp);
					mw.gameMap.desc.addNode(temp);
				}
				else
				{
					points.add(temp2);
					mw.gameMap.desc.addNode(temp2);
				}
				
				if(points.size() == 2)
				{
					mw.gameMap.desc.addEdge(points.remove(0), points.remove(0));
					points.clear();
				}
			}
			else
			{
				Vector temp = inst.translateToReal(e.getX(), e.getY());
				
				for(Triangle t: mw.gameMap.geo)
				{
					Vector temp1 = t.skew(temp, Shoot.SNAP_DISTANCE);
					if(temp1 != null)
					{
						temp = temp1;
						break;
					}
				}
				
				points.add(temp);
				
				if(points.size()%3 == 0)
				{
					int size = points.size();
					Triangle t = new Triangle(points.get( size - 3 ), points.get( size - 2 ), points.get( size - 1 ) );
					if(!t.isFlat())
					{
						mw.gameMap.geo.add(t);
					}
				}
			} 
		} //!inst.GAME_STARTED
		else
		{
			switch(e.getButton())
			{
				case MouseEvent.BUTTON1:
				{
					inst.fireWeapon(e.getX(), e.getY());
					keys.MOUSE_LEFT = true;
					break;
				}
				case MouseEvent.BUTTON2:
				{
					inst.altFireWeapon(e.getX(), e.getY());
					keys.MOUSE_RIGHT = true;
					keys.MOUSE_X = e.getX();
					keys.MOUSE_Y = e.getY();
					break;
				}
				case MouseEvent.BUTTON3:
				{
					keys.MOUSE_X = e.getX();
					keys.MOUSE_Y = e.getY();
					break;
				}
			}
		}
		
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if(!inst.GAME_STARTED)
		{
			;
		}
		else
		{
			switch(e.getButton())
			{
				case MouseEvent.BUTTON1:
				{
					keys.MOUSE_LEFT = false;
					break;
				}
				case MouseEvent.BUTTON2:
				{
					keys.MOUSE_RIGHT = false;
					break;
				}
				case MouseEvent.BUTTON3:
				{
					break;
				}
			}
		}
	}
}

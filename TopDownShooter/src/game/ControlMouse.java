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
			if(inst.PLACE_GRAPH) //placing nodes
			{
				Vector temp = inst.translateToReal(e.getX(), e.getY());
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					Vector snapVector = mw.gameMap.desc.getSkewed(temp, Shoot.SNAP_DISTANCE*Shoot.SNAP_DISTANCE);
					geo.Dijkstra.Edge snapEdge = mw.gameMap.desc.getSkewedEdge(temp, Shoot.SNAP_DISTANCE);
					
					System.out.println(snapVector);
					System.out.println(snapEdge);
					
					if(snapVector != null)
						System.out.println(mw.gameMap.desc.getNodes().contains(snapVector));
					if(snapEdge != null)
						System.out.println(mw.gameMap.desc.getEdges().contains(snapEdge));
					
					if(e.isAltDown())
					{
						if(snapVector != null)
						{
							mw.gameMap.desc.removeNode(snapVector);
						}
						else if(snapEdge != null)
						{
							mw.gameMap.desc.removeEdge(snapEdge);
						}
						
						points.clear();
					}
					else
					{
						if(snapVector == null)
						{
							points.add(temp);
							mw.gameMap.desc.addNode(temp);
						}
						else
						{
							points.add(snapVector);
							mw.gameMap.desc.addNode(snapVector);
						}
						
						if(points.size() == 2)
						{
							mw.gameMap.desc.addEdge(points.remove(0), points.remove(0));
							points.clear();
						}
					}
				}
				else if(e.getButton() == MouseEvent.BUTTON2)
				{
					
				}
			}// inst.PLACE_GRAPH
			else //placing triangles
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
			}// else placing triangle 
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

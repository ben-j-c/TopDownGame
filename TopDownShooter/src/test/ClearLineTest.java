package test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import game.MapWrapper;
import geo.Triangle;
import geo.Vector;
import geo.Map;
import geo.Map.BSPNode;
import geo.Map.Line;

public class ClearLineTest extends JPanel implements MouseListener
{
	Vector mouse;
	MapWrapper mw = new MapWrapper();
	Vector click;
	
	public static void main(String[] args) throws InterruptedException
	{	
		ClearLineTest clt = new ClearLineTest();
		clt.addMouseListener(clt);
		JFrame jf = new JFrame("ClearLineTest");
		jf.setDefaultCloseOperation(jf.EXIT_ON_CLOSE);
		
		clt.mw.gameMap.opendialog();
		
		synchronized(Map.class)
		{
			while(clt.mw.gameMap.geo.size() == 0)
				Map.class.wait();
		}
		
		
		jf.add(clt);
		jf.setVisible(true);
		jf.pack();
		
		while(true)
		{
			Point mp = clt.getMousePosition();
			
			if(mp != null)
				clt.mouse = clt.translateToReal(mp.x, mp.y);
			clt.repaint();
			Thread.sleep(10);
		}
	}
	
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.GRAY);
		for(Triangle t: mw.gameMap.geo)
		{
			Vector sa = translateToScreen(t.a);
			Vector sb = translateToScreen(t.b);
			Vector sc = translateToScreen(t.c);
			
			int[] x = {(int) sa.x, (int) sb.x, (int) sc.x};
			int[] y = {(int) sa.y, (int) sb.y, (int) sc.y};
			Polygon p = new Polygon(x,y, 3);
			g.fillPolygon(p);
		}
		
		if(click != null)
		{
			if(mw.gameMap.clearLine(mouse, click))
			//if(Triangle.clearline(mouse, click, mw.gameMap.geo))
				g.setColor(Color.GREEN);
			else
				g.setColor(Color.RED);
			Vector a = translateToScreen(mouse);
			Vector b = translateToScreen(click);
			g.drawLine((int)a.x, (int)a.y, (int)b.x, (int)b.y);
		}
		
		this.labelLineSeg(g);
		
	}
	
	public void labelLineSeg(Graphics g)
	{
		ArrayList<Line> draw = mw.gameMap.getAllLines();
		
		for(Line l : draw)
		{
			Vector pos = translateToScreen(l.a.add(l.ab.scale(0.5)));
			Vector a = translateToScreen(l.a);
			Vector b = translateToScreen(l.b);
			g.drawString("id:" + l.getId(), (int)pos.x, (int)pos.y);
			g.drawRect((int) a.x -1, (int) a.y - 1, 2, 2);
			g.drawRect((int) b.x -1, (int) b.y - 1, 2, 2);
		}
	}
	
	ClearLineTest()
	{
		this.setPreferredSize(new java.awt.Dimension(800,800));
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if(e.isAltDown())
			System.out.println(mw.gameMap.clearLine(mouse, click));
		else
			click = translateToReal(e.getX(), e.getY());
		
		
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
	public Vector translateToReal(double x, double y)
	{
		int w = this.getWidth();
		int h = this.getHeight();
		return new Vector(
				(2*x - w)/w, //x
				(2*(h - y) - h)/h //y
				);
	}
	
	public Vector translateToScreen(Vector real)
	{
		int w = this.getWidth();
		int h = this.getHeight();
		
		return new Vector(
				(real.x*w + w)/2,
				h - (real.y*h + h)/2);
	}
}

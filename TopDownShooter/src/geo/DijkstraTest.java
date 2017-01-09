package geo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DijkstraTest extends JPanel implements MouseListener
{
	int size = 12;
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	Dijkstra.Description desc = new Dijkstra.Description();
	
	Vector[] buffer = new Vector[2];
	
	Vector start, end;
	java.util.ArrayList<Vector> path = new java.util.ArrayList<Vector>();
	
	DijkstraTest()
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
		{
			start = new Vector(e.getX(), e.getY());
			for(Vector v : desc.getNodes())
			{
				if(start.skew(v) < size/2)
					start = v;
			}
		}
		else
		{
			buffer[0] = new Vector(e.getX(), e.getY());
			for(Vector v : desc.getNodes())
			{
				if(buffer[0].skew(v) < size/2)
					buffer[0] = v;
			}
			
			System.out.println(buffer[0]);
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		
		if(e.isAltDown())
		{
			end = new Vector(e.getX(), e.getY());
			for(Vector v : desc.getNodes())
			{
				if(end.skew(v) < size/2)
					end = v;
			}
			path = Dijkstra.getShortestPath(start, end, desc);
		}
		else if(e.isShiftDown())
		{
		}
		else
		{
			buffer[1] = new Vector(e.getX(), e.getY());
			for(Vector v : desc.getNodes())
			{
				if(buffer[1].skew(v) < size/2)
					buffer[1] = v;
			}
			
			if(buffer[1] != buffer[0]  && buffer[0].skew(buffer[1]) > size/2)
			{
				desc.getNodes().add(buffer[1]);
				desc.getNodes().add(buffer[0]);
				
				desc.getEdges().add(new Dijkstra.Edge(buffer[0], buffer[1]));
			}
			
			System.out.println(buffer[1]);
		}
		repaint();
		
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.BLACK);
		
		g.fillRect(0, 0, 800, 800);
		
		g.setColor(Color.WHITE);
		for(Vector v : desc.getNodes())
		{	
			g.fillOval((int) v.x - size/2, (int) v.y - size/2, size, size);
		}
		g.setColor(Color.GRAY);
		for(Dijkstra.Edge e : desc.getEdges())
		{	
			g.drawLine((int) e.a.x, (int) e.a.y, (int) e.b.x, (int) e.b.y);
		}
		
		g.setColor(Color.CYAN);
		
		for(int i = 1 ; i < path.size() ; i++)
		{
			g.drawLine((int)path.get(i).x, (int)path.get(i).y, (int)path.get(i - 1).x,(int) path.get(i - 1).y);
		}
	}
	
	public static void main(String... args) throws InterruptedException
	{
		DijkstraTest dt = new DijkstraTest();
		
		JFrame frame = new JFrame("Dijkstra");
		
		frame.add(dt);
		dt.addMouseListener(dt);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}

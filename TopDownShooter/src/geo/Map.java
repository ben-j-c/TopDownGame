package geo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import game.Shoot;

public class Map
{
	public TreeSet<Triangle> geo = new TreeSet<Triangle>();
	private BSPNode head = null;
	public Dijkstra.Description desc = new Dijkstra.Description();
	
	public static class Line
	{
		private int id;
		private static int count = 0;
		public Vector a, b;
		public Vector ab;
		
		Line(Vector a, Vector b)
		{
			synchronized(Line.class)
			{
				id = count++;
			}
			this.a = a;
			this.b = b;
			ab = b.sub(a);
		}

		@Override
		public boolean equals(Object o)
		{
			if(o == this)
				return true;
			else if(o instanceof Line)
			{
				Line l = (Line) o;
				
				return a.equals(l.a) && b.equals(l.b);
			}
			
			return false;
		}
		
		public int getId()
		{
			return id;
		}
	}
	
	public static class BSPNode
	{
		public Line data;
		
		public BSPNode left = null;
		public BSPNode right = null;
		
		BSPNode(Line l)
		{
			data = l;
		}
		
		public static BSPNode add(BSPNode cur, Line l)
		{
			if(cur == null)
				return new BSPNode(l);
			else
			{
				double crossA = cur.data.ab.cross(l.a.sub(cur.data.a));
				double crossB = cur.data.ab.cross(l.b.sub(cur.data.a));
				
				if(Math.signum(crossA) == Math.signum(crossB)
						|| Math.abs(crossA) <= Triangle.DEFAULT_ERROR
						|| Math.abs(crossB) <= Triangle.DEFAULT_ERROR)//A and B are on the same side
				{
					if(crossA + crossB > Triangle.DEFAULT_ERROR) //to the left
						cur.left = add(cur.left, l);
					else
						cur.right = add(cur.right, l);
				}
				else//cur bisects l
				{
					double t = Vector.lineSegIntersectLine(cur.data.a, cur.data.b, cur.data.ab, l.a, l.b);
					
					if(t > 0 && t < 1)
					{
						Vector bisect = l.a.add(l.b.sub(l.a).scale(t));
						Line la = new Line(l.a, bisect);
						Line lb = new Line(bisect, l.b);
						if(crossA > 0)//if a was on the left
						{
							cur.left = add(cur.left, la);
							cur.right = add(cur.right, lb);
						}
						else
						{
							cur.right = add(cur.right, la);
							cur.left = add(cur.left, lb);
						}
					}
				}
			}
			
			return cur;
		}
		
		public String toString()
		{
			StringBuilder sbr = new StringBuilder(50);
			sbr.append(data.id);
			sbr.append(" L: ");
			sbr.append( (left != null)? left.data.id : -1);
			sbr.append(" R: ");
			sbr.append( (right != null)? right.data.id : -1);
			sbr.append(" ( ");
			sbr.append(data.a);
			sbr.append(" -> ");
			sbr.append(data.b);
			sbr.append(" )");
			
			return sbr.toString();
		}
		
		public void printTree()
		{
			ArrayList<BSPNode> nodes = new ArrayList<BSPNode>();
			nodes.add(this);
			
			while(!nodes.isEmpty())
			{
				BSPNode cur = nodes.remove(0);
				System.out.println(cur);
				if(cur.left != null)
				{
					nodes.add(cur.left);
				}
				if(cur.right != null)
				{
					nodes.add(cur.right);
				}
			}
		}
	}
	
	public Map()
	{
		
	}
	
	private Map(File directory)
	{
		try
		{
			File[] files = directory.listFiles();
			
			for(File geoFile : files)
			{
				if(geoFile.getName().equalsIgnoreCase("geo"))
				{
					loadGeoBSP(geoFile);
					break;
				}
			}
			
			for(File descFile : files)
			{
				if(descFile.getName().equalsIgnoreCase("desc"))
				{
					loadDesc(descFile);
					break;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Open a dialog to either load a new map or save the current state of this one
	 */
	public void opendialog()
	{
		MapDialog dialog = new MapDialog(this);
	}
	
	/**
	 * Load the description file for this map
	 * @param descFile
	 * @throws Exception
	 */
	private void loadDesc(File descFile) throws Exception
	{	
		if(!descFile.exists())
			return;
		desc = new Dijkstra.Description();
		
		Scanner s = new Scanner(descFile);
		s.useDelimiter("[^-?\\d\\.?\\d*]+");
		
		while(s.hasNext())
		{
			Vector[] temp = new Vector[2];
			for(int i = 0 ; i < 2 ;i++)
			{
				if(s.hasNext())
				{
					temp[i] = new Vector(s.nextDouble(), s.nextDouble());
				}
				else
				{
					throw new Exception("Missing " + (3 - i) + " points in descFile\n");
				}
			}
			
			desc.addEdge(temp[0], temp[1]);
		}
		
		s.close();
	}
	
	private void loadGeo(File geoFile) throws Exception
	{
		geo.clear();
		
		Scanner s = new Scanner(geoFile);
		s.useDelimiter("[^-?\\d\\.?\\d*]+");
		
		while(s.hasNext())
		{
			Vector[] temp = new Vector[3];
			for(int i = 0 ; i < 3 ;i++)
			{
				if(s.hasNext())
				{
					temp[i] = new Vector(s.nextDouble(), s.nextDouble());
				}
				else
				{
					throw new Exception("Missing " + (3 - i) + " points in geoFile\n");
				}
			}
			
			Triangle toAdd = new Triangle(temp[0], temp[1], temp[2]);
			if(!toAdd.isFlat())
			{
				this.geo.add(toAdd);
			}
		}
		
		s.close();
	}
	
	private void loadGeoBSP(File geoFile) throws Exception
	{
		geo.clear();
		head = null;
		
		Scanner s = new Scanner(geoFile);
		s.useDelimiter("[^-?\\d\\.?\\d*]+");
		
		while(s.hasNext())
		{
			Vector[] temp = new Vector[3];
			for(int i = 0 ; i < 3 ;i++)
			{
				if(s.hasNext())
				{
					temp[i] = new Vector(s.nextDouble(), s.nextDouble());
				}
				else
				{
					throw new Exception("Missing " + (3 - i) + " points in geoFile\n");
				}
			}
			
			Triangle toAdd = new Triangle(temp[0], temp[1], temp[2]);
			if(!toAdd.isFlat())
			{
				this.geo.add(toAdd);
				
				this.head = BSPNode.add(head, new Line(toAdd.a, toAdd.b));
				this.head = BSPNode.add(head, new Line(toAdd.b, toAdd.c));
				this.head = BSPNode.add(head, new Line(toAdd.c, toAdd.a));
			}
		}
		
		if(Shoot.getInstance().DEBUG)
			head.printTree();
		
		s.close();
	}
	
	private void saveGeo(File file) throws IOException
	{
		if(file.exists())
		{
			file.delete();
		}
		else
		{
			file.mkdirs();
			file.delete();
		}
		
		file.createNewFile();
		PrintStream ps = new PrintStream(file);
		for(Triangle t: geo)
		{
			ps.println(t);
		}
		ps.close();
	}
	
	private void saveDesc(File file) throws IOException
	{
		if(file.exists())
		{
			file.delete();
		}
		else
		{
			file.mkdirs();
			file.delete();
		}
		
		file.createNewFile();
		PrintStream ps = new PrintStream(file);
		for(Dijkstra.Edge e: desc.getEdges())
		{
			ps.println(e);
		}
		ps.close();
	}
	
	public boolean clearLine(Vector a, Vector b)
	{
		ArrayList<BSPNode> check = new ArrayList<BSPNode>(50);
		
		if(head != null)
			check.add(head);
		
		while(!check.isEmpty())
		{
			BSPNode cur = check.remove(0);
			//System.out.println(cur);
			double crossA = cur.data.ab.cross(a.sub(cur.data.a));
			double crossB = cur.data.ab.cross(b.sub(cur.data.a));
			
			if(crossA > 0 || crossB > 0)
			{
				if(cur.left != null)
					check.add(cur.left);
			}
			if(crossA < 0 || crossB < 0)
			{
				if(cur.right != null)
					check.add(cur.right);
			}
			
			Vector ab = cur.data.b.sub(cur.data.a);
			double t = Vector.lineSegIntersectLine(cur.data.a, cur.data.b, ab, a, b);
			//System.out.println(t);
			
			//Check to see if this edge intersects the given line
			if(t > 0 && t < 1
					&& b.sub(a).scale(t).add(a).isLineBounded(cur.data.a, cur.data.b, Triangle.DEFAULT_ERROR))
				return false;
		}
		
		return true;
	}
	
	public ArrayList<Line> getAllLines()
	{
		ArrayList<Line> ret = new ArrayList<Line>();
		ArrayList<BSPNode> check = new ArrayList<BSPNode>();
		
		check.add(head);
		
		while(!ret.isEmpty())
		{
			BSPNode cur = check.remove(0);
			ret.add(cur.data);
			
			if(cur.left != null)
				check.add(cur.left);
			if(cur.right != null)
				check.add(cur.right);
		}
		
		return ret;
		
	}
	
	private class MapDialog implements ActionListener
	{
		JTextField tfMapname;
		Map handler;
		
		/*
		 * The constructor was mostly Generated with JIVER
		 */
		MapDialog(Map handler)
		{
			this.handler = handler;
			
			JFrame frame;
			JPanel pnHolder;
			JLabel lbInfo;
			JButton btBut0;
			JButton btBut4;
			
			pnHolder = new JPanel();
			pnHolder.setBorder( BorderFactory.createTitledBorder( "Map Tools" ) );
			GridBagLayout gbHolder = new GridBagLayout();
			GridBagConstraints gbcHolder = new GridBagConstraints();
			pnHolder.setLayout( gbHolder );
			
			tfMapname = new JTextField( );
			gbcHolder.gridx = 7;
			gbcHolder.gridy = 7;
			gbcHolder.gridwidth = 7;
			gbcHolder.gridheight = 1;
			gbcHolder.fill = GridBagConstraints.HORIZONTAL;
			gbcHolder.weightx = 0;
			gbcHolder.weighty = 0;
			gbcHolder.anchor = GridBagConstraints.CENTER;
			gbHolder.setConstraints( tfMapname, gbcHolder );
			pnHolder.add( tfMapname );
			
			lbInfo = new JLabel( "Map name:"  );
			gbcHolder.gridx = 7;
			gbcHolder.gridy = 6;
			gbcHolder.gridwidth = 7;
			gbcHolder.gridheight = 1;
			gbcHolder.fill = GridBagConstraints.BOTH;
			gbcHolder.weightx = 1;
			gbcHolder.weighty = 1;
			gbcHolder.anchor = GridBagConstraints.NORTH;
			gbHolder.setConstraints( lbInfo, gbcHolder );
			pnHolder.add( lbInfo );
			
			btBut0 = new JButton( "Load"  );
			btBut0.setActionCommand( "Load" );
			gbcHolder.gridx = 7;
			gbcHolder.gridy = 9;
			gbcHolder.gridwidth = 7;
			gbcHolder.gridheight = 2;
			gbcHolder.fill = GridBagConstraints.BOTH;
			gbcHolder.weightx = 1;
			gbcHolder.weighty = 0;
			gbcHolder.anchor = GridBagConstraints.NORTH;
			gbHolder.setConstraints( btBut0, gbcHolder );
			pnHolder.add( btBut0 );
			btBut0.addActionListener(this);
			
			btBut4 = new JButton( "Save"  );
			btBut4.setActionCommand( "Save" );
			gbcHolder.gridx = 7;
			gbcHolder.gridy = 11;
			gbcHolder.gridwidth = 7;
			gbcHolder.gridheight = 2;
			gbcHolder.fill = GridBagConstraints.BOTH;
			gbcHolder.weightx = 1;
			gbcHolder.weighty = 0;
			gbcHolder.anchor = GridBagConstraints.NORTH;
			gbHolder.setConstraints( btBut4, gbcHolder );
			pnHolder.add( btBut4 );
			btBut4.addActionListener(this);
			
			frame = new JFrame("Map dialog");
			frame.add(pnHolder);
			frame.setVisible(true);
			frame.pack();
			frame.toFront();
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getActionCommand().equalsIgnoreCase("load"))
			{
				try
				{
					handler.loadDesc(new File("Maps//" + tfMapname.getText() + "//" + "desc" ));
					handler.loadGeoBSP(new File("Maps//" + tfMapname.getText() + "//" + "geo" ));
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(e.getActionCommand().equalsIgnoreCase("save"))
			{
				try
				{
					handler.saveDesc(new File("Maps//" + tfMapname.getText() + "//" + "desc" ));
					handler.saveGeo(new File("Maps//" + tfMapname.getText() + "//" + "geo" ));
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			synchronized(Map.class)
			{
				Map.class.notifyAll();
			}
		}
	}
}

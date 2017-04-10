package geo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Map
{
	public TreeSet<Triangle> geo = new TreeSet<Triangle>();
	public Dijkstra.Description desc = new Dijkstra.Description();
	
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
					loadGeo(geoFile);
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
					handler.loadGeo(new File("Maps//" + tfMapname.getText() + "//" + "geo" ));
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
		}
	}
}

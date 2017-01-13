package geo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeSet;

public class Map
{
	private TreeSet<Triangle> geo = new TreeSet<Triangle>();
	private Dijkstra.Description desc = new Dijkstra.Description();
	
	public Map()
	{
		
	}
	
	public Map(File geoFile, File descFile)
	{
		try
		{
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
				
				this.geo.add(new Triangle(temp[0], temp[1], temp[2]));
			}
			
			s.close();
			
			s = new Scanner(descFile);
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
}

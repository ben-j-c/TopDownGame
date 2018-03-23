package game;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;

import game.Entities.Projectile;
import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;

public class Display  extends JFrame implements GLEventListener
{
	public Shoot inst;
	GLU glu = new GLU();
	
	protected static final double SLAB_HEIGHT = 0.2; 
	
	Display(Shoot inst)
	{
		this.inst = inst;
	}
	
	@Override
	public void display(GLAutoDrawable drawable)
	{
		synchronized(inst)
		{
			inst.stepGame();
			
			GL2 gl = drawable.getGL().getGL2();
			
			gl.glLoadIdentity();
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			
			if(inst.GAME_STARTED)
				setCamera(gl, glu);
			else
				setCameraMapping(gl, glu);
			
			drawMapGeometry(gl);
			drawEnts(gl);
			if(Shoot.DEBUG || !inst.GAME_STARTED)
				drawGraph(gl);
			
			gl.glTranslated((inst.getPlayerPos().x + inst.getOffset().x), (inst.getPlayerPos().y + inst.getOffset().y), 0);
			drawPlayerInfo(gl);
		}
		
	}
	
	public void dispose(GLAutoDrawable drawable)
	{
		
	}
	
	@Override
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		gl.glPointSize(5.0f);
		gl.glLineWidth(1.0f);
		
		gl.glEnable(GL2.GL_BLEND);
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-1, 1, -1, 1, 0.01, 1000);
		//gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		
		
		// Enable smooth shading.
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		// Define "clear" color.
		gl.glClearColor(0f, 0f, 0f, 0f);
		
		// We want a nice perspective.
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		float[] pos = {1,-1,1,0};
		float[] diff = {1,1,1,1};
		
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diff, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		
		if(Shoot.DEBUG)
			drawable.getAnimator().setUpdateFPSFrames(25, System.out);
		
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		
	}
	
	private void setCamera(GL2 gl, GLU glu) 
	{
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		
		glu.gluPerspective(75, 1, 0.01, 1000);		
		//look at the player from 1.5 units directly above the player 
		glu.gluLookAt(
				inst.player.pos.x, inst.player.pos.y, 1.5, 
				inst.player.pos.x, inst.player.pos.y, 0,
				0, 1, 0);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	private void setCameraMapping(GL2 gl, GLU glu) 
	{
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		gl.glOrtho(-1, 1, -1, 1, 0.01, 1000);
		
		//look at the player from 2.5 units directly above the player 
		glu.gluLookAt(
				inst.player.pos.x + inst.offset.x, inst.player.pos.y + inst.offset.y, 1.5, 
				inst.player.pos.x + inst.offset.x, inst.player.pos.y + inst.offset.y, 0,
				0, 1, 0);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////RENDER FUNCTIONS/////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void drawMapGeometry(GL2 gl)
	{
		gl.glColor3d(0.75,0.75,0.75);
		gl.glBegin(GL2.GL_TRIANGLES);
		
		
		for(Triangle t : inst.mw.gameMap.geo)
		{
			gl.glNormal3d(0, 0, 1);
			gl.glVertex3d(t.a.x, t.a.y, SLAB_HEIGHT);
			gl.glVertex3d(t.b.x, t.b.y, SLAB_HEIGHT);
			gl.glVertex3d(t.c.x, t.c.y, SLAB_HEIGHT);
		}
		
		gl.glEnd();
		
		for(Triangle t : inst.mw.gameMap.geo)
		{
			drawWalls(gl, t);
		}
		
		gl.glBegin(GL2.GL_LINES);
		gl.glNormal3d(0, 0, 1);
		for(Triangle t : inst.mw.gameMap.geo)
		{
			gl.glVertex3d(t.a.x, t.a.y, SLAB_HEIGHT);
			gl.glVertex3d(t.b.x, t.b.y, SLAB_HEIGHT);
			
			gl.glVertex3d(t.b.x, t.b.y, SLAB_HEIGHT);
			gl.glVertex3d(t.c.x, t.c.y, SLAB_HEIGHT);
			
			gl.glVertex3d(t.c.x, t.c.y, SLAB_HEIGHT);
			gl.glVertex3d(t.a.x, t.a.y, SLAB_HEIGHT);
		}
		
		gl.glEnd();
	}
	
	private void drawWalls(GL2 gl,Triangle t)
	{
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glNormal3d(t.ab.y, -t.ab.x, 0);
		gl.glVertex3d(t.a.x, t.a.y, 0);
		gl.glVertex3d(t.a.x, t.a.y, SLAB_HEIGHT);
		gl.glVertex3d(t.b.x, t.b.y, SLAB_HEIGHT);
		gl.glVertex3d(t.b.x, t.b.y, 0);
		
		gl.glNormal3d(t.bc.y, -t.bc.x, 0);
		gl.glVertex3d(t.b.x, t.b.y, 0);
		gl.glVertex3d(t.b.x, t.b.y, SLAB_HEIGHT);
		gl.glVertex3d(t.c.x, t.c.y, SLAB_HEIGHT);
		gl.glVertex3d(t.c.x, t.c.y, 0);
		
		gl.glNormal3d(t.ca.y, -t.ca.x, 0);
		gl.glVertex3d(t.c.x, t.c.y, 0);
		gl.glVertex3d(t.c.x, t.c.y, SLAB_HEIGHT);
		gl.glVertex3d(t.a.x, t.a.y, SLAB_HEIGHT);
		gl.glVertex3d(t.a.x, t.a.y, 0);
		
		gl.glEnd();
		
	}
	
	public void drawGraph(GL2 gl)
	{
		gl.glBegin(GL.GL_LINES);
		gl.glNormal3d(0, 0, 1);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : inst.mw.gameMap.desc.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : inst.mw.gameMap.desc.getNodes())
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();
		
		gl.glLineWidth(1.0f);
		
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : inst.mw.descWithPlayer.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : inst.mw.descWithPlayer.getNodes())
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();
		
		gl.glLineWidth(2.0f);
		
		gl.glPopMatrix();
	}
	
	public void drawPlayerInfo(GL2 gl)
	{
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glColor4d(0.75, 0.25, 0.25, 0.5);
		
		gl.glNormal3d(0, 0, 1);
		gl.glVertex3d(-1, 1, 0.5);
		gl.glVertex3d(-1, 0.95, 0.5);
		gl.glVertex3d((inst.player.life - 5.0)/5.0 , 0.95, 0.5);
		gl.glVertex3d((inst.player.life - 5.0)/5.0 , 1, 0.5);
		
		
		gl.glEnd();
		
		{
			java.awt.Point pos = inst.canvas.getMousePosition();
			if(pos != null)
			{
				
				
				gl.glBegin(GL2.GL_POINTS);
				gl.glColor3d(Math.random(), Math.random(), Math.random());
				Vector pos2 = inst.translateToReal(pos.x, pos.y);
				gl.glVertex2d(pos.x, pos.y);
				gl.glEnd();
			}
		}
	}
	
	public void drawEnts(GL2 gl)
	{
		if(inst.GAME_STARTED)
		{
			//gl.glColor3d(1, 0, 0);
			for(Entity e : inst.entityWrapper.ents)
			{
				e.render(gl);
				
				if((e.TYPE & Entity.PROJECTILE) != 0)
				{
					gl.glColor3d(Math.random()*0.5 +0.5, Math.random()*0.5, 0);
					drawCube(gl, 0.005, e.pos.x, e.pos.y, 0.01);
					
				}
				
				if((e.TYPE & Entity.BODY) != 0)
				{
					gl.glColor3d(e.r,e.g,e.b);
					drawCube(gl, 0.005, e.pos.x, e.pos.y, 0.01);
				}
				
			}
			
			gl.glColor3d(0, 1, 0);
			gl.glVertex2d(inst.player.pos.x, inst.player.pos.y);
			drawCube(gl, 0.005, inst.player.pos.x, inst.player.pos.y, 0.01);
			
			for(Projectile p : inst.entityWrapper.projectiles)
			{
				p.render(gl);
			}
		}
	}
	public static void drawCube(GL2 gl, double r, double x, double y, double z)
	{
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glNormal3d(0, 0, -1);
		gl.glVertex3d(-r, -r, -r);
		gl.glVertex3d(+r, -r, -r);
		gl.glVertex3d(+r, +r, -r);
		gl.glVertex3d(-r, +r, -r);
		
		gl.glNormal3d(0, 0, 1);
		gl.glVertex3d(-r, -r, +r);
		gl.glVertex3d(+r, -r, +r);
		gl.glVertex3d(+r, +r, +r);
		gl.glVertex3d(-r, +r, +r);
		
		gl.glNormal3d(0, -1, 0);
		gl.glVertex3d(-r, -r, -r);
		gl.glVertex3d(+r, -r, -r);
		gl.glVertex3d(+r, -r, +r);
		gl.glVertex3d(-r, -r, +r);
		
		gl.glNormal3d(0, 1, 0);
		gl.glVertex3d(+r, +r, -r);
		gl.glVertex3d(-r, +r, -r);
		gl.glVertex3d(-r, +r, +r);
		gl.glVertex3d(+r, +r, +r);
		
		gl.glNormal3d(1, 0, 0);
		gl.glVertex3d(+r, -r, -r);
		gl.glVertex3d(+r, +r, -r);
		gl.glVertex3d(+r, +r, +r);
		gl.glVertex3d(+r, -r, +r);
		
		gl.glNormal3d(-1, 0, 0);
		gl.glVertex3d(-r, -r, -r);
		gl.glVertex3d(-r, +r, -r);
		gl.glVertex3d(-r, +r, +r);
		gl.glVertex3d(-r, -r, +r);
		
		gl.glEnd();
		gl.glPopMatrix();
	}
	
}

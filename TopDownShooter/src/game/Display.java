package game;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import geo.Dijkstra;
import geo.Triangle;
import geo.Vector;

public class Display  extends JFrame implements GLEventListener
{
	public Shoot inst;

	Display(Shoot inst)
	{
		this.inst = inst;
	}

	@Override
	public void display(GLAutoDrawable drawable)
	{
		inst.stepGame();

		GL2 gl = drawable.getGL().getGL2();

		gl.glLoadIdentity();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		gl.glPushMatrix();

		gl.glTranslated(-(inst.getPlayerPos().x + inst.getOffset().x), -(inst.getPlayerPos().y + inst.getOffset().y), 0);

		drawMapGeometry(gl);
		drawEnts(gl);
		if(inst.DEBUG)
			drawGraph(gl);

		gl.glPopMatrix();

		drawPlayerInfo(gl);

	}

	public void dispose(GLAutoDrawable drawable)
	{

	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		gl.glPointSize(5.0f);
		gl.glLineWidth(2.0f);

		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_POLYGON_SMOOTH);

	}

	public void reshape(GLAutoDrawable drawable, int w, int h, int arg3,
			int arg4)
	{

	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////RENDER FUNCTIONS/////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void drawMapGeometry(GL2 gl)
	{
		gl.glColor3d(0.75,0.75,0.75);
		gl.glBegin(GL2.GL_TRIANGLES);


		for(Triangle t : inst.gameMap.geo)
		{
			gl.glVertex2d(t.a.x, t.a.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.c.x, t.c.y);
		}

		gl.glEnd();

		gl.glBegin(gl.GL_LINES);

		for(Triangle t : inst.gameMap.geo)
		{
			gl.glVertex2d(t.a.x, t.a.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.b.x, t.b.y);
			gl.glVertex2d(t.c.x, t.c.y);
			gl.glVertex2d(t.c.x, t.c.y);
			gl.glVertex2d(t.a.x, t.a.y);
		}

		gl.glEnd();
	}

	public void drawGraph(GL2 gl)
	{
		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : inst.gameMap.desc.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();

		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : inst.gameMap.desc.getNodes())
		{
			gl.glVertex2d(v.x, v.y);
		}
		gl.glEnd();

		gl.glLineWidth(1.0f);

		gl.glBegin(GL.GL_LINES);
		gl.glColor3d(0.5, 0.5, 0.5);
		for(Dijkstra.Edge e : inst.descWithPlayer.getEdges())
		{
			gl.glVertex2d(e.a.x, e.a.y);
			gl.glVertex2d(e.b.x, e.b.y);
		}
		gl.glEnd();

		gl.glBegin(GL.GL_POINTS);
		gl.glColor3d(0.5, 0, 1);
		for(Vector v : inst.descWithPlayer.getNodes())
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

		gl.glVertex2d(-1, 1);
		gl.glVertex2d(-1, 0.95);
		gl.glVertex2d((inst.player.life - 5.0)/5.0 , 0.95);
		gl.glVertex2d((inst.player.life - 5.0)/5.0 , 1);


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
			gl.glBegin(gl.GL_POINTS);
			for(Entity e : inst.ents)
			{
				if((e.TYPE & Entity.PROJECTILE) != 0)
				{
					gl.glColor3d(Math.random()*0.5 +0.5, Math.random()*0.5, 0);
					gl.glVertex2d(e.pos.x, e.pos.y);

				}

				if((e.TYPE & Entity.BODY) != 0)
				{
					gl.glColor3d(e.r,e.g,e.b);
					gl.glVertex2d(e.pos.x, e.pos.y);
				}

			}

			gl.glColor3d(0, 1, 0);
			gl.glVertex2d(inst.player.pos.x, inst.player.pos.y);
			gl.glEnd();

			gl.glBegin(gl.GL_LINES);

			for(Entity e : inst.ents)
			{
				if((e.TYPE & Entity.LASER) != 0)
				{

					gl.glColor3d(Math.random()*0.5 +0.5, Math.random()*0.5, 0);
					gl.glVertex2d(e.pos.x,e.pos.y);
					gl.glVertex2d(e.v.x,e.v.y);
				}
			}


			gl.glEnd();
		}
	}

}

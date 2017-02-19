package game;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

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
		
		inst.drawMapGeometry(gl);
		inst.drawEnts(gl);
		//inst.drawGraph(gl);
		
		gl.glPopMatrix();
		
		inst.drawPlayerInfo(gl);
		
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
	
}

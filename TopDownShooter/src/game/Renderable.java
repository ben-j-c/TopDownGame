package game;
import com.jogamp.opengl.GL2;

public interface Renderable
{
	/**
	 * This will be called when the rendering thread deems it necessary to render the object.
	 * @param gl
	 */
	public void render(GL2 gl);
}

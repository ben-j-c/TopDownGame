package game.Entities;

public interface Body
{
	/**
	 * Called when this body is contacted by another body.
	 * @param b
	 */
	public void contact(Body b);
}

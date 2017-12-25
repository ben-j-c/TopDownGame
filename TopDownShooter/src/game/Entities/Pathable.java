package game.Entities;
/**
 * Interface intended to allow for pathing of an entity
 * @author Ben
 *
 */
public interface Pathable
{
	public void prePath();
	public void path();
	public void postPath();
}

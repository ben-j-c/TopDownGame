package game.Entities;

/**
 * An interface intended to allow for the parallel execution of a frequent task in a MT_Generic handler
 * @author Ben
 *
 */
public interface Dynamic
{
	/**
	 * Function that can be executed in parallel
	 */
	public void doStep();
}

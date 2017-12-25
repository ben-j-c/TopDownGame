package game.Entities;

public interface Monster
{
	/**
	 * Gets called before an enemy has taken enough damage to be removed from the entity wrapper
	 */
	public void death();
	
	/**
	 * Gets called after put into entity wrapper
	 */
	public void spawn();
}

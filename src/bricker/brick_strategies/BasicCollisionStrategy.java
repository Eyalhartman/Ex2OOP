package bricker.brick_strategies;

import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;

/**
 * BasicCollisionStrategy implements a simple collision behavior for bricks.
 * When a collision occurs, the brick (object1) is removed from the game,
 * and the brick counter in the BrickerGameManager is decremented.
 */
public class BasicCollisionStrategy implements CollisionStrategy {

	private BrickerGameManager brickerGameManager;

	/**
	 * Constructs a BasicCollisionStrategy with a reference to the BrickerGameManager.
	 *
	 * @param brickerGameManager The game manager responsible for managing global game state,
	 *                           such as removing game objects and tracking the brick counter.
	 */
	public BasicCollisionStrategy(BrickerGameManager brickerGameManager) {
		this.brickerGameManager = brickerGameManager;
	}

	/**
	 * Handles the collision between two game objects. This strategy simply removes
	 * the first object (typically the brick) from the game and notifies the game manager
	 * to decrement the remaining bricks counter.
	 *
	 * @param object1 The first object involved in the collision (usually the brick).
	 * @param object2 The second object involved in the collision (e.g., the ball).
	 */
	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		brickerGameManager.removeGameObject(object1); // Remove the brick from the game
		brickerGameManager.decrementCounter();        // Decrease remaining bricks count
	}
}

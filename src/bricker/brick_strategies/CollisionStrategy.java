package bricker.brick_strategies;

import danogl.GameObject;

/**
 * An interface representing a collision strategy for bricks in the game.
 * <p>
 * Implementations of this interface define the behavior that should occur
 * when a brick collides with another game object (e.g., the ball).
 * </p>
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public interface CollisionStrategy {
	/**
	 * Defines what happens when two game objects collide.
	 *
	 * @param object1 The first game object involved in the collision (typically the brick).
	 * @param object2 The second game object involved in the collision (typically the ball).
	 */
	void onCollision(GameObject object1, GameObject object2);
}

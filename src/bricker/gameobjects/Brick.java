package bricker.gameobjects;

import bricker.brick_strategies.CollisionStrategy;
import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a brick in the Bricker game.
 * <p>
 * When a ball or any other object collides with this brick,
 * it delegates the collision handling logic to a {@link CollisionStrategy}
 * instance, allowing flexible behavior such as disappearing, spawning
 * bonuses, or triggering special effects.
 * </p>
 *
 *  * @author Eyal Hartman, Dana Weitzhandler
 */
public class Brick extends GameObject {
	private final CollisionStrategy collisionStrategy;

	/**
	 * Constructs a new Brick object with the given position, size, visual appearance,
	 * and collision behavior.
	 *
	 * @param topLeftCorner      Position of the brick in window coordinates.
	 *                           (0,0) is the top-left corner of the window.
	 * @param dimensions         Width and height of the brick in pixels.
	 * @param renderable         The image or shape to draw for this brick.
	 *                           If null, the brick will not be rendered visually.
	 * @param collisionStrategy  A strategy object that defines the brick's behavior
	 *                           upon collision.
	 */
	public Brick(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable,
				 CollisionStrategy collisionStrategy) {
		super(topLeftCorner, dimensions, renderable);
		this.collisionStrategy = collisionStrategy;
	}

	/**
	 * Called automatically when another object collides with this brick.
	 * Delegates the collision response to the brick's {@link CollisionStrategy}.
	 *
	 * @param other     The other GameObject involved in the collision.
	 * @param collision Information about the collision (position, normal, etc.).
	 */
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		this.collisionStrategy.onCollision(this, other);
	}
}

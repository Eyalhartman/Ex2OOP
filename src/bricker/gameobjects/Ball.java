package bricker.gameobjects;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.Sound;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a ball in the Bricker game.
 * The ball moves within the game window and bounces off other objects it collides with.
 * On each collision, it inverts its velocity based on the collision normal, plays a sound,
 * and increments its collision counter.
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class Ball extends GameObject {

	private final Sound collisionSound;
	private int collisionCounter = 0;

	/**
	 * Constructs a new Ball object.
	 *
	 * @param topLeftCorner  Position of the object in window coordinates (pixels).
	 *                       Note that (0,0) is the top-left corner of the window.
	 * @param dimensions     Width and height of the ball in window coordinates.
	 * @param renderable     The renderable representing the ball. Can be null if no rendering is needed.
	 * @param collisionSound The sound to play upon each collision.
	 */
	public Ball(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable, Sound collisionSound) {
		super(topLeftCorner, dimensions, renderable);
		this.collisionSound = collisionSound;
	}

	/**
	 * Handles behavior upon entering a collision.
	 * Flips the ball's velocity according to the collision normal,
	 * plays a collision sound, and increments the collision counter.
	 *
	 * @param other     The GameObject this ball collided with.
	 * @param collision Collision information including the normal vector.
	 */
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		Vector2 newVel = getVelocity().flipped(collision.getNormal());
		setVelocity(newVel);
		this.collisionSound.play();
		this.collisionCounter++;
	}

	/**
	 * Returns the number of times this ball has collided with other objects.
	 *
	 * @return The collision counter.
	 */
	public int getCollisionCounter() {
		return this.collisionCounter;
	}
}

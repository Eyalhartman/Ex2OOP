package bricker.brick_strategies;

import bricker.gameobjects.Ball;
import bricker.gameobjects.PuckBall;
import danogl.GameObject;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * A collision strategy that enables "Turbo Mode" for the ball.
 * <p>
 * When the ball hits a brick for the first time under this strategy,
 * it temporarily increases its speed and changes its appearance.
 * After a certain number of collisions, the ball returns to its original
 * velocity and appearance.
 * </p>
 */
public class TurboModeStrategy implements CollisionStrategy {

	private static final int NUM_COLLISIONS = 6;
	private static final float SPEED_FACTOR = 1.4f;

	private final Ball ball;
	private final BasicCollisionStrategy basicCollisionStrategy;
	private final Renderable renderable;

	private int collisionCounter;
	private boolean turboMode = false;
	private Renderable originalRenderer;
	private Vector2 originalVel;

	/**
	 * Constructs a TurboModeStrategy that wraps a basic strategy and augments it
	 * with turbo behavior (speed-up and appearance change).
	 *
	 * @param ball                  The main ball to apply turbo behavior to.
	 * @param basicCollisionStrategy The basic brick destruction behavior.
	 * @param renderable            The renderable to apply to the ball during turbo mode.
	 */
	public TurboModeStrategy(Ball ball, BasicCollisionStrategy basicCollisionStrategy, Renderable renderable) {
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.renderable = renderable;
		this.ball = ball;
	}

	/**
	 * Handles the collision event. If the turbo mode is not yet active,
	 * it enables turbo mode for the ball (unless the ball is a puck).
	 *
	 * @param object1 The brick that was hit.
	 * @param object2 The object that collided with the brick (typically a ball).
	 */
	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		basicCollisionStrategy.onCollision(object1, object2);

		if (object2 instanceof PuckBall) {
			return; // Turbo mode is only for the main ball, not for pucks
		}

		if (!this.turboMode) {
			this.collisionCounter = this.ball.getCollisionCounter();
			this.turboMode = true;

			originalVel = ball.getVelocity();
			Vector2 newVel = originalVel.mult(SPEED_FACTOR);
			ball.setVelocity(newVel);

			this.originalRenderer = ball.renderer().getRenderable();
			ball.renderer().setRenderable(renderable);
		}
	}

	/**
	 * Updates the turbo state of the ball. If the ball has reached
	 * the required number of collisions in turbo mode, it reverts
	 * to its original velocity and appearance.
	 *
	 * @param deltaTime The time passed since the last frame (not used here).
	 */
	public void update(float deltaTime) {
		if (!turboMode || originalVel == null || originalRenderer == null) return;

		if (this.ball.getCollisionCounter() - this.collisionCounter >= NUM_COLLISIONS) {
			this.turboMode = false;
			ball.setVelocity(originalVel);
			ball.renderer().setRenderable(originalRenderer);
		}
	}
}

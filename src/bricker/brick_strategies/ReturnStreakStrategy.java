package bricker.brick_strategies;

import bricker.gameobjects.FallingHeart;
import bricker.gameobjects.Paddle;
import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * A strategy for handling the collision between a brick and a ball.
 * When a brick is hit by a ball, it creates a FallingHeart object that falls from the brick's position.
 * The heart will increase the player's lives when it collides with the paddle.
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class ReturnStreakStrategy implements CollisionStrategy{
	private final CollisionStrategy basicCollisionStrategy;
	private final GameObjectCollection gameObjects;
	private final Vector2 windowDimensions;
	private final Paddle originalPaddle;
	private final Renderable heartImage;
	private final Vector2 heartDimensions;
	private final BrickerGameManager gameManager;

	private static final float CENTER_OFFSET_FACTOR = 0.5f;
	private static final int FALLING_HEART_SPEED_Y = 100;
	private static final int FALLING_HEART_SPEED_X = 0;


	/**
	 * Constructs a new ReturnStreakStrategy instance.
	 *
	 * @param delegate         The delegate collision strategy to handle the collision.
	 * @param gameObjects      The collection of game objects in the game.
	 * @param windowDimensions The dimensions of the window.
	 * @param originalPaddle   The original paddle object.
	 * @param heartImage       The image representing the heart.
	 * @param heartDimensions  The dimensions of the heart.
	 * @param gameManager      The game manager for managing game state.
	 */
	public ReturnStreakStrategy(CollisionStrategy delegate,
							  GameObjectCollection gameObjects,
							  Vector2 windowDimensions,
							  Paddle originalPaddle,
							  Renderable heartImage,
							  Vector2 heartDimensions,
							  BrickerGameManager gameManager) {
		this.basicCollisionStrategy         = delegate;
		this.gameObjects      = gameObjects;
		this.windowDimensions = windowDimensions;
		this.originalPaddle   = originalPaddle;
		this.heartImage       = heartImage;
		this.heartDimensions  = heartDimensions;
		this.gameManager      = gameManager;
	}
    	/**
	 * Handles the collision between a brick and a ball.
	 * Creates a FallingHeart object that falls from the brick's position.
	 *
	 * @param brick The brick that was hit by the ball.
	 * @param other The other object involved in the collision (the ball).
	 */
	public void onCollision(GameObject brick, GameObject other) {
		if (!(other instanceof bricker.gameobjects.Ball)) return;

		basicCollisionStrategy.onCollision(brick, other);

		Vector2 center   = brick.getCenter();
		Vector2 topLeft = new Vector2(
				center.x() - heartDimensions.x()*CENTER_OFFSET_FACTOR,
				center.y() - heartDimensions.y()*CENTER_OFFSET_FACTOR
		);
		FallingHeart h   = new FallingHeart(
				topLeft, heartDimensions,
				heartImage,
				windowDimensions,
				gameObjects,
				originalPaddle,
				gameManager
		);
		h.setVelocity(new Vector2(FALLING_HEART_SPEED_X, FALLING_HEART_SPEED_Y));
		gameObjects.addGameObject(h, Layer.DEFAULT);
	}
}

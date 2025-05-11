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
 */
public class ReturnStreakStrategy implements CollisionStrategy{
	private final CollisionStrategy delegate;
	private final GameObjectCollection gameObjects;
	private final Vector2 windowDimensions;
	private final Paddle originalPaddle;
	private final Renderable heartImage;
	private final Vector2 heartDimensions;
	private final BrickerGameManager gameManager;

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
		this.delegate         = delegate;
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

		delegate.onCollision(brick, other);

		Vector2 center   = brick.getCenter();
		Vector2 topLeft = new Vector2(
				center.x() - heartDimensions.x()*0.5f,
				center.y() - heartDimensions.y()*0.5f
		);
		FallingHeart h   = new FallingHeart(
				topLeft, heartDimensions,
				heartImage,
				windowDimensions,
				gameObjects,
				originalPaddle,
				gameManager
		);
		h.setVelocity(new Vector2(0, 100));
		gameObjects.addGameObject(h, Layer.DEFAULT);
	}
}

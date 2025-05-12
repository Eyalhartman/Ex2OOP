package bricker.gameobjects;

import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a heart that falls from the brick when it is hit by a ball.
 * The heart will increase the player's lives when it collides with the paddle.
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class FallingHeart extends GameObject {

	private final GameObjectCollection gameObjects;
	private final Paddle originalPaddle;
	private final BrickerGameManager gameManager;
	private final Vector2 windowDimensions;
    	/**
	 * Constructs a new FallingHeart instance.
	 *
	 * @param topLeftCorner   Position of the object, in window coordinates (pixels).
	 *                        Note that (0,0) is the top-left corner of the window.
	 * @param dimensions      Width and height in window coordinates.
	 * @param renderable      The renderable representing the object. Can be null, in which case
	 *                        the GameObject will not be rendered.
	 * @param windowDimensions The dimensions of the window.
	 * @param gameObjects     The collection of game objects in the game.
	 * @param originalPaddle  The original paddle object.
	 * @param gameManager     The game manager for managing game state.
	 */
	public FallingHeart(Vector2 topLeftCorner,
						Vector2 dimensions,
						Renderable renderable,
						Vector2 windowDimensions,
						GameObjectCollection gameObjects,
						Paddle originalPaddle,
						BrickerGameManager gameManager) {
		super(topLeftCorner, dimensions, renderable);
		this.windowDimensions = windowDimensions;
		this.gameObjects      = gameObjects;
		this.originalPaddle   = originalPaddle;
		this.gameManager      = gameManager;
	}

	/**
	 * For this object, the collision with the paddle is handled.
	 *
	 * @return True if the other object is the original paddle, false otherwise.
	 */
	@Override
	public boolean shouldCollideWith(GameObject other) {
		return other == originalPaddle
				&& super.shouldCollideWith(other);
	}
    	/**
	 * Handles the collision with the paddle.
	 * Increases the player's lives and removes the heart from the game.
	 *
	 * @param other     The other object involved in the collision.
	 * @param collision The collision information.
	 */
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		gameManager.incrementLives();
		gameObjects.removeGameObject(this);
	}
    	/**
	 * Updates the position of the heart.
	 * If the heart goes out of the window, it is removed from the game.
	 *
	 * @param deltaTime The time since the last update.
	 */
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (getTopLeftCorner().y() > windowDimensions.y())
			gameObjects.removeGameObject(this);
	}
}

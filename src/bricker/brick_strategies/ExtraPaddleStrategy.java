package bricker.brick_strategies;

import bricker.gameobjects.ExtraPaddle;
import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * A collision strategy that adds an extra controllable paddle when a brick is hit.
 * <p>
 * This strategy wraps another basic collision strategy, performing its logic first.
 * If the player currently has no extra paddle, it spawns a new one at the center
 * of the game window, allowing the player to control an additional paddle temporarily.
 * </p>
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class ExtraPaddleStrategy implements CollisionStrategy{

	private static final float PADDLE_SPAWN_X_Y_FACTOR = 2f;
	private static final String EXTRA_PADDLE_IMAGE_PATH = "assets/assets/paddle.png";
	private static final boolean USE_TRANSPARENCY = true;
	private static final int NO_EXTRA_PADDLES = 0;

	private final BrickerGameManager brickerGameManager;
	private final CollisionStrategy basicCollisionStrategy;
	private final GameObjectCollection gameObjects;
	private final ImageReader imageReader;
	private final UserInputListener inputListener;
	private final Vector2 windowDimensions;
	private final Vector2 paddleDimensions;

	/**
	 * Constructor for ExtraPaddleStrategy.
	 *
	 * @param brickerGameManager The game manager for managing game state.
	 * @param basicCollisionStrategy The basic collision strategy to delegate to.
	 * @param gameObjects The collection of game objects in the game.
	 * @param imageReader The image reader for loading images.
	 * @param inputListener The input listener for user input.
	 * @param windowDimensions The dimensions of the window.
	 * @param paddleDimensions The dimensions of the paddle.
	 */
	public ExtraPaddleStrategy(BrickerGameManager brickerGameManager,
							   CollisionStrategy basicCollisionStrategy,
							   GameObjectCollection gameObjects,
							   ImageReader imageReader,
							   UserInputListener inputListener,
							   Vector2 windowDimensions, Vector2 paddleDimensions) {
		this.brickerGameManager = brickerGameManager;
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.gameObjects = gameObjects;
		this.imageReader = imageReader;
		this.inputListener = inputListener;
		this.windowDimensions = windowDimensions;
		this.paddleDimensions = paddleDimensions;
	}


	/**
	 * Handles the collision between a brick and a ball.
	 * If the player has no extra paddles, creates a new ExtraPaddle object.
	 *
	 * @param object1 The first object involved in the collision (the brick).
	 * @param object2 The second object involved in the collision (the ball).
	 */
	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		if (!(object2 instanceof bricker.gameobjects.Ball)) {
			return;
		}
		basicCollisionStrategy.onCollision(object1, object2);

		if (brickerGameManager.getExtraPaddlesCount() == NO_EXTRA_PADDLES) {
			Vector2 center = new Vector2(
					windowDimensions.x() / PADDLE_SPAWN_X_Y_FACTOR,
					windowDimensions.y() / PADDLE_SPAWN_X_Y_FACTOR
			);

			Renderable paddleImage = imageReader.readImage(EXTRA_PADDLE_IMAGE_PATH, USE_TRANSPARENCY);
			ExtraPaddle newPaddle = new ExtraPaddle(center,
						paddleDimensions,
						paddleImage,
						inputListener,
						windowDimensions,
						gameObjects,
						brickerGameManager
			);
			gameObjects.addGameObject(newPaddle, Layer.DEFAULT);
			brickerGameManager.incrementExtraPaddles();
			}
		}
	}


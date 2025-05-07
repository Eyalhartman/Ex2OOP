package bricker.gameobjects;

import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class ExtraPaddle extends Paddle{
	private static final int MAX_HITS = 4;
	private int hitCount = 0;
	private static final float MOVEMENT_SPEED = 300;

	private final GameObjectCollection gameObjects;
	private final BrickerGameManager gameManager;

	/**
	 * Construct a new GameObject instance.
	 *
	 * @param topLeftCorner    Position of the object, in window coordinates (pixels).
	 *                         Note that (0,0) is the top-left corner of the window.
	 * @param dimensions       Width and height in window coordinates.
	 * @param renderable       The renderable representing the object. Can be null, in which case
	 *                         the GameObject will not be rendered.
	 * @param inputListener    The input listener for user input.
	 * @param windowDimensions The dimensions of the window.
	 */
	public ExtraPaddle(Vector2 topLeftCorner,
					   Vector2 dimensions,
					   Renderable renderable,
					   UserInputListener inputListener,
					   Vector2 windowDimensions, GameObjectCollection gameObjects, BrickerGameManager gameManager) {
		super(topLeftCorner, dimensions, renderable, inputListener, windowDimensions);
		this.gameObjects = gameObjects;
		this.gameManager = gameManager;
	}
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
	}
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		if (!(other instanceof bricker.gameobjects.Ball)) {
			return;
		}
		hitCount++;
		if (hitCount >= MAX_HITS) {
			gameObjects.removeGameObject(this);
			gameManager.decrementExtraPaddles();
		}
	}
}

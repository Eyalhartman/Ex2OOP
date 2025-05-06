package bricker.gameobjects;

import bricker.brick_strategies.CollisionStrategy;
import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.gui.Sound;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class PuckBall extends Ball {
	private final GameObjectCollection gameObjects;
	private final Vector2 windowDimensions;
	private final BrickerGameManager brickerGameManager;
	private final Sound collisionSound;



	/**
	 * Construct a new GameObject instance.
	 *
	 * @param topLeftCorner  Position of the object, in window coordinates (pixels).
	 *                       Note that (0,0) is the top-left corner of the window.
	 * @param dimensions     Width and height in window coordinates.
	 * @param renderable     The renderable representing the object. Can be null, in which case
	 *                       the GameObject will not be rendered.
	 * @param collisionSound
	 */
	public PuckBall(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable,
					Sound collisionSound, GameObjectCollection gameObjects, Vector2 windowDimensions,
					BrickerGameManager brickerGameManager) {
		super(topLeftCorner, dimensions, renderable, collisionSound);
		this.gameObjects = gameObjects;
		this.windowDimensions = windowDimensions;
		this.brickerGameManager = brickerGameManager;
		this.collisionSound = collisionSound;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		double ballHeight = this.getCenter().y();
		if (ballHeight > windowDimensions.y() || ballHeight < 0) {
			gameObjects.removeGameObject(this);
		}
	}

	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {

		super.onCollisionEnter(other, collision);
		Vector2 newVel = getVelocity().flipped(collision.getNormal());
		setVelocity(newVel);
		this.collisionSound.play();

		if (!(other instanceof Ball || other instanceof Paddle )) {
			if (gameObjects.removeGameObject(other)) {
				brickerGameManager.decrementCounter();
			}
		}
	}
}

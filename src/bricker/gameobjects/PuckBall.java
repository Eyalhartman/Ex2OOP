package bricker.gameobjects;

import bricker.brick_strategies.CollisionStrategy;
import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.gui.Sound;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a puck ball in the game, a secondary ball type
 * introduced through special brick behaviors.
 *
 * This class extends {@link Ball} and adds behavior such as
 * self-destruction when exiting the screen and managing game state
 * upon collisions with breakable objects.
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class PuckBall extends Ball {
	private final GameObjectCollection gameObjects;
	private final Vector2 windowDimensions;
	private final BrickerGameManager brickerGameManager;
	private final Sound collisionSound;

	private static final int MAX_HEIGHT = 0;

	/**
	 * Constructs a new {@code PuckBall} instance.
	 *
	 * @param topLeftCorner Position of the puck in window coordinates (pixels).
	 *                      (0,0) is the top-left corner of the window.
	 * @param dimensions Width and height of the puck in pixels.
	 * @param renderable The visual representation of the puck.
	 * @param collisionSound Sound to be played on collision.
	 * @param gameObjects A collection of all game objects in the scene.
	 * @param windowDimensions Dimensions of the game window.
	 * @param brickerGameManager Reference to the game manager to update brick count.
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

	/**
	 * Updates puck state every frame.
	 * If the puck goes off the screen vertically, it is removed from the game.
	 *
	 * @param deltaTime Time elapsed since the last frame.
	 */
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		double ballHeight = this.getCenter().y();
		if (ballHeight > windowDimensions.y() || ballHeight < MAX_HEIGHT) {
			gameObjects.removeGameObject(this);
		}
	}

	/**
	 * Handles behavior when the puck collides with another object.
	 * Plays collision sound, reflects velocity, and if the collided object is a brick,
	 * removes it and updates the game manager's brick count.
	 *
	 * @param other The other GameObject involved in the collision.
	 * @param collision The collision data containing normal vectors and collision point.
	 */
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		Vector2 newVel = getVelocity().flipped(collision.getNormal());
		setVelocity(newVel);
		this.collisionSound.play();

		if (!(other instanceof Ball || other instanceof Paddle)) {
			if (gameObjects.removeGameObject(other)) {
				brickerGameManager.decrementCounter();
			}
		}
	}
}

package bricker.gameobjects;

import danogl.GameObject;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

/**
 * Represents a paddle controlled by the player.
 * The paddle moves left and right in response to keyboard input
 * and is constrained within the horizontal bounds of the game window.
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class Paddle extends GameObject {

	private static final float MOVEMENT_SPEED = 300;
	private final UserInputListener inputListener;
	private final Vector2 windowDimensions;

	private boolean enableMovement = false;


	/**
	 * Construct a new GameObject instance.
	 *
	 * @param topLeftCorner Position of the object, in window coordinates (pixels).
	 *                      Note that (0,0) is the top-left corner of the window.
	 * @param dimensions    Width and height in window coordinates.
	 * @param renderable    The renderable representing the object. Can be null, in which case
	 *                      the GameObject will not be rendered.
	 * @param inputListener The input listener for user input.
	 * @param windowDimensions The dimensions of the window.
	 */
	public Paddle(Vector2 topLeftCorner,
				  Vector2 dimensions,
				  Renderable renderable,
				  UserInputListener inputListener,
				  Vector2 windowDimensions) {
		super(topLeftCorner, dimensions, renderable);
		this.inputListener = inputListener;
		this.windowDimensions = windowDimensions;
	}

	/**
	 * Updates the paddle's position based on user input.
	 * Enables movement after the first update call and ensures the paddle stays within window bounds.
	 *
	 * @param deltaTime Time passed since last frame, used for consistent movement speed.
	 */
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (!enableMovement) {
			inputListener.pressedKeys().clear();
			enableMovement = true;
			return;
		}
		Vector2 movementDir = Vector2.ZERO;
		if (this.inputListener.isKeyPressed(KeyEvent.VK_LEFT))   {
			movementDir = movementDir.add(Vector2.LEFT);
		}
		if (this.inputListener.isKeyPressed(KeyEvent.VK_RIGHT))   {
			movementDir = movementDir.add(Vector2.RIGHT);
		}
		setVelocity(movementDir.mult(MOVEMENT_SPEED));
		Vector2 topLeft = getTopLeftCorner();
		float paddleWidth = getDimensions().x();
		float windowWidth = windowDimensions.x();
		float minX = 0f;
		float maxX = windowWidth - paddleWidth;
		float clampedX = Math.max(minX, Math.min(topLeft.x(), maxX));
		if (clampedX != topLeft.x()) {
			setTopLeftCorner(new Vector2(clampedX, topLeft.y()));
		}
	}
}


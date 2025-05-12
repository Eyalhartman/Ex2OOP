package bricker.brick_strategies;

import bricker.gameobjects.PuckBall;

import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.Sound;
import danogl.gui.SoundReader;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;


import java.util.Random;

/**
 * A collision strategy that spawns additional puck balls when a brick is hit.
 * <p>
 * This class decorates a base collision strategy by first executing its logic
 * (e.g., removing the brick), and then adding two extra puck balls at the brick's center.
 * The puck balls are given randomized directions and added to the game world.
 * </p>
 *
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class ExtraBallsStrategy implements CollisionStrategy{

	private static final int NUM_PUCKS = 2;
	private static final String PUCK_IMAGE = "assets/assets/mockBall.png";
	private static final String PUCK_SOUND = "assets/assets/blop.wav";

	private final ImageReader imageReader;
	private final SoundReader soundReader;
	private final GameObjectCollection gameObjects;
	private final Vector2 puckSize;
	private final int ballSpeed;
	private final CollisionStrategy basicCollisionStrategy;
	private final Vector2 windowDimensions;
	private final BrickerGameManager brickerGameManager;



	/**
	 * Constructs a new ExtraBallsStrategy.
	 *
	 * @param imageReader             Utility to read images for puck rendering.
	 * @param soundReader             Utility to read sound files for collisions.
	 * @param gameObjects             The collection to which new game objects are added.
	 * @param puckSize                Size of the spawned puck balls.
	 * @param speed                   Speed of the spawned puck balls.
	 * @param basicCollisionStrategy  The base collision strategy to wrap/decorate.
	 * @param windowDimensions        The dimensions of the game window.
	 * @param brickerGameManager      Reference to the main game manager.
	 */
	public ExtraBallsStrategy(ImageReader imageReader, SoundReader soundReader,
							  GameObjectCollection gameObjects, Vector2 puckSize, int speed,
							  CollisionStrategy basicCollisionStrategy, Vector2 windowDimensions,
							  BrickerGameManager brickerGameManager){
		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.gameObjects = gameObjects;
		this.puckSize = puckSize;
		this.ballSpeed = speed;
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.windowDimensions = windowDimensions;
		this.brickerGameManager = brickerGameManager;
	}

	/**
	 * Called when a collision involving a brick occurs.
	 * Delegates to the base strategy, and then spawns extra puck balls at the
	 * collision location with randomized movement.
	 *
	 * @param object1 The brick object being hit.
	 * @param object2 The object colliding with the brick (usually a ball).
	 */
	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		basicCollisionStrategy.onCollision(object1, object2);
		Vector2 spawnCenter = object1.getCenter(); // the center of the brick!

		createPucks(spawnCenter);
	}

	/**
	 * Spawns a fixed number of puck balls at a given location with random velocities.
	 *
	 * @param spawnCenter The center position where the pucks should appear.
	 */
	private void createPucks(Vector2 spawnCenter) {
		Renderable ballImage = this.imageReader.readImage(PUCK_IMAGE, true);
		Sound collisionSound = soundReader.readSound(PUCK_SOUND);

		for (int i = 0; i<NUM_PUCKS;i++){

			PuckBall ball =  new PuckBall(spawnCenter,
					puckSize,
					ballImage,
					collisionSound, gameObjects,
					windowDimensions, brickerGameManager);
			Random random = new Random();
			double angle = random.nextDouble()*Math.PI;
			float velocityX = (float)Math.cos(angle)* ballSpeed;
			float velocityY = (float)Math.sin(angle)* ballSpeed;
			ball.setVelocity(new Vector2(velocityX, velocityY));

			gameObjects.addGameObject(ball, Layer.DEFAULT);

		}
	}
}

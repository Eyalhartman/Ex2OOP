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



import java.security.PublicKey;
import java.util.Random;

public class PuckStrategy implements CollisionStrategy{

	private static final int NUM_PUCKS = 2;
	private final ImageReader imageReader;
	private final SoundReader soundReader;
	private final GameObjectCollection gameObjects;
	private final Vector2 puckSize;
	private final int BALL_SPEED;
	private final CollisionStrategy basicCollisionStrategy;
	private final Vector2 windowDimensions;
	private final BrickerGameManager brickerGameManager;
	private final Vector2 puck_loc;


	public PuckStrategy(ImageReader imageReader,
						SoundReader soundReader,
						GameObjectCollection gameObjects,
						Vector2 puckLoc,
						Vector2 puckSize,
						int speed,
						CollisionStrategy basicCollisionStrategy,
						Vector2 windowDimensions,
						BrickerGameManager brickerGameManager){
		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.gameObjects = gameObjects;
		this.puckSize = puckSize;
		this.BALL_SPEED = speed;
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.windowDimensions = windowDimensions;
		this.brickerGameManager = brickerGameManager;
		this.puck_loc = puckLoc;
	}

	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		basicCollisionStrategy.onCollision(object1, object2);
		createPucks();
	}


	private void createPucks() {
		Renderable ballImage = this.imageReader.readImage("assets/assets/mockBall.png", true);
		Sound collisionSound = soundReader.readSound("assets/assets/blop.wav");
		for (int i = 0; i<NUM_PUCKS;i++){
			PuckBall ball =  new PuckBall(puck_loc,
					puckSize,
					ballImage,
					collisionSound, gameObjects,
					windowDimensions, brickerGameManager);
			Random random = new Random();
			double angle = random.nextDouble()*Math.PI;
			float velocityX = (float)Math.cos(angle)*BALL_SPEED;
			float velocityY = (float)Math.sin(angle)*BALL_SPEED;
			ball.setVelocity(new Vector2(velocityX, velocityY));

			gameObjects.addGameObject(ball, Layer.DEFAULT);

		}
	}
}

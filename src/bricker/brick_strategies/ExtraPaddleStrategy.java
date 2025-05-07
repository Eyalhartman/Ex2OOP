package bricker.brick_strategies;

import bricker.gameobjects.ExtraPaddle;
import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class ExtraPaddleStrategy implements CollisionStrategy{

	private final BrickerGameManager brickerGameManager;
	private final CollisionStrategy basicCollisionStrategy;
	private final GameObjectCollection gameObjects;
	private final ImageReader imageReader;
	private final SoundReader soundReader;
	private final Renderable paddleRenderable;
	private final UserInputListener inputListener;
	private final Vector2 windowDimensions;
	private final Vector2 paddleDimensions;


	public ExtraPaddleStrategy(BrickerGameManager brickerGameManager, CollisionStrategy basicCollisionStrategy, GameObjectCollection gameObjects, ImageReader imageReader, SoundReader soundReader, Renderable paddleRenderable, UserInputListener inputListener, Vector2 windowDimensions, Vector2 paddleDimensions){
		this.brickerGameManager = brickerGameManager;
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.gameObjects = gameObjects;
		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.paddleRenderable = paddleRenderable;
		this.inputListener = inputListener;
		this.windowDimensions = windowDimensions;
		this.paddleDimensions = paddleDimensions;
	}



	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		if (!(object2 instanceof bricker.gameobjects.Ball)) {
			return;
		}
		basicCollisionStrategy.onCollision(object1, object2);

		if (brickerGameManager.getExtraPaddlesCount() == 0) {
			Vector2 center = new Vector2(
					windowDimensions.x() / 2f,
					windowDimensions.y() / 2f
			);

			Renderable paddleImage = imageReader.readImage("assets/assets/paddle.png", true);
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


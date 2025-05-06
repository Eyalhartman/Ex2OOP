package bricker.main;

import bricker.brick_strategies.PuckStrategy;
import bricker.gameobjects.Ball;
import bricker.gameobjects.Brick;
import bricker.gameobjects.Paddle;
import bricker.brick_strategies.BasicCollisionStrategy;
import danogl.GameManager;
import danogl.GameObject;

import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.*;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Counter;
import danogl.util.Vector2;


import danogl.gui.WindowController;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class BrickerGameManager extends GameManager {

	private final static int SIZE_BALL = 20;
	public static final int PUCK_SIZE = SIZE_BALL * 3 / 4;
	private final static int BALL_SPEED = 100;
	private final static float PADDLE_WIDTH = 100;
	private final static float PADDLE_BRICK_HEIGHT = 15;
	private final static float PADDLE_FROM_EDGE = 30;
	private final static float WALLS_WIDTH = 5;
	private final static int FIRST_ARG = 0;
	private final static int SECOND_ARG = 1;
	private final static int DEF_LINES = 7;
	private final static int DEF_BRICKS = 8;
	private final static int ADDED_SPACE = 1;
	private final static int MAX_STREAKS = 3;
	private final static int HEART_HEIGHT_WIDTH = 20;
	private final static int NUMERIC_HEIGHT_WIDTH = 23;


	private final int num_lines;
	private final int num_bricks;
	private Ball ball;
	private Vector2 windowDimensions;
	private WindowController windowController;
	private int num_lives = MAX_STREAKS;
	private GameObject[] hearts;

	private Counter bricksCounter = new Counter();


	private ImageReader imageReader;
	private SoundReader soundReader;
	private UserInputListener inputListener;
	private TextRenderable numericLife;
	private GameObject numericLifeObject;

	private boolean wKeyHandled = false;




	public BrickerGameManager(String windowTitle, Vector2 windowDimensions, String[] args){
		super(windowTitle, windowDimensions);
		if (args.length==0){
			this.num_lines = DEF_LINES;
			this.num_bricks = DEF_BRICKS;
		}
		else{
			this.num_lines = parseInt(args[FIRST_ARG]);
			this.num_bricks = parseInt(args[SECOND_ARG]);
		}
	}


	@Override
	public void initializeGame(ImageReader imageReader, SoundReader soundReader,
							   UserInputListener inputListener, WindowController windowController) {

		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.inputListener = inputListener;

		this.windowController = windowController;
		windowController.setTargetFramerate(60);

		super.initializeGame(imageReader, soundReader, inputListener, windowController);

		windowDimensions = windowController.getWindowDimensions();

		//creating background
		createBackground(imageReader);

		//creating ball
		createBall(imageReader, soundReader, windowDimensions);

		//creating user paddle
		createPaddle(imageReader, inputListener);

		//creating walls
		creatingWalls(windowDimensions);

		//creating bricks
		createBricks(imageReader, windowDimensions);

		//creating hearts
		createHearts(imageReader);

		createNumeric();
	}

	private void createNumeric() {
		numericLife = new TextRenderable(Integer.toString(num_lives));
		if (num_lives >= 3){
			numericLife.setColor(Color.green);
		}
		numericLifeObject = new GameObject(new Vector2(2 * WALLS_WIDTH,
				windowDimensions.y() - HEART_HEIGHT_WIDTH - NUMERIC_HEIGHT_WIDTH -5),
				new Vector2(NUMERIC_HEIGHT_WIDTH, NUMERIC_HEIGHT_WIDTH), numericLife);
		gameObjects().addGameObject(numericLifeObject);
	}

	public void decrementCounter() {
		bricksCounter.decrement();
	}

	public boolean removeGameObject(GameObject object) {
		return gameObjects().removeGameObject(object);
	}


	private void createBackground(ImageReader imageReader) {
		Renderable backgroundImage = imageReader.readImage("assets/assets/DARK_BG2_small.jpeg", true);
		GameObject background = new GameObject(Vector2.ZERO, new Vector2(windowDimensions.x(),
				windowDimensions.y()), backgroundImage);
		gameObjects().addGameObject(background, Layer.BACKGROUND);
		background.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
	}

	private void createPaddle(ImageReader imageReader, UserInputListener inputListener) {
		Renderable paddleImage = imageReader.readImage("assets/assets/paddle.png", true);
		GameObject userPaddle =  new Paddle(Vector2.ZERO, new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT),
				paddleImage, inputListener, windowDimensions);
		userPaddle.setCenter(new Vector2(windowDimensions.x()/2, windowDimensions.y()-PADDLE_FROM_EDGE));
		gameObjects().addGameObject(userPaddle, Layer.DEFAULT);
	}

	private void createHearts(ImageReader imageReader) {
		Renderable heartImage = imageReader.readImage("assets/assets/heart.png",true);
		float len_heart = HEART_HEIGHT_WIDTH+1;
		hearts = new GameObject[MAX_STREAKS];
		num_lives = MAX_STREAKS;
		for(int i=0; i<MAX_STREAKS; i++){
			hearts[i] = new GameObject(new Vector2(2*WALLS_WIDTH+(i*len_heart),
					windowDimensions.y()-HEART_HEIGHT_WIDTH-2),
					new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH), heartImage);
			gameObjects().addGameObject(hearts[i], Layer.BACKGROUND);
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		double ballHeight = this.ball.getCenter().y();
		String prompt="";

		if (ballHeight >windowDimensions.y() ){
			if (this.num_lives > 0){
				gameObjects().removeGameObject(hearts[--this.num_lives],Layer.BACKGROUND);
				System.out.println(num_lives);
				setVelocityBall();
				numericLife.setString(Integer.toString(num_lives));
				if (num_lives >= 3){
					numericLife.setColor(Color.green);
				}
				if (num_lives==2){
					numericLife.setColor(Color.yellow);
				}
				if (num_lives ==1){
					numericLife.setColor(Color.red);
				}
			}
		}
		if (ballHeight > windowDimensions.y() && this.num_lives == 0) {
			prompt += "You lose! Play again?";
			if (windowController.openYesNoDialog(prompt)){
				restartGame();
			}
			else {
				windowController.closeWindow();
			}
		}

		if (bricksCounter.value() == 0){
			prompt += "You win! Play again?";
			if (windowController.openYesNoDialog(prompt)){
				restartGame();
			}
			else {
				windowController.closeWindow();
			}
		}

		//todo fix the loop
		if (inputListener.isKeyPressed(KeyEvent.VK_W)) {
			prompt += "You win! Play again?";
			if (windowController.openYesNoDialog(prompt)) {
				restartGame();
			} else {
				windowController.closeWindow();
			}
		}
	}


	private void restartGame() {
		Iterable<GameObject> objectsDefault = gameObjects().objectsInLayer(Layer.DEFAULT);
		for (GameObject object:objectsDefault){
			gameObjects().removeGameObject(object);
		}

		Iterable<GameObject> objectsBackground = gameObjects().objectsInLayer(Layer.BACKGROUND);
		for (GameObject object:objectsBackground){
			gameObjects().removeGameObject(object);
		}
		Iterable<GameObject> objectsStatic = gameObjects().objectsInLayer(Layer.STATIC_OBJECTS);
		for (GameObject object:objectsStatic){
			gameObjects().removeGameObject(object);
		}

		// Reset internal state
		num_lives = MAX_STREAKS;

		bricksCounter.reset();

		// Recreate game objects
		initializeGame(imageReader, soundReader, inputListener, windowController);

	}


	private void createBricks(ImageReader imageReader, Vector2 windowDimensions) {
		Renderable brickImage = imageReader.readImage("assets/assets/brick.png", false);

		float len_bricks = windowDimensions.x()-(2*WALLS_WIDTH+2)-(this.num_bricks-1);
		float brick_width = len_bricks/this.num_bricks;
		for (int row = 0; row<this.num_lines; row++){
			float y = row*(50)+50;
			//todo fix the rows and the probability of the special bricks
			for (int col=0; col<this.num_bricks; col++){
				float x = WALLS_WIDTH+ col*(brick_width +ADDED_SPACE);
				if (col==1){
					Vector2 puckLoc = new Vector2(x+(brick_width/2),y);

					GameObject brick = new Brick(new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT)
							,brickImage, new PuckStrategy(imageReader, soundReader, gameObjects(),
							puckLoc, new Vector2(PUCK_SIZE,PUCK_SIZE),BALL_SPEED,
							new BasicCollisionStrategy(this), windowDimensions, this));
					gameObjects().addGameObject(brick, Layer.DEFAULT);
					bricksCounter.increment();
				}
				else {
					GameObject brick = new Brick(new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT)
							, brickImage, new BasicCollisionStrategy(this));
					gameObjects().addGameObject(brick, Layer.DEFAULT);
					bricksCounter.increment();
				}
			}
		}
	}

	private void createBall(ImageReader imageReader, SoundReader soundReader, Vector2 windowDimensions) {
		Renderable ballImage = imageReader.readImage("assets/assets/ball.png", true);
		Sound collisionSound = soundReader.readSound("assets/assets/blop.wav");
		ball =  new Ball(Vector2.ZERO, new Vector2(SIZE_BALL, SIZE_BALL), ballImage,
				collisionSound);
		setVelocityBall();

		gameObjects().addGameObject(ball, Layer.DEFAULT);
	}

	private void setVelocityBall() {
		ball.setCenter(windowDimensions.mult(0.5f));
		float ballVelX = BALL_SPEED;
		float ballVelY = BALL_SPEED;
		Random rand = new Random();
		if (rand.nextBoolean()){
			ballVelX = -1*BALL_SPEED;
		}
		if (rand.nextBoolean()){
			ballVelY = -1*BALL_SPEED;
		}
		ball.setVelocity(new Vector2(ballVelX, ballVelY));
	}


	private void creatingWalls(Vector2 windowDimensions) {
		GameObject leftWall = new GameObject(Vector2.ZERO, new Vector2(WALLS_WIDTH, windowDimensions.y()),
				new RectangleRenderable(Color.BLACK));
		gameObjects().addGameObject(leftWall, Layer.STATIC_OBJECTS);

		GameObject rightWall = new GameObject(new Vector2(windowDimensions.x()-WALLS_WIDTH, 0),
				new Vector2(windowDimensions.x()-5, windowDimensions.y()), new RectangleRenderable(Color.BLACK));
		gameObjects().addGameObject(rightWall, Layer.STATIC_OBJECTS);

		GameObject upWall = new GameObject(Vector2.ZERO, new Vector2(windowDimensions.x(), WALLS_WIDTH),
				new RectangleRenderable(Color.BLACK));
		gameObjects().addGameObject(upWall, Layer.STATIC_OBJECTS);
	}


	public static void main(String[] args){
		BrickerGameManager game = new BrickerGameManager("BOUNCING bALL",
				new Vector2(700,500), args);
		game.run();


	}
}

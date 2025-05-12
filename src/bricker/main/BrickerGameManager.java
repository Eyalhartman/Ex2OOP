package bricker.main;

import bricker.brick_strategies.*;
import bricker.gameobjects.Ball;
import bricker.gameobjects.Brick;
import bricker.gameobjects.Paddle;
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

/**
 * The BrickerGameManager class manages the game state and logic for a simple brick-breaking game.
 * It initializes the game objects, handles user input, and updates the game state.
 * @author Dana and Eyal.
 */
public class BrickerGameManager extends GameManager {

	private final static int SIZE_BALL = 20;
	private static final int PUCK_SIZE = SIZE_BALL * 3 / 4;
	private final static int BALL_SPEED = 150;
	private final static float PADDLE_WIDTH = 100;
	private final static float PADDLE_BRICK_HEIGHT = 15;
	private final static float PADDLE_FROM_EDGE = 30;
	private final static float WALLS_WIDTH = 5;
	private final static int FIRST_ARG = 0;
	private final static int SECOND_ARG = 1;
	private final static int LENGTH_ROW_ARG = 2;
	private final static int DEF_LINES = 7;
	private final static int DEF_BRICKS = 8;
	private final static int ADDED_SPACE = 1;
	private final static int MAX_STREAKS = 4;
	private static final int INITIAL_HEART_COUNT = 3;
	private final static int HEART_HEIGHT_WIDTH = 20;
	private final static int NUMERIC_HEIGHT_WIDTH = 23;
	private final static int SPACE_FROM_HEART = 5;
	private final static int SPACE_FROM_WALL = 5;
	private final static int MAX_BOUND = 11;
	private final static int BASIC_BRICK = 5;
	private final static int EXTRA_BRICK = 6;
	private final static int EXTRA_PADDLE_BRICK = 7;
	private final static int TURBO_BRICK = 8;
	private final static int RETURN_LIFE_BRICK = 9;
	private final static int DOUBLE_BRICK = 10;
	private final static float MULT_BY_HALF = 0.5f;
	private final static int GREEN_LIVES = 3;
	private final static int WIDTH_WINDOW = 700;
	private final static int HEIGHT_WINDOW = 500;

	private final static String LOOSE_STRING = "You lose! Play again?";
	private final static String WIN_STRING = "You win! Play again?";
	private final static String BALL_ADDRESS = "assets/assets/ball.png";
	private final static String PADDLE_ADDRESS = "assets/assets/paddle.png";
	private final static String BRICK_ADDRESS = "assets/assets/brick.png";
	private final static String HEART_ADDRESS = "assets/assets/heart.png";
	private final static String COLLISION_SOUND_ADDRESS = "assets/assets/blop.wav";
	private final static String BACKGROUND_ADDRESS = "assets/assets/DARK_BG2_small.jpeg";
	private final static String IS_INEGER_REGEX = "^-?\\d+$";
	private final static String BRICKER = "Bricker";
	private final static String RED_BALL_ADDRESS = "assets/assets/redball.png";

	private final int numLines;
	private boolean wHandled = false;
	private final int numBricks;
	private Ball ball;
	private Paddle userPaddle;
	private Vector2 windowDimensions;
	private WindowController windowController;
	private int numLives = INITIAL_HEART_COUNT;
	private GameObject[] hearts;
	private TurboModeStrategy turboStrategy;
	private Renderable turboImage;
	private FactoryDoubleStrategy strategyDoubleFactory;

	private final Counter bricksCounter = new Counter();
	private final Counter extraPaddlesCount = new Counter();


	private Renderable heartImage;
	private ImageReader imageReader;
	private SoundReader soundReader;
	private UserInputListener inputListener;
	private TextRenderable numericLife;
	private GameObject numericLifeObject;



	/**
	 * Constructor for the BrickerGameManager class.
	 *
	 * @param windowTitle The title of the game window.
	 * @param windowDimensions The dimensions of the game window.
	 * @param args Command line arguments for the game.
	 */
	public BrickerGameManager(String windowTitle, Vector2 windowDimensions, String[] args){
		super(windowTitle, windowDimensions);
		if (args.length < LENGTH_ROW_ARG ||  !isInteger(args[FIRST_ARG]) || !isInteger(args[SECOND_ARG])) {
			this.numLines = DEF_LINES;
			this.numBricks = DEF_BRICKS;
		} else {
			this.numLines = parseInt(args[FIRST_ARG]);
			this.numBricks = parseInt(args[SECOND_ARG]);
		}
	}

	/**
	 * Initializes the game by creating the game objects and setting up the game state.
	 *
	 * @param imageReader The image reader used to load images.
	 * @param soundReader The sound reader used to load sounds.
	 * @param inputListener The input listener for user input.
	 * @param windowController The window controller for managing the game window.
	 */
	@Override
	public void initializeGame(ImageReader imageReader,
							   SoundReader soundReader,
							   UserInputListener inputListener,
							   WindowController windowController) {
		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.inputListener = inputListener;
		this.windowController = windowController;
		wHandled = false;
		this.turboStrategy = null;
		super.initializeGame(imageReader, soundReader, inputListener, windowController);
		windowDimensions = windowController.getWindowDimensions();

		createBackground(imageReader);
		createBall(imageReader, soundReader, windowDimensions);
		createPaddle(imageReader, inputListener);
		creatingWalls(windowDimensions);
		createHearts(imageReader);
		createNumeric();

		turboImage = imageReader.readImage(RED_BALL_ADDRESS, false);
		strategyDoubleFactory = new FactoryDoubleStrategy(imageReader, soundReader,
				gameObjects(), new Vector2(PUCK_SIZE, PUCK_SIZE), BALL_SPEED,
				new BasicCollisionStrategy(this),
				windowDimensions, this, ball, turboImage,
				new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT), inputListener,
				(Paddle)userPaddle, heartImage,
				new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH));
		createBricks(imageReader, windowDimensions);
	}

	/**
	 * Updates the game state and checks for win/lose conditions.
	 *
	 * @param deltaTime The time since the last update in seconds.
	 */
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		double ballHeight = this.ball.getCenter().y();
		if (turboStrategy != null) {
			turboStrategy.update(deltaTime);
		}
		if (ballHeight >windowDimensions.y() ){
			if (this.numLives > 0){
				hearts[numLives - 1].renderer().setRenderable(null);
				numLives--;
				setVelocityBall();
				Renderable defaultBall = imageReader.readImage(BALL_ADDRESS, true);
				ball.renderer().setRenderable(defaultBall);
				updateLifeDisplay();
			}
		}
		if (ballHeight > windowDimensions.y() && this.numLives == 0) {
			if (windowController.openYesNoDialog(LOOSE_STRING)) {
				restartGame();
			} else {
				windowController.closeWindow();
			}
		}
		if (bricksCounter.value() == 0) {
			if (windowController.openYesNoDialog(WIN_STRING)) {
				restartGame();
			} else {
				windowController.closeWindow();
			}
		}
		if (inputListener.wasKeyPressedThisFrame(KeyEvent.VK_W )&& !wHandled ) {
			wHandled = true;
			boolean yes = windowController.openYesNoDialog(WIN_STRING);
			if (yes) restartGame();
			else    windowController.closeWindow();
		}
	}


	/**
	 * Decreases the counter of bricks by one.
	 */
	public void decrementCounter() {
		bricksCounter.decrement();
	}

	/**
	 * Returns the number of extra paddles.
	 *
	 * @return The number of extra paddles.
	 * */
	public int getExtraPaddlesCount() {
		return extraPaddlesCount.value();
	}

	/**
	 * Increases the number of extra paddles by one.
	 */
	public void incrementExtraPaddles() {
		extraPaddlesCount.increment();
	}

	/**
	 * Decreases the number of extra paddles by one.
	 */
	public void decrementExtraPaddles() {
		extraPaddlesCount.decrement();
	}

	/**
	 * Decreases the number of lives by one and updates the life display.
	 * If the number of lives is greater than 0, it removes the heart image from the display.
	 */
	public void incrementLives() {
		if (numLives < MAX_STREAKS) {
			hearts[numLives].renderer().setRenderable(heartImage);
			numLives++;
			updateLifeDisplay();
		}
	}

	/**
	 * Gets the game objects in the game. And then removes the game object from the game.
	 * @param object The game object to be removed.
	 * @return The game objects in the game.
	 */
	public boolean removeGameObject(GameObject object) {
		return gameObjects().removeGameObject(object);
	}

	/**
	 * Restarts the game by removing all game objects and resetting the game state.
	 */
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
		numLives = INITIAL_HEART_COUNT;
		bricksCounter.reset();
		extraPaddlesCount.reset();

		// Recreate game objects
		initializeGame(imageReader, soundReader, inputListener, windowController);

		turboStrategy = null;


	}

	/**
	 * Creates a numeric display for the number of lives remaining.
	 */
	private void createNumeric() {
		numericLife = new TextRenderable(Integer.toString(numLives));
		if (numLives >= INITIAL_HEART_COUNT){
			numericLife.setColor(Color.green);
		}
		numericLifeObject = new GameObject(new Vector2(2 * WALLS_WIDTH,
				windowDimensions.y() - HEART_HEIGHT_WIDTH - NUMERIC_HEIGHT_WIDTH - SPACE_FROM_HEART),
				new Vector2(NUMERIC_HEIGHT_WIDTH, NUMERIC_HEIGHT_WIDTH), numericLife);
		gameObjects().addGameObject(numericLifeObject, Layer.BACKGROUND);
	}

	/**
	 * Creates the background of the game window.
	 *
	 * @param imageReader The image reader used to load the background image.
	 */
	private void createBackground(ImageReader imageReader) {
		Renderable backgroundImage = imageReader.readImage(BACKGROUND_ADDRESS, true);
		GameObject background = new GameObject(Vector2.ZERO, new Vector2(windowDimensions.x(),
				windowDimensions.y()), backgroundImage);
		gameObjects().addGameObject(background, Layer.BACKGROUND);
		background.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
	}

	/**
	 * Creates the paddle for the user.
	 *
	 * @param imageReader The image reader used to load the paddle image.
	 * @param inputListener The input listener for user input.
	 */
	private void createPaddle(ImageReader imageReader, UserInputListener inputListener) {
		Renderable paddleImage = imageReader.readImage(PADDLE_ADDRESS, true);
		GameObject userPaddle =  new Paddle(Vector2.ZERO,
				new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT),
				paddleImage, inputListener, windowDimensions);
		userPaddle.setCenter(new Vector2(windowDimensions.x()/2, windowDimensions.y()-PADDLE_FROM_EDGE));
		gameObjects().addGameObject(userPaddle, Layer.DEFAULT);
		this.userPaddle = (Paddle)userPaddle;
	}

	/**
	 * Creates the hearts that represent the player's lives.
	 *
	 * @param imageReader The image reader used to load the heart image.
	 */
	private void createHearts(ImageReader imageReader) {
		this.heartImage = imageReader.readImage(HEART_ADDRESS, true);
		float lenHeart = HEART_HEIGHT_WIDTH+1;
		hearts = new GameObject[MAX_STREAKS];
		for(int i=0; i<MAX_STREAKS; i++){
			hearts[i] = new GameObject(new Vector2(2*WALLS_WIDTH+(i*lenHeart),
					windowDimensions.y()-HEART_HEIGHT_WIDTH-2),
					new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH), heartImage);
			if (i == INITIAL_HEART_COUNT)
				hearts[i].renderer().setRenderable(null);
			gameObjects().addGameObject(hearts[i], Layer.BACKGROUND);

		}
		numLives = INITIAL_HEART_COUNT;
	}

	/**
	 * Creates the bricks in the game.
	 *
	 * @param imageReader The image reader used to load the brick image.
	 * @param windowDimensions The dimensions of the game window.
	 */
	private void createBricks(ImageReader imageReader, Vector2 windowDimensions) {
		Renderable brickImage = imageReader.readImage(BRICK_ADDRESS, false);
		Renderable heartImage = imageReader.readImage(HEART_ADDRESS, true);
		Vector2 heartDimensions = new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH);
		Random random = new Random();
		float verticalSpacing = 2;
		float lenBricks = windowDimensions.x()-(2*WALLS_WIDTH+2)-(this.numBricks -1);
		float brickWidth = lenBricks/this.numBricks;
		for (int row = 0; row<this.numLines; row++){
			float y = WALLS_WIDTH + row * (PADDLE_BRICK_HEIGHT + verticalSpacing);
			for (int col = 0; col<this.numBricks; col++){
				int chooseBehavior = random.nextInt(1,MAX_BOUND);
				GameObject brick = null;
				float x = WALLS_WIDTH+ col*(brickWidth +ADDED_SPACE);
				if (1<= chooseBehavior && chooseBehavior <= BASIC_BRICK){
					brick = new Brick(new Vector2(x, y),
							new Vector2(brickWidth, PADDLE_BRICK_HEIGHT)
							, brickImage, new BasicCollisionStrategy(this));
				} else if (chooseBehavior == EXTRA_BRICK){
					brick = extraBallBrick(imageReader, windowDimensions, x, y, brickWidth, brickImage);
				} else if (chooseBehavior == EXTRA_PADDLE_BRICK) {
					brick = extraPaddleBrick(imageReader, windowDimensions, x, y, brickWidth, brickImage);
				} else if (chooseBehavior == TURBO_BRICK){
					brick = turboBrick(imageReader, x, y, brickWidth, brickImage);
				} else if(chooseBehavior == RETURN_LIFE_BRICK){
					brick = returnLifeBrick(windowDimensions, heartImage, heartDimensions, x, y,
							brickWidth, brickImage);
				} else if (chooseBehavior == DOUBLE_BRICK) {
					brick = doubleBrick(x, y, brickWidth, brickImage);
				}
				gameObjects().addGameObject(brick, Layer.DEFAULT);
				bricksCounter.increment();
			}
		}
	}

	/**
	 * Creates a double brick object.
	 *
	 * @param x The x-coordinate of the brick's position.
	 * @param y The y-coordinate of the brick's position.
	 * @param brickWidth The width of the brick.
	 * @param brickImage The image to be used for rendering the brick.
	 * @return A GameObject representing the double brick.
	 */
	private GameObject doubleBrick(float x, float y, float brickWidth, Renderable brickImage) {
		GameObject brick;
		CollisionStrategy strategyDouble = strategyDoubleFactory.buildDoubleStrategy();
		brick = new Brick(
				new Vector2(x, y),
				new Vector2(brickWidth, PADDLE_BRICK_HEIGHT), brickImage, strategyDouble
		);
		return brick;
	}

	/**
	 * Creates a brick that returns a life to the player.
	 *
	 * @param windowDimensions The dimensions of the game window.
	 * @param heartImage The image to be used for rendering the heart.
	 * @param heartDimensions The dimensions of the heart image.
	 * @param x The x-coordinate of the brick's position.
	 * @param y The y-coordinate of the brick's position.
	 * @param brickWidth The width of the brick.
	 * @param brickImage The image to be used for rendering the brick.
	 * @return A GameObject representing the life-returning brick.
	 */
	private GameObject returnLifeBrick(Vector2 windowDimensions, Renderable heartImage,
									   Vector2 heartDimensions,
									   float x, float y, float brickWidth,
									   Renderable brickImage) {
		GameObject brick;
		CollisionStrategy heartStrat = new ReturnStreakStrategy(
				new BasicCollisionStrategy(this),
				gameObjects(),
				windowDimensions,
				(Paddle)userPaddle,
				heartImage,
				heartDimensions,
				this
		);
		brick = new Brick(
				new Vector2(x, y),
				new Vector2(brickWidth, PADDLE_BRICK_HEIGHT),
				brickImage,
				heartStrat
		);
		return brick;
	}

	/**
	 * Creates a turbo brick object.
	 *
	 * @param imageReader The image reader used to load the brick image.
	 * @param x The x-coordinate of the brick's position.
	 * @param y The y-coordinate of the brick's position.
	 * @param brickWidth The width of the brick.
	 * @param brickImage The image to be used for rendering the brick.
	 * @return A GameObject representing the turbo brick.
	 */
	private GameObject turboBrick(ImageReader imageReader, float x, float y,
								  float brickWidth, Renderable brickImage) {
		GameObject brick;
		Renderable turboImage = imageReader.readImage(RED_BALL_ADDRESS, false);
		this.turboStrategy = new TurboModeStrategy(ball, new BasicCollisionStrategy(this),
				turboImage);
		brick = new Brick(new Vector2(x, y),
				new Vector2(brickWidth, PADDLE_BRICK_HEIGHT)
				, brickImage, turboStrategy);
		return brick;
	}

	/**
	 * Creates a brick that gives the player an extra paddle.
	 *
	 * @param imageReader The image reader used to load the brick image.
	 * @param windowDimensions The dimensions of the game window.
	 * @param x The x-coordinate of the brick's position.
	 * @param y The y-coordinate of the brick's position.
	 * @param brickWidth The width of the brick.
	 * @param brickImage The image to be used for rendering the brick.
	 * @return A GameObject representing the extra paddle brick.
	 */
	private GameObject extraPaddleBrick(ImageReader imageReader, Vector2 windowDimensions,
										float x, float y, float brickWidth, Renderable brickImage) {
		GameObject brick;
		brick = new Brick(new Vector2(x, y),
			   new Vector2(brickWidth, PADDLE_BRICK_HEIGHT)
			   , brickImage, new ExtraPaddleStrategy(this,
			   new BasicCollisionStrategy(this),
				gameObjects(),
				imageReader,
				inputListener,
				windowDimensions,
				new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT)));
		return brick;
	}

	/**
	 * Creates a brick that gives the player an extra ball.
	 *
	 * @param imageReader The image reader used to load the brick image.
	 * @param windowDimensions The dimensions of the game window.
	 * @param x The x-coordinate of the brick's position.
	 * @param y The y-coordinate of the brick's position.
	 * @param brickWidth The width of the brick.
	 * @param brickImage The image to be used for rendering the brick.
	 * @return A GameObject representing the extra ball brick.
	 */
	private GameObject extraBallBrick(ImageReader imageReader, Vector2 windowDimensions, float x, float y,
									  float brickWidth, Renderable brickImage) {
		GameObject brick;
		brick = new Brick(new Vector2(x, y),
				new Vector2(brickWidth, PADDLE_BRICK_HEIGHT)
				, brickImage, new ExtraBallsStrategy(imageReader,
				soundReader,
				gameObjects(),
				new Vector2(PUCK_SIZE,PUCK_SIZE),
				BALL_SPEED,
				new BasicCollisionStrategy(this),
				windowDimensions,
				this));
		return brick;
	}

	/**
	 * Creates a ball object and adds it to the game.
	 *
	 * @param imageReader The image reader used to load the ball image.
	 * @param soundReader The sound reader used to load the collision sound.
	 * @param windowDimensions The dimensions of the game window.
	 */
	private void createBall(ImageReader imageReader, SoundReader soundReader, Vector2 windowDimensions) {
		Renderable ballImage = imageReader.readImage(BALL_ADDRESS, true);
		Sound collisionSound = soundReader.readSound(COLLISION_SOUND_ADDRESS);
		ball =  new Ball(Vector2.ZERO, new Vector2(SIZE_BALL, SIZE_BALL), ballImage,
				collisionSound);
		setVelocityBall();
		gameObjects().addGameObject(ball, Layer.DEFAULT);
	}

	/**
	 * Creates the walls of the game window.
	 *
	 * @param windowDimensions The dimensions of the game window.
	 */
	private void creatingWalls(Vector2 windowDimensions) {
		GameObject leftWall = new GameObject(Vector2.ZERO, new Vector2(WALLS_WIDTH, windowDimensions.y()),
				new RectangleRenderable(Color.BLACK));
		gameObjects().addGameObject(leftWall, Layer.STATIC_OBJECTS);

		GameObject rightWall = new GameObject(new Vector2(windowDimensions.x()-WALLS_WIDTH, 0),
				new Vector2(windowDimensions.x()-SPACE_FROM_WALL, windowDimensions.y()),
				new RectangleRenderable(Color.BLACK));
		gameObjects().addGameObject(rightWall, Layer.STATIC_OBJECTS);

		GameObject upWall = new GameObject(Vector2.ZERO, new Vector2(windowDimensions.x(), WALLS_WIDTH),
				new RectangleRenderable(Color.BLACK));
		gameObjects().addGameObject(upWall, Layer.STATIC_OBJECTS);
	}

	/**
	 * Sets the velocity of the ball to a random direction.
	 */
	private void setVelocityBall() {
		ball.setCenter(windowDimensions.mult(MULT_BY_HALF));
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

	/**
	 * Checks if the given string is a valid integer.
	 *
	 * @param s The string to check.
	 * @return true if the string is a valid integer, false otherwise.
	 */
	private static boolean isInteger(String s) {
		return s != null && s.matches(IS_INEGER_REGEX);
	}

	/** Updates the color of the numeric life display based on the number of lives.
	 * If the number of lives is 3 or more, the color is green.
	 * If the number of lives is 2, the color is yellow.
	 * If the number of lives is 1 or less, the color is red.
	 */
	private void updateLifeDisplay() {
		numericLife.setString(Integer.toString(numLives));
		if (numLives >= GREEN_LIVES) numericLife.setColor(Color.green);
		else if (numLives == 2) numericLife.setColor(Color.yellow);
		else                numericLife.setColor(Color.red);
	}

	/**
	 * The main method to run the game.
	 *
	 * @param args Command line arguments for the game.
	 */
	public static void main(String[] args){
		BrickerGameManager game = new BrickerGameManager(BRICKER,
				new Vector2(WIDTH_WINDOW,HEIGHT_WINDOW), args);
		game.run();


	}
}

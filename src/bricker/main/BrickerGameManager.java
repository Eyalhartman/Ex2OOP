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
	private final static int MAX_STREAKS = 4;
	private static final int INITIAL_HEART_COUNT = 3;
	private final static int HEART_HEIGHT_WIDTH = 20;
	private final static int NUMERIC_HEIGHT_WIDTH = 23;


	private final int num_lines;
	private boolean wHandled = false;
	private final int num_bricks;
	private Ball ball;
	private Paddle userPaddle;
	private Vector2 windowDimensions;
	private WindowController windowController;
	private int num_lives = INITIAL_HEART_COUNT;
	private GameObject[] hearts;
	private TurboModeStrategy turboStrategy;
	private Renderable turboImage;
	private Vector2 puckLoc;
	private FactoryDoubleStrategy strategyDoubleFactory;

	private Counter bricksCounter = new Counter();
	private final Counter extraPaddlesCount = new Counter();


	private Renderable heartImage;
	private ImageReader imageReader;
	private SoundReader soundReader;
	private UserInputListener inputListener;
	private TextRenderable numericLife;
	private GameObject numericLifeObject;





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
	public void initializeGame(ImageReader imageReader,
							   SoundReader soundReader,
							   UserInputListener inputListener,
							   WindowController windowController) {

		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.inputListener = inputListener;

		this.windowController = windowController;
		wHandled = false;

		super.initializeGame(imageReader, soundReader, inputListener, windowController);
		windowDimensions = windowController.getWindowDimensions();

		strategyDoubleFactory = new FactoryDoubleStrategy(
				imageReader,
				soundReader,
				gameObjects(),
				puckLoc, // can be any placeholder Vector2 (used by ExtraBallsStrategy)
				new Vector2(PUCK_SIZE, PUCK_SIZE),
				BALL_SPEED,
				new BasicCollisionStrategy(this),
				windowDimensions,
				this,
				ball,
				turboImage,
				new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT),
				inputListener,
				(Paddle)userPaddle,
				heartImage,
				new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH));

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
		gameObjects().addGameObject(numericLifeObject, Layer.BACKGROUND);
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
		GameObject userPaddle =  new Paddle(Vector2.ZERO,
				new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT),
				paddleImage, inputListener, windowDimensions);
		userPaddle.setCenter(new Vector2(windowDimensions.x()/2, windowDimensions.y()-PADDLE_FROM_EDGE));
		gameObjects().addGameObject(userPaddle, Layer.DEFAULT);
		this.userPaddle = (Paddle)userPaddle;
	}

	private void createHearts(ImageReader imageReader) {
		this.heartImage = imageReader.readImage("assets/assets/heart.png", true);
		float len_heart = HEART_HEIGHT_WIDTH+1;
		hearts = new GameObject[MAX_STREAKS];
		for(int i=0; i<MAX_STREAKS; i++){
			hearts[i] = new GameObject(new Vector2(2*WALLS_WIDTH+(i*len_heart),
					windowDimensions.y()-HEART_HEIGHT_WIDTH-2),
					new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH), heartImage);
			if (i == INITIAL_HEART_COUNT)
				hearts[i].renderer().setRenderable(null);
			gameObjects().addGameObject(hearts[i], Layer.BACKGROUND);

		}
		num_lives = INITIAL_HEART_COUNT;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		double ballHeight = this.ball.getCenter().y();
		String prompt="";
		turboStrategy.update(deltaTime);


		if (ballHeight >windowDimensions.y() ){
			if (this.num_lives > 0){
				hearts[num_lives - 1].renderer().setRenderable(null);
				num_lives--;
				setVelocityBall();
				updateLifeDisplay();
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

		if (inputListener.wasKeyPressedThisFrame(KeyEvent.VK_W )&& !wHandled ) {
			wHandled = true;
			boolean yes = windowController.openYesNoDialog("You win! Play again?");
			if (yes) restartGame();
			else    windowController.closeWindow();
			return;
			}
		if (inputListener.wasKeyReleasedThisFrame(KeyEvent.VK_W ) ) {
			wHandled = true;
			boolean yes = windowController.openYesNoDialog("You win! Play again?");
			if (yes) restartGame();
			else    windowController.closeWindow();
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
		num_lives = INITIAL_HEART_COUNT;

		bricksCounter.reset();
		extraPaddlesCount.reset();

		// Recreate game objects
		initializeGame(imageReader, soundReader, inputListener, windowController);
	}

	private void createBricks(ImageReader imageReader, Vector2 windowDimensions) {
		Renderable brickImage = imageReader.readImage("assets/assets/brick.png", false);
		Renderable heartImage = imageReader.readImage("assets/assets/heart.png", true);
		Vector2 heartDimensions = new Vector2(HEART_HEIGHT_WIDTH, HEART_HEIGHT_WIDTH);
		Random random = new Random();
		float verticalSpacing = 2;

		float len_bricks = windowDimensions.x()-(2*WALLS_WIDTH+2)-(this.num_bricks-1);
		float brick_width = len_bricks/this.num_bricks;
		for (int row = 0; row<this.num_lines; row++){
			float y = WALLS_WIDTH + row * (PADDLE_BRICK_HEIGHT + verticalSpacing);
			for (int col=0; col<this.num_bricks; col++){

				int choose_behavior = random.nextInt(1,11);
				GameObject brick = null;

				float x = WALLS_WIDTH+ col*(brick_width +ADDED_SPACE);
				if (1<= choose_behavior && choose_behavior<=5){
					brick = new Brick(new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT)
							, brickImage, new BasicCollisionStrategy(this));
				}
				else if (choose_behavior == 6){
					puckLoc = new Vector2(x+(brick_width/2),y);
					brick = new Brick(new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT)
							,brickImage, new ExtraBallsStrategy(imageReader,
							soundReader,
							gameObjects(),
							puckLoc,
							new Vector2(PUCK_SIZE,PUCK_SIZE),
							BALL_SPEED,
							new BasicCollisionStrategy(this),
							windowDimensions,
							this));

				} else if (choose_behavior == 7) {
					 brick = new Brick(new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT)
							, brickImage, new ExtraPaddleStrategy(this,
							new BasicCollisionStrategy(this),
							 gameObjects(),
							 imageReader,
							 inputListener,
							 windowDimensions,
							new Vector2(PADDLE_WIDTH, PADDLE_BRICK_HEIGHT)));
				}

				else if (choose_behavior == 8){
					Renderable turboImage = imageReader.readImage("assets/assets/redball.png", false);
					this.turboStrategy = new TurboModeStrategy(ball, new BasicCollisionStrategy(this),
							turboImage);

					brick = new Brick(new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT)
							, brickImage, turboStrategy);
				}


				else if(choose_behavior == 9){
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
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT),
							brickImage,
							heartStrat
					);
				}


				else if (choose_behavior == 10) {
					puckLoc = new Vector2(x+(brick_width/2),y);
					CollisionStrategy strategyDouble = strategyDoubleFactory.buildStrategy(0);

					brick = new Brick(
							new Vector2(x, y),
							new Vector2(brick_width, PADDLE_BRICK_HEIGHT), brickImage, strategyDouble
					);

				}
				gameObjects().addGameObject(brick, Layer.DEFAULT);
				bricksCounter.increment();
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

	public int getExtraPaddlesCount() { return extraPaddlesCount.value(); }
	public void incrementExtraPaddles() { extraPaddlesCount.increment(); }
	public void decrementExtraPaddles() { extraPaddlesCount.decrement(); }

	/**
	 * Decreases the number of lives by one and updates the life display.
	 * If the number of lives is greater than 0, it removes the heart image from the display.
	 */
	public void incrementLives() {
		if (num_lives < MAX_STREAKS) {
			hearts[num_lives].renderer().setRenderable(heartImage);
			num_lives++;
			updateLifeDisplay();
		}
	}


	/** Updates the color of the numeric life display based on the number of lives.
	 * If the number of lives is 3 or more, the color is green.
	 * If the number of lives is 2, the color is yellow.
	 * If the number of lives is 1 or less, the color is red.
	 */
	private void updateLifeDisplay() {
		numericLife.setString(Integer.toString(num_lives));
		if (num_lives >= 3) numericLife.setColor(Color.green);
		else if (num_lives == 2) numericLife.setColor(Color.yellow);
		else                numericLife.setColor(Color.red);
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

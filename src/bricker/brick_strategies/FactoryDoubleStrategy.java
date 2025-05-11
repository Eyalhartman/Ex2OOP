package bricker.brick_strategies;

import bricker.gameobjects.Ball;
import bricker.gameobjects.Paddle;
import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.util.Random;

/**
 * Factory class responsible for creating random {@link CollisionStrategy} instances,
 * including both single and double (composite) strategies.
 * <p>
 * The double strategies are constructed recursively and avoid excessive nesting
 * by limiting depth to 1.
 * </p>
 */
public class FactoryDoubleStrategy {
	private static final int EXTRA_BALLS_STRG = 1;
	private static final int EXTRA_PADDLE_STRG = 2;
	private static final int TURBO_MODE_STRG = 3;
	private static final int RETURN_STREAK_STRG = 4;
	private static final int DOUBLE_STRG = 5;
	private static final int UPPER_BOUND = 6;
	private static final int INCREACMENT = 1;

	private final ImageReader imageReader;
	private final SoundReader soundReader;
	private final GameObjectCollection gameObjectCollection;
	private final Vector2 puckLoc;
	private final Vector2 puckSize;
	private final int speed;
	private final BasicCollisionStrategy basicCollisionStrategy;
	private final Vector2 windowDimensions;
	private final BrickerGameManager brickerGameManage;
	private final Ball ball;
	private final Renderable turboModeRenderable;
	private final Vector2 paddleDimensions;
	private final UserInputListener inputListener;
	private final Paddle originalPaddle;
	private final Renderable heartImage;
	private final Vector2 heartDimensions;


	private final Random random = new Random();

	/**
	 * Constructs the strategy factory with all dependencies required to generate any type
	 * of {@link CollisionStrategy} used in the game.
	 *
	 * @param imageReader         Used to load graphical assets.
	 * @param soundReader         Used to load sound effects.
	 * @param gameObjectCollection Reference to the game's global object manager.
	 * @param puckLoc             Initial position for spawning extra pucks.
	 * @param puckSize            Size of puck objects.
	 * @param speed               Speed value for moving objects like pucks.
	 * @param basicCollisionStrategy The default strategy to wrap when needed.
	 * @param windowDimensions    Dimensions of the game window.
	 * @param brickerGameManage   Reference to the game manager (score/lives etc.).
	 * @param ball                Main game ball, used by some strategies.
	 * @param turboModeRenderable Renderable to display for turbo ball mode.
	 * @param paddleDimensions    Size of the paddle.
	 * @param inputListener       For user paddle input (mouse/keyboard).
	 * @param originalPaddle      The main game paddle.
	 * @param heartImage          Renderable used for extra life (heart) display.
	 * @param heartDimensions     Size of the heart renderable.
	 */
	public FactoryDoubleStrategy(ImageReader imageReader,
								 SoundReader soundReader,
								 GameObjectCollection gameObjectCollection,
								 Vector2 puckLoc, Vector2 puckSize,
								 int speed,
								 BasicCollisionStrategy basicCollisionStrategy,
								 Vector2 windowDimensions,
								 BrickerGameManager brickerGameManage,
								 Ball ball,
								 Renderable turboModeRenderable,
								 Vector2 paddleDimensions,
								 UserInputListener inputListener,
								 Paddle originalPaddle, Renderable heartImage, Vector2 heartDimensions) {

		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.gameObjectCollection = gameObjectCollection;
		this.puckLoc = puckLoc;
		this.puckSize = puckSize;
		this.speed = speed;
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.windowDimensions = windowDimensions;
		this.brickerGameManage = brickerGameManage;
		this.ball = ball;
		this.turboModeRenderable = turboModeRenderable;
		this.paddleDimensions = paddleDimensions;
		this.inputListener = inputListener;
		this.originalPaddle = originalPaddle;
		this.heartImage = heartImage;
		this.heartDimensions = heartDimensions;
	}

	/**
	 * Builds a randomly selected {@link CollisionStrategy}. May return a composite
	 * (double) strategy with two distinct strategies combined.
	 *
	 * @param currentDepth The current depth of nested double strategies.
	 * @return A strategy object implementing desired behavior.
	 */
	public CollisionStrategy buildStrategy(int currentDepth) {
		int strategyNumber = random.nextInt(EXTRA_BALLS_STRG, UPPER_BOUND); // 1 to 5
		return strategiesFactory(strategyNumber, currentDepth);
	}

	/**
	 * Internal factory method to construct specific strategies based on the given type number.
	 *
	 * @param numStrategy   The strategy type number (1–5).
	 * @param currentDepth  The current recursion level (used to limit DoubleStrategy depth).
	 * @return A corresponding CollisionStrategy object.
	 */
	public CollisionStrategy strategiesFactory(int numStrategy, int currentDepth) {
		if (numStrategy == EXTRA_BALLS_STRG) {
			return new ExtraBallsStrategy(imageReader, soundReader, gameObjectCollection,
					puckLoc, puckSize, speed, basicCollisionStrategy,
					windowDimensions, brickerGameManage);
		}
		if (numStrategy == EXTRA_PADDLE_STRG) {
			return new ExtraPaddleStrategy(brickerGameManage, basicCollisionStrategy, gameObjectCollection,
					imageReader, inputListener, windowDimensions, paddleDimensions);
		}
		if (numStrategy == TURBO_MODE_STRG) {
			return new TurboModeStrategy(ball, basicCollisionStrategy, turboModeRenderable);
		}
		if (numStrategy == RETURN_STREAK_STRG) {
			return new ReturnStreakStrategy(basicCollisionStrategy, gameObjectCollection,
					windowDimensions, originalPaddle, heartImage, heartDimensions, brickerGameManage);
		}
		if (numStrategy == DOUBLE_STRG) {
			if (currentDepth >= 1) {
				// Prevent recursive nesting of double strategies
				int fallback = random.nextInt(EXTRA_BALLS_STRG, DOUBLE_STRG); // 1–4
				return strategiesFactory(fallback, currentDepth);
			}
			// Create composite of two random strategies
			int s1 = random.nextInt(EXTRA_BALLS_STRG, UPPER_BOUND);
			int s2 = random.nextInt(EXTRA_BALLS_STRG, UPPER_BOUND);
			CollisionStrategy strat1 = strategiesFactory(s1, currentDepth + INCREACMENT);
			CollisionStrategy strat2 = strategiesFactory(s2, currentDepth + INCREACMENT);
			return new DoubleStrategy(strat1, strat2);
		}

		return null; // Should never occur
	}
}

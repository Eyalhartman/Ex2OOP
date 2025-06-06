package bricker.brick_strategies;

import bricker.gameobjects.Ball;
import bricker.gameobjects.Paddle;
import bricker.main.BrickerGameManager;
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
 * by limiting depth to a maximum of 2 levels.
 * </p>
 * @author Eyal Hartman, Dana Weitzhandler
 */
public class FactoryDoubleStrategy {

	private static final int STRATEGY_EXTRA_BALLS = 1;
	private static final int STRATEGY_EXTRA_PADDLE = 2;
	private static final int STRATEGY_TURBO_MODE = 3;
	private static final int STRATEGY_RETURN_STREAK = 4;
	private static final int STRATEGY_DOUBLE = 5;
	private static final int STRATEGY_UPPER_BOUND = 6; // exclusive bound for nextInt
	private static final int INITIAL_RECURSION_DEPTH = 0;
	private static final int MAX_RECURSION_DEPTH = 2;
	private static final int INCREMENT = 1;

	private final ImageReader imageReader;
	private final SoundReader soundReader;
	private final GameObjectCollection gameObjectCollection;
	private final Vector2 puckSize;
	private final int speed;
	private final BasicCollisionStrategy basicCollisionStrategy;
	private final Vector2 windowDimensions;
	private final BrickerGameManager brickerGameManager;
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
	 * @param imageReader           Reader for loading images.
	 * @param soundReader           Reader for loading sounds.
	 * @param gameObjectCollection  Collection to manage game objects.
	 * @param puckSize              Size of the pucks to spawn.
	 * @param speed                 Speed of the spawned pucks.
	 * @param basicCollisionStrategy Base collision behavior strategy.
	 * @param windowDimensions      Dimensions of the game window.
	 * @param brickerGameManager     Reference to the game manager.
	 * @param ball                  The main ball object.
	 * @param turboModeRenderable   Visual representation for turbo mode.
	 * @param paddleDimensions      Dimensions of the paddle.
	 * @param inputListener         Handles user input (for paddle control).
	 * @param originalPaddle        The player's original paddle.
	 * @param heartImage            Image used for life display.
	 * @param heartDimensions       Size of heart icons for UI.
	 */
	public FactoryDoubleStrategy(ImageReader imageReader,
								 SoundReader soundReader,
								 GameObjectCollection gameObjectCollection,
								 Vector2 puckSize,
								 int speed,
								 BasicCollisionStrategy basicCollisionStrategy,
								 Vector2 windowDimensions,
								 BrickerGameManager brickerGameManager,
								 Ball ball,
								 Renderable turboModeRenderable,
								 Vector2 paddleDimensions,
								 UserInputListener inputListener,
								 Paddle originalPaddle,
								 Renderable heartImage,
								 Vector2 heartDimensions) {

		this.imageReader = imageReader;
		this.soundReader = soundReader;
		this.gameObjectCollection = gameObjectCollection;
		this.puckSize = puckSize;
		this.speed = speed;
		this.basicCollisionStrategy = basicCollisionStrategy;
		this.windowDimensions = windowDimensions;
		this.brickerGameManager = brickerGameManager;
		this.ball = ball;
		this.turboModeRenderable = turboModeRenderable;
		this.paddleDimensions = paddleDimensions;
		this.inputListener = inputListener;
		this.originalPaddle = originalPaddle;
		this.heartImage = heartImage;
		this.heartDimensions = heartDimensions;
	}

	/**
	 * Builds a double strategy as the root strategy (with depth 0).
	 *
	 * @return A composed double {@link CollisionStrategy}.
	 */
	public CollisionStrategy buildDoubleStrategy() {
		return strategiesFactory(STRATEGY_DOUBLE, INITIAL_RECURSION_DEPTH);
	}

	/**
	 * Internal factory method to construct specific strategies based on the given type number.
	 *
	 * @param numStrategy  The strategy type number (1–5).
	 * @param currentDepth The current recursion level (used to limit DoubleStrategy depth).
	 * @return A corresponding CollisionStrategy object.
	 */
	public CollisionStrategy strategiesFactory(int numStrategy, int currentDepth) {
		if (numStrategy == STRATEGY_EXTRA_BALLS) {
			return new ExtraBallsStrategy(
					imageReader, soundReader,
					gameObjectCollection, puckSize,
					speed, basicCollisionStrategy,
					windowDimensions, brickerGameManager
			);
		}
		if (numStrategy == STRATEGY_EXTRA_PADDLE) {
			return new ExtraPaddleStrategy(
					brickerGameManager, basicCollisionStrategy,
					gameObjectCollection, imageReader,
					inputListener, windowDimensions, paddleDimensions
			);
		}
		if (numStrategy == STRATEGY_TURBO_MODE) {
			return new TurboModeStrategy(
					ball, basicCollisionStrategy, turboModeRenderable
			);
		}
		if (numStrategy == STRATEGY_RETURN_STREAK) {
			return new ReturnStreakStrategy(
					basicCollisionStrategy, gameObjectCollection,
					windowDimensions, originalPaddle,
					heartImage, heartDimensions, brickerGameManager
			);
		}
		if (numStrategy == STRATEGY_DOUBLE) {
			return getDoubleStrategy(currentDepth);
		}

		return null; // should never occur
	}

	/**
	 * Generates a DoubleStrategy composed of two random sub-strategies.
	 * Limits recursion depth to avoid nested DoubleStrategies beyond MAX_RECURSION_DEPTH.
	 *
	 * @param currentDepth The current recursion level in the strategy tree.
	 * @return A new DoubleStrategy or fallback strategy based on recursion depth.
	 */
	private CollisionStrategy getDoubleStrategy(int currentDepth) {
		if (currentDepth >= MAX_RECURSION_DEPTH) {
			int fallback = random.nextInt(STRATEGY_EXTRA_BALLS, STRATEGY_DOUBLE); // [1–4]
			return strategiesFactory(fallback, currentDepth);
		}
		int s1 = random.nextInt(STRATEGY_EXTRA_BALLS, STRATEGY_UPPER_BOUND); // [1–5]
		int s2 = random.nextInt(STRATEGY_EXTRA_BALLS, STRATEGY_UPPER_BOUND);

		if (s1 == STRATEGY_DOUBLE && s2 == STRATEGY_DOUBLE) {
			s2 = random.nextInt(STRATEGY_EXTRA_BALLS, STRATEGY_DOUBLE); // force single
		}

		CollisionStrategy strat1 = strategiesFactory(s1, currentDepth + INCREMENT);
		CollisionStrategy strat2 = strategiesFactory(s2, currentDepth + INCREMENT);
		return new DoubleStrategy(strat1, strat2);
	}
}

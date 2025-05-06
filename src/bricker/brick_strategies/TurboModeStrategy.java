package bricker.brick_strategies;

import bricker.gameobjects.Ball;
import bricker.gameobjects.PuckBall;
import danogl.GameObject;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class TurboModeStrategy implements CollisionStrategy{
	private static final int NUM_COLLISIONS = 6;
	private final Ball ball;
	private int collisionCounter;
	private BasicCollisionStrategy basicCollisionStrategy;
	private final Renderable renderable;
	private static final float SPEED_FACTOR = (float) 1.4;
	private boolean turboMode = false;
	private Renderable originalRenderer;
	private Vector2 originalVel;

	public TurboModeStrategy(Ball ball, BasicCollisionStrategy basicCollisionStrategy, Renderable renderable){

		this.basicCollisionStrategy = basicCollisionStrategy;
		this.renderable = renderable;
		this.ball = ball;


	}

	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		basicCollisionStrategy.onCollision(object1, object2);
		if (object2 instanceof PuckBall){
			return;
		}
		if (!this.turboMode){
			this.collisionCounter = this.ball.getCollisionCounter();
			this.turboMode = true;
			originalVel = ball.getVelocity();
			Vector2 newVel =originalVel.mult(SPEED_FACTOR);
			ball.setVelocity(newVel);

			this.originalRenderer = ball.renderer().getRenderable();
			ball.renderer().setRenderable(renderable);
		}

	}

	public void update(float deltaTime) {
		if (!turboMode || originalVel == null || originalRenderer == null) return;
		if( this.ball.getCollisionCounter()- this.collisionCounter >= NUM_COLLISIONS){
			this.turboMode = false;
			ball.setVelocity(originalVel);
			ball.renderer().setRenderable(originalRenderer);
		}
	}
}

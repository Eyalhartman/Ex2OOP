package bricker.brick_strategies;

import bricker.main.BrickerGameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;

public class BasicCollisionStrategy implements CollisionStrategy{
	private BrickerGameManager brickerGameManager;


	public BasicCollisionStrategy(BrickerGameManager brickerGameManager){
		this.brickerGameManager = brickerGameManager;
	}


	@Override
	public void onCollision(GameObject object1, GameObject object2) {
		brickerGameManager.removeGameObject(object1);// מסיר את הלבנה
		brickerGameManager.decrementCounter();
	}
}

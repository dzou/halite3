package map;

import hlt.GameMap;
import hlt.Position;

public class GoalGenerator {

  public static Position generateRandomGoal(GameMap map) {
    int randomX = (int) (map.width * Math.random());
    int randomY = (int) (map.height * Math.random());
    return Position.at(randomX, randomY);
  }
}

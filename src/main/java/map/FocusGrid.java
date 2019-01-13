package map;

import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import shipagent.MapOracle;

import java.util.ArrayList;
import java.util.List;

public class FocusGrid {

  private static final double FOCUS_REWARD = 15.0;
  private static final int FOCUS_POINT_RANGE = 4;

  private final List<FocusPoint> focusPoints;

  FocusGrid(List<FocusPoint> focusPoints) {
    this.focusPoints = focusPoints;
  }

  public double score(Ship ship, Direction d) {
    return focusPoints.stream().mapToDouble(f -> f.score(ship, d)).sum();
  }

  public static FocusGrid create(MapOracle mapOracle) {
    ArrayList<FocusPoint> focusPoints = new ArrayList<>();

    for (Position myDropoff : mapOracle.myDropoffsMap.keySet()) {
      if (haliteSum(myDropoff, mapOracle.haliteGrid) > 4000) {
        focusPoints.add(new FocusPoint(myDropoff, FOCUS_POINT_RANGE, mapOracle.myDropoffsMap.get(myDropoff)));
      }
    }

    return new FocusGrid(focusPoints);
  }

  private static int haliteSum(Position origin, Grid<Integer> haliteGrid) {
    int sum = 0;

    for (int dy = -FOCUS_POINT_RANGE; dy <= FOCUS_POINT_RANGE; dy++) {
      for (int dx = -FOCUS_POINT_RANGE + Math.abs(dy); dx <= FOCUS_POINT_RANGE - Math.abs(dy); dx++) {
        int x = origin.x + dx;
        int y = origin.y + dy;
        sum += haliteGrid.get(x, y);
      }
    }

    return sum;
  }

  private static class FocusPoint {
    private final Position focusCenter;
    private final int size;
    private final DjikstraGrid djikstraGrid;

    public FocusPoint(Position focusCenter, int size, DjikstraGrid djikstraGrid) {
      this.focusCenter = focusCenter;
      this.size = size;
      this.djikstraGrid = djikstraGrid;
    }

    public double score(Ship ship, Direction dir) {
      Position destination = ship.position.directionalOffset(dir);

//      double multiplier = ((Constants.MAX_HALITE - ship.halite) * (Constants.MAX_HALITE - ship.halite))
//          / (Constants.MAX_HALITE * Constants.MAX_HALITE);
      // double multiplier = (1.0 * Constants.MAX_HALITE - ship.halite) / Constants.MAX_HALITE;
      double reward = FOCUS_REWARD;

      if (djikstraGrid.haliteGrid.distance(destination, focusCenter) <= size) {
        return reward;
      }


      if (dir != Direction.STILL && DjikstraGrid.isInDirection(ship.position, focusCenter, dir, djikstraGrid.haliteGrid)) {
        return reward - 0.001 * djikstraGrid.costCache.get(destination.x, destination.y);
      } else {
        return 0.0;
      }
    }

  }
}

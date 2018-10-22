package map;

import hlt.Position;

public class Navigator {

  private final CostGrid costGrid;

  public Navigator(CostGrid costGrid) {
    this.costGrid = costGrid;
  }

  public CostGrid findShortestPath(Position start, Position goal) {
    CostGrid cache = new CostGrid(costGrid.width, costGrid.height);

    int xLimit = getAxisDirection(start.x, goal.x, costGrid.width);
    int yLimit = getAxisDirection(start.y, goal.y, costGrid.height);

    int xStep = xLimit > 0 ? 1 : -1;
    int yStep = yLimit > 0 ? 1 : -1;

    for (int y = start.y; y != start.y + yLimit + yStep; y += yStep) {
      for (int x = start.x; x != start.x + xLimit + xStep; x += xStep) {

        if (x == start.x && y == start.y) {
          cache.set(x, y, 0);
        } else {
          int cost = Integer.MAX_VALUE;
          if (x != start.x) {
            cost = costGrid.get(x, y) + cache.get(x - xStep, y);
          }

          if (y != start.y) {
            cost = Math.min(cost, costGrid.get(x, y) + cache.get(x, y - yStep));
          }
          cache.set(x, y, cost);
        }
      }
    }

    return cache;
  }

  private static int getAxisDirection(int start, int goal, int max) {
    int basicDelta = goal - start;
    int toroidalDelta = 0;
    if (goal > start) {
      toroidalDelta = goal - (start + max);
    } else if (goal < start) {
      toroidalDelta = goal - (start - max);
    }

    return minAbs(basicDelta, toroidalDelta);
  }

  private static int minAbs(int a, int b) {
    if (Math.abs(a) < Math.abs(b)) {
      return a;
    } else {
      return b;
    }
  }
}

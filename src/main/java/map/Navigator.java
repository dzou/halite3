package map;

import com.google.common.collect.ImmutableList;
import hlt.Position;
import hlt.Ship;

import java.util.ArrayDeque;
import java.util.HashMap;

public class Navigator {

  public static Path findShortestPath(Position start, Position goal, CostGrid costGrid) {
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
            cost = Math.min(cost, costGrid.get(x, y) + cache.get(x - xStep, y));
          }

          if (y != start.y) {
            cost = Math.min(cost, costGrid.get(x, y) + cache.get(x, y - yStep));
          }
          cache.set(x, y, cost);
        }
      }
    }

    Path path = new Path();

    int xCurr = start.x + xLimit;
    int yCurr = start.y + yLimit;

    while (xCurr != start.x || yCurr != start.y) {
      path.push(costGrid.normalize(xCurr, yCurr));
      if (xCurr == start.x) {
        yCurr -= yStep;
      } else if (yCurr == start.y) {
        xCurr -= xStep;
      } else {
        if (cache.get(xCurr - xStep, yCurr) < cache.get(xCurr, yCurr - yStep)) {
          xCurr -= xStep;
        } else {
          yCurr -= yStep;
        }
      }
    }

    return path;
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

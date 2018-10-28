package map;

import com.google.common.collect.ImmutableList;
import grid.CostGrid;
import grid.Grid;
import hlt.GameMap;
import hlt.Position;

import java.util.Comparator;
import java.util.PriorityQueue;

public class GoalGenerator {

  private final CostGrid costGrid;
  private final Position home;

  public GoalGenerator(GameMap gameMap, Position home) {
    this(CostGrid.create(gameMap.toHaliteGrid(), home));
  }

  public GoalGenerator(CostGrid costGrid) {
    this.costGrid = costGrid;
    this.home = costGrid.origin;
  }

  public ImmutableList<Position> getBestPositions(int positionCount) {
    PositionComparator comp = new PositionComparator(costGrid);
    PriorityQueue<Position> queue = new PriorityQueue<>(comp.reversed());
    for (int y = 0; y < costGrid.height; y++) {
      for (int x = 0; x < costGrid.width; x++) {
        queue.offer(Position.at(x, y));
        if (queue.size() > positionCount) {
          queue.poll();
        }
      }
    }

    return ImmutableList.sortedCopyOf(comp, queue);
  }

  static class PositionComparator implements Comparator<Position> {

    private final CostGrid costGrid;

    PositionComparator(CostGrid costGrid) {
      this.costGrid = costGrid;
    }

    public double getScore(Position a) {
      return 1.0 * costGrid.haliteGrid.get(a.x, a.y) / (costGrid.get(a.x, a.y) + 1);
    }

    @Override
    public int compare(Position a, Position b) {
      double scoreA = getScore(a);
      double scoreB = getScore(b);

      if (scoreA > scoreB) {
        return -1;
      } else if (scoreA < scoreB) {
        return 1;
      } else {
        return costGrid.haliteGrid.get(b.x, b.y) - costGrid.haliteGrid.get(a.x, a.y);
      }
    }
  }
}

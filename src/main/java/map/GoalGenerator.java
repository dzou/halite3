package map;

import com.google.common.collect.ImmutableList;
import grid.DjikstraGrid;
import hlt.GameMap;
import hlt.Position;

import java.util.Comparator;
import java.util.PriorityQueue;

public class GoalGenerator {

  private final DjikstraGrid djikstraGrid;
  private final Position home;

  public GoalGenerator(GameMap gameMap, Position home) {
    this(DjikstraGrid.create(gameMap.toHaliteGrid(), home, null));
  }

  public GoalGenerator(DjikstraGrid djikstraGrid) {
    this.djikstraGrid = djikstraGrid;
    this.home = djikstraGrid.origin;
  }

  public ImmutableList<Position> getBestPositions(int positionCount) {
    PositionComparator comp = new PositionComparator(djikstraGrid);
    PriorityQueue<Position> queue = new PriorityQueue<>(comp.reversed());
    for (int y = 0; y < djikstraGrid.haliteGrid.height; y++) {
      for (int x = 0; x < djikstraGrid.haliteGrid.width; x++) {
        queue.offer(Position.at(x, y));
        if (queue.size() > positionCount) {
          queue.poll();
        }
      }
    }

    return ImmutableList.sortedCopyOf(comp, queue);
  }

  static class PositionComparator implements Comparator<Position> {

    private final DjikstraGrid djikstraGrid;

    PositionComparator(DjikstraGrid djikstraGrid) {
      this.djikstraGrid = djikstraGrid;
    }

    public double getScore(Position a) {
      return 1.0 * (djikstraGrid.haliteGrid.get(a.x, a.y) - 0.1 * djikstraGrid.costCache.get(a.x, a.y))
          / djikstraGrid.haliteGrid.distance(djikstraGrid.origin, a);
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
        return djikstraGrid.haliteGrid.get(b.x, b.y) - djikstraGrid.haliteGrid.get(a.x, a.y);
      }
    }
  }
}

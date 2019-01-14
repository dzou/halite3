package map;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Position;

import java.util.*;

/**
 * A modified djikstra's alg. Find the lowest cost path from the origin to all points under the constraint that the
 * ship must take path with lowest manhattan distance.
 */
public class DjikstraGrid {

  public final Position origin;

  public final Grid<Integer> haliteGrid;
  public final Grid<Direction> directionCache;
  public final Grid<Integer> costCache;

  private DjikstraGrid(
      Position origin,
      Grid<Integer> haliteGrid,
      Grid<Direction> directionCache,
      Grid<Integer> costCache) {

    this.origin = origin;

    this.haliteGrid = haliteGrid;
    this.directionCache = directionCache;
    this.costCache = costCache;
  }

  public static DjikstraGrid create(Grid<Integer> haliteGrid, Position home) {
    Grid<Integer> costCache = new Grid<>(haliteGrid.width, haliteGrid.height, Integer.MAX_VALUE);
    Grid<Direction> directionCache = new Grid<>(haliteGrid.width, haliteGrid.height, Direction.STILL);
    costCache.set(home.x, home.y, 0);

    HashSet<Position> visited = new HashSet<>();
    ArrayDeque<Position> queue = new ArrayDeque<>();
    queue.push(home);

    while (!queue.isEmpty()) {
      Position curr = queue.removeFirst();

      if (visited.contains(curr)) {
        continue;
      }

      visited.add(curr);

      Optional<Position> minNeighborOption = getPrev(curr, home, haliteGrid)
          .stream()
          .min(Comparator.comparingInt(p -> costCache.get(p.x, p.y)));

      if (minNeighborOption.isPresent()) {
        Position minNeighbor = minNeighborOption.get();
        costCache.set(curr.x, curr.y, costCache.get(minNeighbor.x, minNeighbor.y) + haliteGrid.get(curr.x, curr.y));
        directionCache.set(curr.x, curr.y, haliteGrid.calculateDirection(curr, minNeighbor));
      }

      for (Position neighbor : haliteGrid.getNeighbors(curr)) {
        if (!visited.contains(neighbor)) {
          queue.addLast(neighbor);
        }
      }
    }

    return new DjikstraGrid(home, haliteGrid, directionCache, costCache);
  }

  private static Set<Position> getPrev(Position curr, Position home, Grid<Integer> haliteGrid) {
    ImmutableSet.Builder<Position> neighbors = ImmutableSet.builder();

    int xDelta = getAxisDirection(curr.x, home.x, haliteGrid.width);
    int yDelta = getAxisDirection(curr.y, home.y, haliteGrid.height);

    if (xDelta < 0) {
      neighbors.add(curr.directionalOffset(Direction.WEST));
    } else if (xDelta > 0) {
      neighbors.add(curr.directionalOffset(Direction.EAST));
    }

    if (yDelta < 0) {
      neighbors.add(curr.directionalOffset(Direction.NORTH));
    } else if (yDelta > 0) {
      neighbors.add(curr.directionalOffset(Direction.SOUTH));
    }

    return neighbors.build();
  }

  public static int getAxisDirection(int start, int goal, int max) {
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

  public static boolean isInDirection(Position origin, Position destination, Direction dir, Grid<Integer> haliteGrid) {
    if (dir == Direction.STILL) {
      return true;
    }

    int dx = getAxisDirection(origin.x, destination.x, haliteGrid.width);
    int dy = getAxisDirection(origin.y, destination.y, haliteGrid.height);

    if (dir == Direction.NORTH && dy <= 0
        || dir == Direction.SOUTH && dy >= 0
        || dir == Direction.WEST && dx <= 0
        || dir == Direction.EAST && dx >= 0
        || dir == Direction.STILL) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return costCache.toString();
  }
}

package grid;

import hlt.Direction;
import hlt.Position;
import map.Path;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DjikstraGrid {

  public final Position origin;
  public final Grid<Integer> haliteGrid;
  public final Grid<Direction> directionCache;
  public final Grid<Integer> costCache;

  private DjikstraGrid(
      Position origin, Grid<Integer> haliteGrid, Grid<Direction> directionCache, Grid<Integer> costCache) {
    this.origin = origin;
    this.haliteGrid = haliteGrid;
    this.directionCache = directionCache;
    this.costCache = costCache;
  }

  public Path findPath(Position destination) {
    if (!destination.equals(origin) && directionCache.get(destination.x, destination.y) == Direction.STILL) {
      throw new RuntimeException("Tried to route to destination which was never cached.");
    }

    Path path = new Path();

    int x = destination.x;
    int y = destination.y;

    Direction currDirection = directionCache.get(x, y);
    while (currDirection != Direction.STILL) {
      path.push(Position.at(x, y));

      if (currDirection == Direction.NORTH) {
        y -= 1;
      } else if (currDirection == Direction.SOUTH) {
        y += 1;
      } else if (currDirection == Direction.EAST) {
        x += 1;
      } else if (currDirection == Direction.WEST) {
        x -= 1;
      } else {
        throw new RuntimeException("Bug in findPath, don't know which direction to increment.");
      }

      currDirection = directionCache.get(x, y);
    }
    path.push(origin);

    return path;
  }

  public static DjikstraGrid create(Integer[][] rawInputGrid, Position origin) {
    return DjikstraGrid.create(new Grid<Integer>(rawInputGrid), origin, null);
  }

  public static DjikstraGrid create(Integer[][] rawInputGrid, Position origin, Position goal) {
    return DjikstraGrid.create(new Grid<Integer>(rawInputGrid), origin, goal);
  }

  public static DjikstraGrid create(Grid<Integer> haliteGrid, Position origin, @Nullable Position goal) {
    Grid<Direction> directionCache = new Grid<>(haliteGrid.width, haliteGrid.height, Direction.STILL);
    Grid<Integer> cache = new Grid<>(haliteGrid.width, haliteGrid.height, Integer.MAX_VALUE);
    cache.set(origin.x, origin.y, 0);

    HashSet<Position> visited = new HashSet<>();
    PriorityQueue<PositionEntry> queue = new PriorityQueue<>(Comparator.comparingInt(p -> p.costToPosition));
    queue.offer(PositionEntry.of(origin, 0));

    while (!queue.isEmpty()) {
      PositionEntry entry = queue.poll();
      Position curr = entry.position;

      if (goal != null && curr.equals(goal)) {
        break;
      }

      if (visited.contains(curr)) {
        continue;
      } else {
        visited.add(curr);
      }

      for (Position neighbor : haliteGrid.getNeighbors(curr)) {
        int costToNeighbor = cache.get(curr.x, curr.y) + haliteGrid.get(curr.x, curr.y);

        if (costToNeighbor < cache.get(neighbor.x, neighbor.y)) {
          cache.set(neighbor.x, neighbor.y, costToNeighbor);
          queue.offer(PositionEntry.of(neighbor, costToNeighbor));
          directionCache.set(neighbor.x, neighbor.y, haliteGrid.calculateDirection(neighbor, curr));
        }
      }
    }

    return new DjikstraGrid(origin, haliteGrid, directionCache, cache);
  }
}

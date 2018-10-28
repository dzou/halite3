package grid;

import hlt.Direction;
import hlt.Position;
import map.Path;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class CostGrid extends Grid<Integer> {

  private static final int MAX_VAL = 999999;

  private final Position origin;

  final Direction[][] prevGrid;

  private CostGrid(int width, int height, int initValue, Position origin) {
    super(width, height, initValue);

    this.origin = origin;

    this.prevGrid = new Direction[width][height];
    for (int i = 0; i < prevGrid.length; i++) {
      Arrays.fill(prevGrid[i], Direction.STILL);
    }
  }

  public Path findPath(Position destination) {
    Path path = new Path();

    int x = normalizeX(destination.x);
    int y = normalizeY(destination.y);

    Direction currDirection = prevGrid[y][x];
    while (currDirection != Direction.STILL) {
      path.push(Position.at(x, y));

      if (currDirection == Direction.NORTH) {
        y = normalizeY(y - 1);
      } else if (currDirection == Direction.SOUTH) {
        y = normalizeY(y + 1);
      } else if (currDirection == Direction.EAST) {
        x = normalizeX(x + 1);
      } else if (currDirection == Direction.WEST) {
        x = normalizeX(x - 1);
      } else {
        throw new RuntimeException("Bug in findPath, don't know which direction to increment.");
      }

      currDirection = prevGrid[y][x];
    }
    path.push(origin);

    return path;
  }

  public Position getOrigin() {
    return origin;
  }

  static CostGrid create(Grid<Integer> inputGrid, Position origin) {
    CostGrid costGrid = new CostGrid(inputGrid.width, inputGrid.height, MAX_VAL, origin);
    costGrid.set(origin.x, origin.y, 0);

    HashSet<Position> visited = new HashSet<>();
    PriorityQueue<PositionEntry> queue = new PriorityQueue<>(Comparator.comparingInt(p -> p.costToPosition));
    queue.offer(PositionEntry.of(origin, 0));

    while (!queue.isEmpty()) {
      PositionEntry entry = queue.poll();
      Position curr = entry.position;

      if (visited.contains(curr)) {
        continue;
      } else {
        visited.add(curr);
      }

      for (Position neighbor : inputGrid.getNeighbors(curr)) {
        int costToSquare = costGrid.get(curr.x, curr.y) + inputGrid.get(curr.x, curr.y);
        if (costToSquare < costGrid.get(neighbor.x, neighbor.y)) {
          costGrid.set(neighbor.x, neighbor.y, costToSquare);
          queue.offer(PositionEntry.of(neighbor, costToSquare));

          int nX = ((neighbor.x % inputGrid.width) + inputGrid.width) % inputGrid.width;
          int nY = ((neighbor.y % inputGrid.height) + inputGrid.height) % inputGrid.height;
          costGrid.prevGrid[nY][nX] = costGrid.calculateDirection(neighbor, curr);
        }
      }
    }

    return costGrid;
  }

  private static class PositionEntry {
    final Position position;
    final int costToPosition;

    public PositionEntry(Position position, int costToPosition) {
      this.position = position;
      this.costToPosition = costToPosition;
    }

    public static PositionEntry of(Position position, int costToPosition) {
      return new PositionEntry(position, costToPosition);
    }
  }
}

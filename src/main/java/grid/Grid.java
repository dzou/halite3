package grid;

import com.google.common.collect.ImmutableList;
import hlt.Direction;
import hlt.Position;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Generic grid representing the map. It self-contains the wrap-around map logic doing
 * the modulos and stuff for convenience.
 */
public class Grid<T> {

  public final int height;
  public final int width;
  private final T[][] costGrid;

  public Grid(int width, int height, T initValue) {
    this.width = width;
    this.height = height;
    this.costGrid = (T[][]) new Object[height][width];

    for (int i = 0; i < costGrid.length; i++) {
      Arrays.fill(costGrid[i], initValue);
    }
  }

  public Grid(T[][] costGrid) {
    this.costGrid = costGrid;
    this.height = costGrid.length;
    this.width = costGrid[0].length;
  }

  public void set(int x, int y, T value) {
    int adjX = normalizeX(x);
    int adjY = normalizeY(y);
    costGrid[adjY][adjX] = value;
  }

  public T get(int x, int y) {
    int adjX = normalizeX(x);
    int adjY = normalizeY(y);
    return costGrid[adjY][adjX];
  }

  public List<Position> getNeighbors(Position position) {
    ImmutableList.Builder<Position> neighbors = ImmutableList.builder();
    for (Direction d : Direction.ALL_CARDINALS) {
      Position neighbor = position.directionalOffset(d);
      neighbors.add(neighbor);
    }
    return neighbors.build();
  }

  public Position normalize(final Position position) {
    int x = ((position.x % width) + width) % width;
    int y = ((position.y % height) + height) % height;
    return new Position(x, y);
  }

  public int distance(final Position source, final Position target) {
    int sx = normalizeX(source.x);
    int sy = normalizeY(source.y);

    int tx = normalizeX(target.x);
    int ty = normalizeY(target.y);

    final int dx = Math.abs(sx - tx);
    final int dy = Math.abs(sy - ty);

    final int toroidal_dx = Math.min(dx, width - dx);
    final int toroidal_dy = Math.min(dy, height - dy);

    return toroidal_dx + toroidal_dy;
  }

  public Position normalize(int x, int y) {
    return new Position(normalizeX(x), normalizeY(y));
  }

  public int normalizeX(int x) {
    return (x % width + width) % width;
  }

  public int normalizeY(int y) {
    return (y % height + height) % height;
  }

  public Direction calculateDirection(Position start, Position end) {

    if (normalizeX(start.x - end.x) != 0) {
      if (normalizeX(start.x + 1 - end.x) == 0) {
        return Direction.EAST;
      } else {
        return Direction.WEST;
      }
    } else if (normalizeY(start.y - end.y) != 0) {
      if (normalizeY(start.y + 1 - end.y) == 0) {
        return Direction.SOUTH;
      } else {
        return Direction.NORTH;
      }
    }

    return Direction.STILL;
  }

  public Optional<Position> findClosestPosition(Position origin, Collection<Position> candidates) {
    Position best = null;
    int bestDist = Integer.MAX_VALUE;

    for (Position p : candidates) {
      int dist = distance(origin, p);
      if (dist < bestDist) {
        bestDist = dist;
        best = p;
      }
    }

    return Optional.ofNullable(best);
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0000");
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < costGrid.length; i++) {
      for (int j = 0; j < costGrid[i].length; j++) {
        if (costGrid[i][j] instanceof Integer) {
          stringBuilder.append(df.format(costGrid[i][j]) + " ");
        } else {
          stringBuilder.append(costGrid[i][j] + " ");
        }
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }
}

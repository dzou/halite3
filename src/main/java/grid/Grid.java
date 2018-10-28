package grid;

import com.google.common.collect.ImmutableList;
import hlt.Direction;
import hlt.Position;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

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
    if (start.x != end.x) {
      if (normalizeX(start.x + 1) == normalizeX(end.x)) {
        return Direction.EAST;
      } else {
        return Direction.WEST;
      }
    } else if (start.y != end.y) {
      if (normalizeY(start.y + 1) == normalizeY(end.y)) {
        return Direction.SOUTH;
      } else {
        return Direction.NORTH;
      }
    }

    return Direction.STILL;
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

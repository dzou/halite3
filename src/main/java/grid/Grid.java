package grid;

import com.google.common.collect.ImmutableList;
import hlt.Direction;
import hlt.Position;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Grid {

  public final int height;
  public final int width;

  private final int[][] costGrid;

  public Grid(int width, int height) {
    this(width, height, 0);
  }

  public Grid(int width, int height, int initValue) {
    this.costGrid = new int[height][width];
    this.width = width;
    this.height = height;

    for (int i = 0; i < costGrid.length; i++) {
      Arrays.fill(costGrid[i], initValue);
    }
  }

  public Grid(int[][] costGrid) {
    this.costGrid = costGrid;
    this.height = costGrid.length;
    this.width = costGrid[0].length;
  }

  public void set(int x, int y, int value) {
    int adjX = normalizeX(x);
    int adjY = normalizeY(y);
    costGrid[adjY][adjX] = value;
  }

  public int get(int x, int y) {
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
        stringBuilder.append(df.format(costGrid[i][j]) + " ");
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }
}

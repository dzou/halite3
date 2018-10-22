package map;

import hlt.Position;

import java.text.DecimalFormat;
import java.util.Arrays;

public class CostGrid {

  public final int height;
  public final int width;

  private final int[][] costGrid;

  public CostGrid(int width, int height) {
    this.costGrid = new int[height][width];
    this.width = width;
    this.height = height;
  }

  public CostGrid(int[][] costGrid) {
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

  private int normalizeX(int x) {
    return (x % width + width) % width;
  }

  private int normalizeY(int y) {
    return (y % height + height) % height;
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

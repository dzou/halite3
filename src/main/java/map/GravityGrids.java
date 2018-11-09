package map;

import grid.Grid;
import hlt.Position;

public class GravityGrids {

  private static final double DECAY_CONST = 0.5;

  private static final int MAX_EXPONENT = 32;

  private static final double[] powerCache = new double[MAX_EXPONENT + 1];

  public static Grid<Double> createGravityGrid(Grid<Integer> haliteGrid) {
    Grid<Double> gravityGrid = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);
    for (int a = 0; a < haliteGrid.height; a++) {
      for (int b = 0; b < haliteGrid.width; b++) {
        updateInfluence(a, b, haliteGrid, gravityGrid);
      }
    }

    return gravityGrid;
  }

  private static void updateInfluence(
      int originX, int originY, Grid<Integer> haliteGrid, Grid<Double> gravityGrid) {
    for (int x = 0; x < haliteGrid.height; x++) {
      for (int y = 0; y < haliteGrid.width; y++) {
        int distanceFromOrigin = Math.min(haliteGrid.distance(originX, originY, x, y), MAX_EXPONENT);

        if (powerCache[distanceFromOrigin] == 0) {
          powerCache[distanceFromOrigin] = Math.pow(DECAY_CONST, distanceFromOrigin);
        }

        double multiplier = powerCache[distanceFromOrigin];

        gravityGrid.set(x, y, gravityGrid.get(x, y) + haliteGrid.get(originX, originY) * multiplier);
      }
    }
  }
}

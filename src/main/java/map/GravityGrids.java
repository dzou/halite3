package map;

import grid.Grid;

public class GravityGrids {

  private static final double DECAY_CONST = 0.25;

  private static final int MAX_EXPONENT = 10;

  private static final double[] powerCache = new double[MAX_EXPONENT + 1];

  public static Grid<Double> createGravityGrid(Grid<Integer> haliteGrid) {
    Grid<Double> gravityGrid = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);
    for (int a = 0; a < haliteGrid.height; a++) {
      for (int b = 0; b < haliteGrid.width; b++) {
        updateInfluence(b, a, haliteGrid, gravityGrid);
      }
    }

    return gravityGrid;
  }

  private static void updateInfluence(
      int originX, int originY, Grid<Integer> haliteGrid, Grid<Double> gravityGrid) {

    double bestHalitePotential = -9999999;
    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        int turnsSpent = haliteGrid.distance(originX, originY, x, y) + 3;
        double halitePotential = 0.58 * haliteGrid.get(x, y) / turnsSpent;
        if (halitePotential > bestHalitePotential) {
          bestHalitePotential = halitePotential;
        }
      }
    }

    gravityGrid.set(originX, originY, bestHalitePotential);
  }
}

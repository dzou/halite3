package tiles;

import hlt.Direction;
import hlt.Position;
import java.util.ArrayList;
import map.Grid;
import map.SimulationGrid;

public class TileValueGrid {

  private static final int SIMULATION_RANGE = 2;

  private static final int SIMULATION_LENGTH = 10;

  public static Grid<ArrayList<TileWalk>> create(Grid<Integer> haliteGrid) {
    GoalFilter goalFilter = new GoalFilter(haliteGrid);

    Grid<ArrayList<TileWalk>> valueGrid = new Grid<>(haliteGrid.width, haliteGrid.height, null);

    for (int y = 0; y < valueGrid.height; y++) {
      for (int x = 0; x < valueGrid.width; x++) {
        ArrayList<TileWalk> tileWalks = simulateWalk(Position.at(x, y), haliteGrid, goalFilter);
        valueGrid.set(x, y, tileWalks);
      }
    }
    return valueGrid;
  }

  private static ArrayList<TileWalk> simulateWalk(Position shipOrigin, Grid<Integer> haliteGrid, GoalFilter goalFilter) {
    ArrayList<TileWalk> tileWalks = new ArrayList<>();

    SimulationGrid simulationGrid = new SimulationGrid(haliteGrid, shipOrigin);
    for (int i = 0; i < SIMULATION_LENGTH; i++) {
      Direction bestDir = getBestDirection(shipOrigin, simulationGrid, goalFilter);
      simulationGrid.moveShip(bestDir);

      tileWalks.add(new TileWalk(simulationGrid.getHaliteGained(), simulationGrid.getPosition()));
    }

    return tileWalks;
  }

  private static Direction getBestDirection(
      Position shipOrigin, SimulationGrid simulationGrid, GoalFilter goalFilter) {

    Direction bestDir = Direction.STILL;
    double bestScore = 0.0;

    for (Direction dir : Direction.values()) {
      double score = getValueOfMove(shipOrigin, dir, simulationGrid, goalFilter);
      if (score > bestScore) {
        bestDir = dir;
        bestScore = score;
      }
    }

    return bestDir;
  }

  private static double getValueOfMove(
      Position shipOrigin, Direction dir, SimulationGrid simulationGrid, GoalFilter goalFilter) {
    if (dir == Direction.STILL) {
      return 0.25 * simulationGrid.getHalite(shipOrigin);
    } else {
      return goalFilter.getLocalMoves(shipOrigin, dir, SIMULATION_RANGE)
          .stream()
          .mapToDouble(pos ->
              (1.0 * simulationGrid.getHalite(pos) - 0.1 * simulationGrid.getHalite(shipOrigin))
                  / (simulationGrid.distance(pos, shipOrigin) + 1))
          .max()
          .orElse(0.0);
    }

  }
}

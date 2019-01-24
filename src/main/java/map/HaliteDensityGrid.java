package map;

import hlt.Log;
import hlt.Position;
import hlt.Ship;
import shipagent.MapOracle;
import tiles.GoalFilter;
import tiles.GoalFinder;
import tiles.TileScoreEntry;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HaliteDensityGrid {

  private static final int MIN_HALITE_SUMS = 6000;

  private static final int MIN_HALITE_VELOCITY = 1600;

  private static final int MIN_SPACE_BTWN_DROPOFFS = 16;

  private static final int HALITE_DENSITY_RANGE = 4;

  private final MapOracle mapOracle;

  private final Grid<Double> haliteVelocitySums;

  private final Grid<Integer> haliteRawSums;

  private HaliteDensityGrid(MapOracle mapOracle, Grid<Double> haliteVelocitySums, Grid<Integer> haliteRawSums) {
    this.mapOracle = mapOracle;
    this.haliteVelocitySums = haliteVelocitySums;
    this.haliteRawSums = haliteRawSums;
  }

  public Optional<Position> getDropoffCandidate() {
    Set<Position> myDropOffs = mapOracle.myDropoffsMap.keySet();

    Position bestPos = null;
    double bestHaliteVelocity = 0.0;

    for (int y = 0; y < mapOracle.haliteGrid.height; y++) {
      for (int x = 0; x < mapOracle.haliteGrid.width; x++) {

        Position curr = Position.at(x, y);

        boolean tooCloseToOtherDropoff = myDropOffs.stream()
            .filter(dropOff -> mapOracle.haliteGrid.distance(dropOff, curr) <= MIN_SPACE_BTWN_DROPOFFS)
            .findAny()
            .isPresent();

        if (tooCloseToOtherDropoff
            || mapOracle.allExistingDropoffs.contains(curr)
            || mapOracle.influenceDifferenceAtPoint(x, y) < 0
            || (mapOracle.controlMap.get(x, y) < 0 && mapOracle.influenceDifferenceAtPoint(x, y) < 1.0)) {
          continue;
        }

        double haliteVelocity = haliteVelocitySums.get(x, y);
        double haliteSums = haliteRawSums.get(x, y);

        if (haliteVelocity > MIN_HALITE_VELOCITY
            && haliteSums > MIN_HALITE_SUMS
            && haliteVelocity > bestHaliteVelocity) {
          bestPos = curr;
          bestHaliteVelocity = haliteVelocity;
        }
      }
    }

    return Optional.ofNullable(bestPos);
  }

  public void logInfo(Position dropoffCandidate) {
    Log.log("Candidate Dropoff: " + dropoffCandidate);
    Log.log("Halite Velocity: " + haliteVelocitySums.get(dropoffCandidate.x, dropoffCandidate.y));
    Log.log("Halite Sums: " + haliteRawSums.get(dropoffCandidate.x, dropoffCandidate.y));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Raw sums: \n");
    builder.append(haliteRawSums + "\n");
    builder.append("velocities: \n");
    builder.append(haliteVelocitySums + "\n");

    return builder.toString();
  }

  public static HaliteDensityGrid create(MapOracle mapOracle) {
    Grid<Integer> haliteGrid = mapOracle.haliteGrid;
    Grid<Double> haliteVelocitySums = new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, 0.0);
    Grid<Integer> haliteRawSums = new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, 0);

    for (int centerY = 0; centerY < haliteGrid.height; centerY++) {
      for (int centerX = 0; centerX < haliteGrid.width; centerX++) {
        for (int dy = -HALITE_DENSITY_RANGE; dy <= HALITE_DENSITY_RANGE; dy++) {
          for (int dx = -HALITE_DENSITY_RANGE + Math.abs(dy); dx <= HALITE_DENSITY_RANGE - Math.abs(dy); dx++) {
            int x = centerX + dx;
            int y = centerY + dy;

            if (mapOracle.influenceDifferenceAtPoint(x, y) < 0) {
              continue;
            }

            double multiplier = mapOracle.inspireMap.get(x, y) > 1 ? 2.2 : 1;
            int haliteGridAmt = (int) (multiplier * haliteGrid.get(centerX, centerY));

            double curr = 1.0 * haliteGridAmt / (haliteVelocitySums.distance(centerX, centerY, x, y) + 1);
            double prev = haliteVelocitySums.get(x, y);
            haliteVelocitySums.set(x, y, prev + curr);

            int prevSum = haliteRawSums.get(x, y);
            haliteRawSums.set(x, y, prevSum + haliteGridAmt);
          }
        }
      }
    }


    for (Ship ship : mapOracle.myShips) {
      for (int dy = -HALITE_DENSITY_RANGE; dy <= HALITE_DENSITY_RANGE; dy++) {
        for (int dx = -HALITE_DENSITY_RANGE + Math.abs(dy); dx <= HALITE_DENSITY_RANGE - Math.abs(dy); dx++) {
          int x = ship.position.x + dx;
          int y = ship.position.y + dy;

          double curr = 1.0 * ship.halite / (haliteVelocitySums.distance(ship.position.x, ship.position.y, x, y) + 1);
          double prev = haliteVelocitySums.get(x, y);
          haliteVelocitySums.set(x, y, prev + curr);
        }
      }
    }

    return new HaliteDensityGrid(mapOracle, haliteVelocitySums, haliteRawSums);
  }


//  private static Grid<Double> haliteVelocityMap(Grid<Integer> haliteGrid) {
//    Grid<Double> densityGrid = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);
//    for (int y = 0; y < haliteGrid.height; y++) {
//      for (int x = 0; x < haliteGrid.width; x++) {
//        densityGrid.set(x, y, getHaliteDensity(x, y, haliteGrid));
//      }
//    }
//
//    return densityGrid;
//  }
//
//  private static double getHaliteDensity(int x, int y, Grid<Integer> haliteGrid) {
//    int densitySum = 0;
//
//    for (int dy = -HALITE_DENSITY_RANGE; dy <= HALITE_DENSITY_RANGE; dy++) {
//      for (int dx = -HALITE_DENSITY_RANGE + Math.abs(dy); dx <= HALITE_DENSITY_RANGE - Math.abs(dy); dx++) {
//        int neighborX = x + dx;
//        int neighborY = y + dy;
//        densitySum += 1.0 * haliteGrid.get(neighborX, neighborY) / (haliteGrid.distance(x, y, neighborX, neighborY) + 1);
//      }
//    }
//
//    return densitySum;
//  }
}

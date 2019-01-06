package map;

import hlt.Log;
import hlt.Position;
import hlt.Ship;
import shipagent.MapOracle;
import tiles.GoalFilter;
import tiles.TileScoreEntry;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HaliteDensityGrid {

  private static final int MIN_HALITE_SUMS = 2000;

  private static final int MIN_HALITE_VELOCITY = 400;

  private static final int MIN_SPACE_BTWN_DROPOFFS = 16;

  private static final int HALITE_DENSITY_RANGE = 5;

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
            || mapOracle.influenceDifferenceAtPoint(x, y) < 0.1) {
          continue;
        }

        double haliteVelocity = haliteVelocitySums.get(x, y);
        double haliteSums = haliteRawSums.get(x, y);

        if (haliteVelocity > MIN_HALITE_VELOCITY
            && haliteSums > MIN_HALITE_SUMS
            && haliteVelocity * haliteSums > bestHaliteVelocity) {
          bestPos = curr;
          bestHaliteVelocity = haliteVelocity * haliteSums;
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
    List<TileScoreEntry> bestTiles = GoalFilter.getBestPositions(mapOracle, 100);

    Grid<Double> haliteVelocitySums = new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, 0.0);
    Grid<Integer> haliteRawSums = new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, 0);

    for (TileScoreEntry tileScoreEntry : bestTiles) {
      for (int dy = -HALITE_DENSITY_RANGE; dy <= HALITE_DENSITY_RANGE; dy++) {
        for (int dx = -HALITE_DENSITY_RANGE + Math.abs(dy); dx <= HALITE_DENSITY_RANGE - Math.abs(dy); dx++) {
          int x = tileScoreEntry.position.x + dx;
          int y = tileScoreEntry.position.y + dy;

          double curr = mapOracle.haliteGrid.get(tileScoreEntry.position.x, tileScoreEntry.position.y)
              / (haliteVelocitySums.distance(tileScoreEntry.position.x, tileScoreEntry.position.y, x, y) + 1);
          double prev = haliteVelocitySums.get(x, y);
          haliteVelocitySums.set(x, y, prev + curr);

          int prevSum = haliteRawSums.get(x, y);
          int haliteOnTile = mapOracle.haliteGrid.get(tileScoreEntry.position.x, tileScoreEntry.position.y);
          haliteRawSums.set(x, y, prevSum + haliteOnTile);
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
}

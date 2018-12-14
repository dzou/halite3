package shipagent;

import hlt.*;
import map.Grid;

import java.util.Optional;
import java.util.Set;

public class HaliteSpender {

  private static final int MIN_HALITE_DENSITY_TO_DROPOFF = 2000;

  private static final int MIN_SPACE_BTWN_DROPOFFS = 17;

  private static final int MAX_DROPOFFS = 8;

  private static final int DROPOFF_TURNS_REMAINING_CUTOFF = 100;

  private final MapOracle oracle;

  private int haliteAvailable;

  public HaliteSpender(MapOracle oracle, int haliteAvailable) {
    this.oracle = oracle;
    this.haliteAvailable = haliteAvailable;
  }

  public boolean shouldMakeShip() {
    double haliteSum = 0;
    for (int y = 0; y < oracle.haliteGrid.height; y++) {
      for (int x = 0; x < oracle.haliteGrid.width; x++) {
        haliteSum += oracle.haliteGrid.get(x, y);
      }
    }
    double avgHalitePotential = 0.25 *  haliteSum / (oracle.haliteGrid.width * oracle.haliteGrid.height);

    return haliteAvailable >= Constants.SHIP_COST && oracle.turnsRemaining * avgHalitePotential > 3000;
  }

  public Optional<Position> orderDropoff() {
    if (!shouldOrderDropoff()) {
      return Optional.empty();
    }

    Set<Position> myDropOffs = oracle.myDropoffsMap.keySet();
    Grid<Double> densityMap = oracle.haliteDensityMap;

    Position bestPos = null;
    double maxNetHaliteDensity = -1;
    int haliteDeducted = 0;

    for (int y = 0; y < densityMap.height; y++) {
      for (int x = 0; x < densityMap.width; x++) {
        final int tx = x;
        final int ty = y;
        boolean tooCloseToOtherDropoff = myDropOffs.stream()
            .filter(dropOff -> densityMap.distance(dropOff.x, dropOff.y, tx, ty) <= MIN_SPACE_BTWN_DROPOFFS)
            .findAny()
            .isPresent();

        if (tooCloseToOtherDropoff || oracle.influenceDifferenceAtPoint(x, y) < 0.0) {
          continue;
        }

        int fundsOnCell = getFundsOnCell(x, y);
        double haliteDensity = densityMap.get(x, y);
        double netHaliteDensity = densityMap.get(x, y) + oracle.shipHaliteDensityMap.get(x, y);

        if (fundsOnCell + haliteAvailable >= Constants.DROPOFF_COST
            && haliteDensity > MIN_HALITE_DENSITY_TO_DROPOFF
            && netHaliteDensity > maxNetHaliteDensity) {
          bestPos = Position.at(x, y);
          maxNetHaliteDensity = netHaliteDensity;
          haliteDeducted = Constants.DROPOFF_COST - fundsOnCell;
        }
      }
    }

    if (bestPos != null) {
      haliteAvailable -= haliteDeducted;
      return Optional.of(bestPos);
    } else {
      return Optional.empty();
    }
  }

  private boolean shouldOrderDropoff() {
    if (oracle.myShips.size() < 10
        || oracle.myDropoffsMap.size() >= MAX_DROPOFFS
        || oracle.turnsRemaining < DROPOFF_TURNS_REMAINING_CUTOFF) {
      return false;
    } else {
      return true;
    }
  }

  private int getFundsOnCell(int x, int y) {
    int fundsOnCell = oracle.haliteGrid.get(x, y);

    Ship shipOnCell = oracle.myShipPositionsMap.get(Position.at(x, y));
    if (shipOnCell != null) {
      fundsOnCell += shipOnCell.halite;
    }

    return fundsOnCell;
  }

}

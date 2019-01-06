package shipagent;

import hlt.Constants;
import hlt.Position;
import map.HaliteDensityGrid;

import java.util.Optional;

public class HaliteSpender {

  private static final int MAX_DROPOFFS = 10;

  private static final int DROPOFF_TURNS_REMAINING_CUTOFF = 75;

  private final MapOracle oracle;

  private final HaliteDensityGrid haliteDensityGrid;

  private int haliteAvailable;

  public HaliteSpender(MapOracle oracle, int haliteAvailable) {
    this.oracle = oracle;
    this.haliteDensityGrid = HaliteDensityGrid.create(oracle);
    this.haliteAvailable = haliteAvailable;
  }

  public boolean shouldMakeShip() {
    double haliteSum = 0;
    for (int y = 0; y < oracle.haliteGrid.height; y++) {
      for (int x = 0; x < oracle.haliteGrid.width; x++) {
        haliteSum += oracle.haliteGrid.get(x, y);
      }
    }
    double avgHalitePotential = 0.25 * haliteSum / (oracle.haliteGrid.width * oracle.haliteGrid.height);

    return haliteAvailable >= Constants.SHIP_COST && oracle.turnsRemaining * avgHalitePotential > 2700;
  }

  public Optional<Position> orderDropoff() {
    Optional<Position> dropoffCandidate = haliteDensityGrid.getDropoffCandidate();

    if (!dropoffCandidate.isPresent()) {
      return Optional.empty();
    }

    if (!shouldOrderDropoff()) {
      return Optional.empty();
    }

    Position dropoffLocation = dropoffCandidate.get();

    int freeHalite = oracle.haliteGrid.get(dropoffLocation.x, dropoffLocation.y);
    if (oracle.myShipPositionsMap.containsKey(dropoffLocation)) {
      freeHalite += oracle.myShipPositionsMap.get(dropoffLocation).halite;
    }

    int haliteDeducted = Constants.DROPOFF_COST - freeHalite;
    haliteAvailable -= haliteDeducted;

    if (haliteAvailable < 0) {
      return Optional.empty();
    } else {
      haliteDensityGrid.logInfo(dropoffLocation);
      return dropoffCandidate;
    }
  }

  private boolean shouldOrderDropoff() {
    if (oracle.myShips.size() < 10
        || oracle.myDropoffsMap.size() >= MAX_DROPOFFS
        || oracle.turnsRemaining < DROPOFF_TURNS_REMAINING_CUTOFF) {
      return false;
    }

    return (oracle.myShips.size() / 10 + 1) > oracle.myDropoffsMap.size();
  }

}

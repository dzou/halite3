package shipagent;

import bot.HaliteStatTracker;
import hlt.Constants;
import hlt.Log;
import hlt.Position;
import map.HaliteDensityGrid;

import java.util.Optional;

public class HaliteSpender {

  private static final int MAX_DROPOFFS = 10;

  private static final int DROPOFF_TURNS_REMAINING_CUTOFF = 100;

  private final MapOracle oracle;

  private final HaliteDensityGrid haliteDensityGrid;

  private int haliteAvailable;

  public HaliteSpender(MapOracle oracle, int haliteAvailable) {
    this.oracle = oracle;
    this.haliteDensityGrid = HaliteDensityGrid.create(oracle);
    this.haliteAvailable = haliteAvailable;
  }

  public boolean canAffordShipAndShouldMake() {
    if (haliteAvailable < Constants.SHIP_COST) {
      return false;
    }

    return oracle.shouldMakeShip();
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
    if (oracle.myShips.size() < 14
        || oracle.myDropoffsMap.size() >= MAX_DROPOFFS
        || oracle.turnsRemaining < DROPOFF_TURNS_REMAINING_CUTOFF) {
      return false;
    }

    return (oracle.myShips.size() / 14 + 1) > oracle.myDropoffsMap.size();
  }

}

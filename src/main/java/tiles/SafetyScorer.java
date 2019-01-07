package tiles;

import hlt.Position;
import hlt.Ship;
import shipagent.MapOracle;

/**
 * Scores the safety of tiles and whether a trade should occur.
 *
 * -infinity to 0.
 */
public class SafetyScorer {

  private final MapOracle mapOracle;

  public SafetyScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
  }

  private double shipOwnershipRatio() {
    return 1.0 * mapOracle.myShips.size() / (mapOracle.myShips.size() + mapOracle.enemyShips.size());
  }

  private boolean isTileSafe(Position destination) {
    if (mapOracle.enemyThreatMap.get(destination.x, destination.y) != -1) {
      return false;
    }

    if (mapOracle.enemyShipCovers.isPositionCovered(destination)
        && mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) < 0) {
      return false;
    }

    return true;
  }

  private boolean isGoodTrade(Position destination) {

  }

  public double tileControlScore(Ship ship, Position position) {
    if (shipOwnershipRatio() > 0.45) {

    } else {

    }

    double influenceDifference = mapOracle.influenceDifferenceAtPoint(destination.x, destination.y);

    Position nearestHome = mapOracle.getNearestHome(destination);
    if (mapOracle.haliteGrid.distance(nearestHome, destination) <= 1) {
      return Math.max(0.0, enemyMinHalite);
    }

    if (influenceDifference > 0.0) {
      if (enemyMinHalite != -1) {
        return Math.min(0, enemyMinHalite - 1.3 * ship.halite);
      } else {
        return 0;
      }
    } else {
      if (enemyMinHalite != -1) {
        double multiplier = 0.75 + (destination.equals(ship.position) ? 0.25 : 0.0);
        return multiplier * -ship.halite;
      } else if () {
        return (0.5 - influenceDifference * 0.01) * -ship.halite;
      } else {
        return 0;
      }
    }
  }
}

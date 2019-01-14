package tiles;

import hlt.Position;
import hlt.Ship;
import shipagent.MapOracle;

/**
 * Scores the safety of tiles and whether a trade should occur.
 * <p>
 * -infinity to 0.
 */
public class SafetyScorer {

  private final MapOracle mapOracle;

  public SafetyScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
  }

  public double safetyScore(Ship ship, Position destination) {
    Position nearestHome = mapOracle.getNearestHome(destination);

    if (mapOracle.enemyDropoffs.contains(destination) && inDirectThreat(destination)) {
      return -ship.halite;
    }

    if (mapOracle.haliteGrid.distance(nearestHome, destination) <= 1) {
      return 0;
    }

    if (!inDirectThreat(destination) && !inSurroundThreat(destination)) {
      return 0;
    }

    if (inDirectThreat(destination) && inSurroundThreat(destination)) {
      return -ship.halite;
    }

    if (inDirectThreat(destination)) {
      if (isGoodTrade(ship, destination)) {
        return 0;
      } else {
        return -ship.halite;
      }
    } else {
      if (ship.halite < 100) {
        return 0;
      } else {
        return -0.1 * ship.halite;
      }
    }
  }

  /**
   * if a trade occurs, would it be positive?
   */
  private boolean isGoodTrade(Ship ship, Position destination) {
    int enemyThreatHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);
    if (ship.halite > enemyThreatHalite) {
      return false;
    }

    boolean moreFriendliesInArea =
        mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 0;

    if (4.0 * ship.halite < enemyThreatHalite) {
      return true;
    }

    if (moreFriendliesInArea) {
      return true;
    }

//    if (shipOwnershipRatio() > 0.35) {
//    } else {
//      if (4 * ship.halite < enemyThreatHalite && moreFriendliesInArea) {
//        return true;
//      }
//    }

    return false;
  }

  private boolean inSurroundThreat(Position destination) {
    return mapOracle.enemyShipCovers.isPositionCovered(destination)
        && mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) < 0;
  }

  private boolean inDirectThreat(Position destination) {
    return mapOracle.enemyThreatMap.get(destination.x, destination.y) != -1;
  }

  private double shipOwnershipRatio() {
    return 1.0 * mapOracle.myShips.size() / (mapOracle.myShips.size() + mapOracle.enemyShips.size());
  }
}

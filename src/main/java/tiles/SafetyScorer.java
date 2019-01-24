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

    if (mapOracle.haliteGrid.distance(nearestHome, destination) <= 2
        && mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 0.5) {
      return 0;
    }

    if (!inDirectThreat(destination) && !inSurroundThreat(destination)) {
      return 0;
    }

    if (inDirectThreat(destination)) {
      if (isGoodTrade(ship, destination)) {
        return 0;
      } else {
        return -ship.halite + 0.1 * mapOracle.enemyThreatMap.get(destination.x, destination.y);
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
  public boolean isGoodTrade(Ship ship, Position destination) {
    int enemyThreatHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);
    int haliteSpoils = ship.halite + enemyThreatHalite;

    if (mapOracle.inspireMap.get(destination.x, destination.y) > 2) {
      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 4.0) {
        return true;
      }

      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 2.5 && shipOwnershipRatio() > 0.30) {
        return true;
      }

      int nearestHome = mapOracle.distance(destination, mapOracle.getNearestHome(destination));
      int nearestEnemy = mapOracle.distance(destination, mapOracle.nearestEnemyBase(destination));

      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 1.0
          && (nearestHome <= nearestEnemy && nearestEnemy <= 2)
          && (ship.halite < enemyThreatHalite || ship.halite < 100)) {
        return true;
      }
    } else {
      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 0.0
          && ship.halite < enemyThreatHalite
          && shipOwnershipRatio() > 0.25) {
        return true;
      }

      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 1.0
          && 2 * ship.halite < enemyThreatHalite
          && haliteSpoils > 200) {
        return true;
      }
    }

    return false;
  }

  private boolean inSurroundThreat(Position destination) {
    return mapOracle.enemyShipCovers.isPositionCovered(destination)
        && mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) < -0.5;
  }

  private boolean inDirectThreat(Position destination) {
    return mapOracle.enemyThreatMap.get(destination.x, destination.y) != -1;
  }

  private double shipOwnershipRatio() {
    return 1.0 * mapOracle.myShips.size() / (mapOracle.myShips.size() + mapOracle.enemyShips.size());
  }
}

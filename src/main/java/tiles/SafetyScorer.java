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

  public boolean isSafeShipMove(Ship ship, Position destination) {
    Position nearestHome = mapOracle.getNearestHome(destination);
    if (mapOracle.haliteGrid.distance(nearestHome, destination) <= 1) {
      return true;
    }

    if (!inDirectThreat(destination) && !inSurroundThreat(destination)) {
      return true;
    }

    if (inDirectThreat(destination) && inSurroundThreat(destination)) {
      return false;
    }

    if (inDirectThreat(destination)) {
      return isGoodTrade(ship, destination);
    } else {
      // else we are in Surround-threat. Safe if we have < 100 halite.
      return ship.halite < 100;
    }
  }

  /** if a trade occurs, would it be positive? */
  private boolean isGoodTrade(Ship ship, Position destination) {
    int enemyThreatHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);
    if (ship.halite > enemyThreatHalite) {
      return false;
    }

    boolean moreFriendliesInArea =
        mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 0;

    if (shipOwnershipRatio() > 0.47) {
      if (3 * ship.halite < enemyThreatHalite) {
        return true;
      }

      if (moreFriendliesInArea) {
        return true;
      }
    } else {
      if (4 * ship.halite < enemyThreatHalite && moreFriendliesInArea) {
        return true;
      }
    }

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
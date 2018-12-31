package tiles;

import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.Zone;
import shipagent.MapOracle;

public class ZoneScorer {

  private final MapOracle mapOracle;

  public ZoneScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
  }

  public double zoneScore(Ship ship, Direction dir, Zone goalZone) {
    Position shipMovedPosition = ship.position.directionalOffset(dir);

    Position zonePosition = goalZone.bestTile().tilePosition;
    Position nearestHome = mapOracle.getNearestHome(zonePosition);

//    int distanceToZone = goalZone.centerPoints.stream()
//        .mapToInt(pos -> mapOracle.haliteGrid.distance(shipOrigin, pos))
//        .min()
//        .orElse(0);

    int distanceToZone = mapOracle.distance(shipMovedPosition, zonePosition);

    int turnsTaken = distanceToZone
        + mapOracle.distance(zonePosition, nearestHome)
        + 13;

    double inspireMultiplier = (mapOracle.inspireMap.get(zonePosition.x, zonePosition.y) > 1) ? 1.10 : 1.0;

    double haliteMined = Math.min(
        Constants.MAX_HALITE - ship.halite,
        goalZone.topThreeSum() * 0.25 * inspireMultiplier);

    double costToHome = mapOracle.goHomeCost(zonePosition);
        // - 0.058 * mapOracle.haliteGrid.get(zonePosition.x, zonePosition.y);

    return (haliteMined - costToHome) / turnsTaken;
  }
}

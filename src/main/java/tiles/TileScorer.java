package tiles;

import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.LocalCostGrid;
import map.ZoneGrid;
import map.ZonePlan;
import shipagent.MapOracle;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TileScorer {

  private static final double[] MINE_RATIOS = {0.44, 0.58, 0.68, 0.76};

  private final MapOracle mapOracle;

  private final ZoneGrid zoneGrid;

  private final HashMap<Ship, LocalCostGrid> shipMoveCostCache;

  public TileScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.zoneGrid = ZoneGrid.create(mapOracle.haliteGrid);
    this.shipMoveCostCache = new HashMap<>();
  }

  public double localGoalScore(Ship ship, Direction dir, Position explorePosition) {
    if (!shipMoveCostCache.containsKey(ship)) {
      shipMoveCostCache.put(ship, LocalCostGrid.create(
          mapOracle.haliteGrid, ship.position, GoalAssignment.LOCAL_SEARCH_RANGE, mapOracle.myShipPositionsMap.keySet()));
    }

    Position shipMovedPosition = ship.position.directionalOffset(dir);

    LocalCostGrid localCostGrid = shipMoveCostCache.get(ship);

    int haliteSumToDest;
    if (mapOracle.distance(ship.position, explorePosition) <= GoalAssignment.LOCAL_SEARCH_RANGE) {
      haliteSumToDest = localCostGrid.getCostToDest(explorePosition, dir);
    } else {
      haliteSumToDest = (int) (localCostGrid.averageDistance() * GoalAssignment.LOCAL_SEARCH_RANGE);
    }

    Position nearestHome = mapOracle.getNearestHome(explorePosition);
    List<ZonePlan> zonePlans = zoneGrid.getZonePlans(explorePosition.x, explorePosition.y, mapOracle);

    double best = 0;
    for (ZonePlan plan : zonePlans) {
      int turnsSpentOnTile = plan.turnsSpentInZone;

      double haliteReward = Math.min(Constants.MAX_HALITE - ship.halite, plan.haliteGained);

      double turnsInTransit = mapOracle.haliteGrid.distance(shipMovedPosition, explorePosition)
          + mapOracle.distance(explorePosition, nearestHome) // * (0.25 * ship.halite / Constants.MAX_HALITE)
          + 1;

      if (dir == Direction.STILL) {
        turnsInTransit += 1;
      }

      double tollToTile = Math.max(0, haliteSumToDest * 0.1);
      double tollHome = (1.0 * ship.halite / Constants.MAX_HALITE) * (mapOracle.goHomeCost(explorePosition));

      best = Math.max(best, (haliteReward - tollToTile - tollHome) / (turnsInTransit + turnsSpentOnTile));
    }

    return best - 0.03 * mapOracle.myInfluenceMap.get(shipMovedPosition.x, shipMovedPosition.y);
  }

  public double mineScore(Ship ship) {
    double haliteMinedPotential = mapOracle.haliteGrid.get(ship.position.x, ship.position.y) * 0.25;
    double actualHaliteMined = Math.min(
        Constants.MAX_HALITE - ship.halite,
        mapOracle.inspireMap.get(ship.position.x, ship.position.y) > 1 ? haliteMinedPotential * 3 : haliteMinedPotential);
    return actualHaliteMined;
  }

  private static class ShipDirectionPair {
    private final Ship ship;
    private final Direction direction;

    ShipDirectionPair(Ship ship, Direction direction) {
      this.ship = ship;
      this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ShipDirectionPair that = (ShipDirectionPair) o;
      return Objects.equals(ship, that.ship) &&
          direction == that.direction;
    }

    @Override
    public int hashCode() {
      return Objects.hash(ship, direction);
    }
  }
}

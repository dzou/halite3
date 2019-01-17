package tiles;

import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import java.util.ArrayList;
import map.Grid;
import map.LocalCostGrid;
import map.ZoneGrid;
import map.ZonePlan;
import shipagent.MapOracle;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TileScorer {

  private final MapOracle mapOracle;

  final Grid<ArrayList<TileWalk>> tileValueGrid;

  final HashMap<Ship, LocalCostGrid> shipMoveCostCache;

  public TileScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.tileValueGrid = TileValueGrid.create(mapOracle.haliteGrid);
    this.shipMoveCostCache = new HashMap<>();
  }

  public double localGoalScore(Ship ship, Direction dir, Position explorePosition) {
    if (!shipMoveCostCache.containsKey(ship)) {
      shipMoveCostCache.put(ship, LocalCostGrid.create(
          mapOracle.haliteGrid, ship.position, GoalAssignment.LOCAL_SEARCH_RANGE, mapOracle.myShipPositionsMap.keySet()));
    }

    Position shipMovedPosition = ship.position.directionalOffset(dir);

    LocalCostGrid localCostGrid = shipMoveCostCache.get(ship);

    int haliteSumToDest = 0;
    if (mapOracle.distance(ship.position, explorePosition) <= GoalAssignment.LOCAL_SEARCH_RANGE) {
      haliteSumToDest = localCostGrid.getCostToDest(explorePosition, dir);
    } else {
      haliteSumToDest = (int) (localCostGrid.averageDistance() * GoalAssignment.LOCAL_SEARCH_RANGE);
    }

    Position nearestHome = mapOracle.getNearestHome(explorePosition);
    ArrayList<TileWalk> tileWalks = tileValueGrid.get(explorePosition.x, explorePosition.y);

    double best = 0;
    for (int i = 0; i < tileWalks.size(); i++) {
      TileWalk tileWalk = tileWalks.get(i);
      int turnsSpentOnTile = i + 1;
      int rawHaliteReward = tileWalk.haliteGain;

      int turnsInTransit =
          mapOracle.haliteGrid.distance(shipMovedPosition, explorePosition)
          + mapOracle.distance(tileWalk.endpoint, nearestHome);

      if (dir == Direction.STILL) {
        rawHaliteReward += mapOracle.haliteGrid.get(ship.position.x, ship.position.y) / 4;
        turnsInTransit += 1;
      }

      double haliteReward = Math.min(Constants.MAX_HALITE - ship.halite, rawHaliteReward);

      double tollToTile = Math.max(0, haliteSumToDest * 0.1);
      double tollHome = mapOracle.goHomeCost(tileWalk.endpoint) - tileWalk.haliteDiscount * 0.10;
          //(1.0 * ship.halite / Constants.MAX_HALITE) * (mapOracle.goHomeCost(explorePosition));

      best = Math.max(best, (haliteReward - tollToTile - tollHome) / (turnsInTransit + turnsSpentOnTile));
    }

    return best; /* - 0.03 * mapOracle.myInfluenceMap.get(shipMovedPosition.x, shipMovedPosition.y); */
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

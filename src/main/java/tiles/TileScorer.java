package tiles;

import com.google.common.collect.ImmutableSet;
import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import java.util.ArrayList;
import map.Grid;
import map.LocalCostGrid;
import shipagent.MapOracle;

import java.util.HashMap;
import java.util.Objects;

public class TileScorer {

  private final MapOracle mapOracle;

  final Grid<ArrayList<TileWalk>> tileValueGrid;

  final HashMap<Ship, LocalCostGrid> shipMoveCostCache;
  final HashMap<Ship, LocalCostGrid> shipCrowdCostCache;

  public TileScorer(MapOracle mapOracle, Grid<ArrayList<TileWalk>> tileValueGrid) {
    this.mapOracle = mapOracle;
    this.tileValueGrid = tileValueGrid;
    this.shipMoveCostCache = new HashMap<>();
    this.shipCrowdCostCache = new HashMap<>();
  }

  public double mineScore(Ship ship) {
    double haliteMinedPotential = mapOracle.haliteGrid.get(ship.position.x, ship.position.y) * 0.25;
    double actualHaliteMined = Math.min(
        Constants.MAX_HALITE - ship.halite,
        mapOracle.inspireMap.get(ship.position.x, ship.position.y) > 1 ? haliteMinedPotential * 3 : haliteMinedPotential);
    return actualHaliteMined;
  }

  public double oneWayTileScore(Ship ship, Position explorePosition) {
    if (!shipMoveCostCache.containsKey(ship)) {
      shipMoveCostCache.put(ship, LocalCostGrid.create(
          mapOracle.haliteGrid, ship.position, GoalAssignment.LOCAL_SEARCH_RANGE, ImmutableSet.of()));
    }

    LocalCostGrid localCostGrid = shipMoveCostCache.get(ship);

    int haliteSumToDest = 0;
    if (mapOracle.distance(ship.position, explorePosition) <= GoalAssignment.LOCAL_SEARCH_RANGE) {
      haliteSumToDest = localCostGrid.getCostToDest(explorePosition, Direction.STILL);
    } else {
      haliteSumToDest = (int) (mapOracle.averageHaliteOnMap * mapOracle.distance(ship.position, explorePosition));
    }
    double tollToTile = 0.1 * haliteSumToDest;

    ArrayList<TileWalk> tileWalks = tileValueGrid.get(explorePosition.x, explorePosition.y);

    double best = 0;
    for (int i = 0; i < tileWalks.size(); i++) {
      TileWalk tileWalk = tileWalks.get(i);
      int turnsSpentOnTile = i + 1;

      double haliteReward = Math.min(Constants.MAX_HALITE - ship.halite, tileWalk.haliteGain);
      double turnsInTransit = mapOracle.haliteGrid.distance(ship.position, explorePosition) + 1;

      best = Math.max(best, (haliteReward - tollToTile) / (turnsInTransit + turnsSpentOnTile));
    }

    return best;
  }

  public double roundTripTileScore(Ship ship, Direction dir, Position explorePosition) {
    if (!shipCrowdCostCache.containsKey(ship)) {
      shipCrowdCostCache.put(ship, LocalCostGrid.create(
          new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, 0),
          ship.position,
          GoalAssignment.LOCAL_SEARCH_RANGE,
          mapOracle.myShipPositionsMap.keySet()));
    }

    LocalCostGrid shipCrowdedCache = shipCrowdCostCache.get(ship);

    Position shipMovedPosition = ship.position.directionalOffset(dir);
    ArrayList<TileWalk> tileWalks = tileValueGrid.get(explorePosition.x, explorePosition.y);

    double crowdFactor;
    if (mapOracle.distance(ship.position, explorePosition) <= GoalAssignment.LOCAL_SEARCH_RANGE) {
      crowdFactor = shipCrowdedCache.getCostToDest(explorePosition, dir);
    } else {
      crowdFactor = 0;
    }

    double best = 0;
    for (int i = 0; i < tileWalks.size(); i++) {
      TileWalk tileWalk = tileWalks.get(i);
      int turnsSpentOnTile = i + 1;
      Position nearestHome = mapOracle.getNearestHome(tileWalk.endpoint);

      int rawHaliteReward = tileWalk.haliteGain;

      double turnsInTransit = mapOracle.haliteGrid.distance(shipMovedPosition, explorePosition);
          // + mapOracle.distance(tileWalk.endpoint, nearestHome);

      if (dir == Direction.STILL) {
        int multiplier = mapOracle.inspireMap.get(ship.position.x, ship.position.y) > 1 ? 3 : 1;
        rawHaliteReward += multiplier * mapOracle.haliteGrid.get(ship.position.x, ship.position.y) / 4;
        // turnsInTransit += 1;
      }

      double haliteReward = Math.min(Constants.MAX_HALITE - ship.halite, rawHaliteReward);
      double tollHome = mapOracle.goHomeCost(tileWalk.endpoint) - tileWalk.haliteDiscount * 0.10;

      best = Math.max(best, (haliteReward - tollHome) / (turnsInTransit + turnsSpentOnTile + crowdFactor));
    }

    return best;
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

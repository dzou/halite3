package tiles;

import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
import shipagent.MapOracle;

import java.util.HashMap;
import java.util.Objects;

public class TileScorer {

  private static final double[] MINE_RATIOS = {0.44, 0.58, 0.68, 0.76, 0.83};

  private final MapOracle mapOracle;

  private final HashMap<ShipDirectionPair, DjikstraGrid> shipMoveCostCache;

  public TileScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.shipMoveCostCache = new HashMap<>();
  }

  public double localGoalScore(Ship ship, Direction dir, Position explorePosition) {
    if (mapOracle.haliteGrid.distance(ship.position, explorePosition) > GoalFilter.LOCAL_DISTANCE) {
      throw new RuntimeException("unexpected: shouldn't do local tile calc");
    }

    ShipDirectionPair shipDirKey = new ShipDirectionPair(ship, dir);
    if (!shipMoveCostCache.containsKey(shipDirKey)) {
      Position projectionDestination = Position.at(0, 0).directionalOffset(dir);
      Grid<Integer> subGrid = mapOracle.haliteGrid.subGrid(ship.position, GoalFilter.LOCAL_DISTANCE);
      DjikstraGrid subGridCosts = DjikstraGrid.create(subGrid, projectionDestination);
      shipMoveCostCache.put(shipDirKey, subGridCosts);
    }

    Position projectionDestination = Position.at(0, 0).directionalOffset(dir);
    DjikstraGrid djikstraSubGrid = shipMoveCostCache.get(shipDirKey);
    int haliteSumToDest = djikstraSubGrid.haliteGrid.get(0, 0) // Adds the cost to move ship from stay
        + djikstraSubGrid.haliteGrid.get(projectionDestination.x, projectionDestination.y) // cost to move ship from dest
        + djikstraSubGrid.costCache.get(explorePosition.x - ship.position.x, explorePosition.y - ship.position.y); // cost to move to goal

    double best = 0;
    for (int i = 0; i < MINE_RATIOS.length; i++) {
      int turnsSpentOnTile = i + 2;
      if (dir == Direction.STILL) {
        turnsSpentOnTile += 1;
      }

      int haliteOnTile = mapOracle.haliteGrid.get(explorePosition.x, explorePosition.y);
      double haliteMined = Math.min(Constants.MAX_HALITE - ship.halite, haliteOnTile * MINE_RATIOS[i]);
      double haliteReward = Math.min(
          Constants.MAX_HALITE - ship.halite,
          mapOracle.inspireMap.get(explorePosition.x, explorePosition.y) > 1 ? haliteMined * 2.0 : haliteMined);

      int turnsFromDest = mapOracle.haliteGrid.distance(
          ship.position.x, ship.position.y, explorePosition.x, explorePosition.y);

      double tollToTile = Math.max(0, (haliteSumToDest - haliteMined) * 0.1);
      best = Math.max(best, (haliteReward - tollToTile) / (turnsFromDest + turnsSpentOnTile));
    }

    return best;
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

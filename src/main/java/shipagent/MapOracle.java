package shipagent;

import bot.HaliteStatTracker;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Log;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
import map.TriangulationGrid;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains all rich information about the game state.
 */
public class MapOracle {

  private static final int ENEMY_COVER_RANGE = 4;

  public final PlayerId myPlayerId;
  public final Grid<Integer> haliteGrid;
  public final int turnsRemaining;
  public final Collection<Ship> myShips;
  public final Collection<Ship> enemyShips;

  public final Map<Position, DjikstraGrid> myDropoffsMap;
  public final Set<Position> allExistingDropoffs;
  public final Set<Position> enemyDropoffs;

  public final ImmutableMap<Position, Ship> myShipPositionsMap;
  public final ImmutableMap<Position, Ship> enemyShipPositionsMap;

  public final Grid<Double> myInfluenceMap;
  public final Grid<Double> enemyInfluenceMap;

  public final Grid<Integer> enemyThreatMap;

  public final Grid<Integer> inspireMap;
  public final Grid<Integer> controlMap;

  public final BaseManager baseManager;
  public final TriangulationGrid enemyShipCovers;

  public final int haliteSum;
  public final double averageHaliteOnMap;

  public MapOracle(
      PlayerId myPlayerId,
      Grid<Integer> haliteGrid,
      int turnsRemaining,
      Collection<Ship> myShips,
      Collection<Ship> enemyShips,
      Map<PlayerId, Set<Position>> playerBases) {

    this.myPlayerId = myPlayerId;
    this.haliteGrid = haliteGrid;
    this.turnsRemaining = turnsRemaining;
    this.myShips = myShips;
    this.enemyShips = enemyShips;

    this.myShipPositionsMap = myShips.stream().collect(ImmutableMap.toImmutableMap(ship -> ship.position, s -> s));
    this.enemyShipPositionsMap = enemyShips.stream().collect(ImmutableMap.toImmutableMap(ship -> ship.position, s -> s));

    this.myDropoffsMap = playerBases.get(myPlayerId)
        .stream()
        .collect(Collectors.toMap(pos -> pos, pos -> DjikstraGrid.create(haliteGrid, pos)));
    this.allExistingDropoffs = playerBases.values()
        .stream()
        .flatMap(base -> base.stream())
        .collect(ImmutableSet.toImmutableSet());
    this.enemyDropoffs = playerBases.keySet()
        .stream()
        .filter(playerId -> !playerId.equals(myPlayerId))
        .flatMap(player -> playerBases.get(player).stream())
        .collect(ImmutableSet.toImmutableSet());

    this.myInfluenceMap = InfluenceMaps.buildShipInfluenceMap(myShips, haliteGrid);
    this.enemyInfluenceMap = InfluenceMaps.buildShipInfluenceMap(enemyShips, haliteGrid);

    this.enemyThreatMap = InfluenceMaps.threatMap(enemyShips, haliteGrid);

    this.inspireMap = InfluenceMaps.inspiredMap(enemyShips, haliteGrid);
    this.controlMap = InfluenceMaps.getControlMap(haliteGrid, myDropoffsMap.keySet(), enemyDropoffs);

    this.baseManager = new BaseManager(playerBases, haliteGrid);
    this.enemyShipCovers = new TriangulationGrid(enemyShips, ENEMY_COVER_RANGE);

    this.haliteSum = haliteGrid.stream().mapToInt(n -> n).sum();
    this.averageHaliteOnMap = haliteGrid.stream()
        .mapToInt(n -> n)
        .average()
        .orElse(0.0);
  }

  public boolean shouldMakeShip() {
    int shipCount = myShips.size() + enemyShips.size();

    double averageShipConsumptionRate = HaliteStatTracker.getHaliteConsumptionRate(shipCount);
    int haliteSum = HaliteStatTracker.getHaliteSum();

    Log.log("Halite Sum on Map: " + haliteSum);
    Log.log("Ship Consumption Rate: " + averageShipConsumptionRate);

    double avgHalitePotential = 0.25 * haliteSum / (haliteGrid.width * haliteGrid.height);

    return turnsRemaining * avgHalitePotential > 2700
        || averageShipConsumptionRate * turnsRemaining > 1400;
  }

  public int distance(Position origin, Position destination) {
    return haliteGrid.distance(origin, destination);
  }

  public boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, getNearestHome(ship.position)) + 5 + (shipCount / (5 * myDropoffsMap.size())) >= turnsRemaining;
  }

  public Optional<Ship> orderDropOff(Position projectedDropOffLoc) {
    myDropoffsMap.put(projectedDropOffLoc, DjikstraGrid.create(haliteGrid, projectedDropOffLoc));
    return Optional.ofNullable(myShipPositionsMap.get(projectedDropOffLoc));
  }

  public Position nearestEnemyBase(Position origin) {
    return enemyDropoffs.stream()
        .min(Comparator.comparingInt(dropoff -> distance(origin, dropoff)))
        .orElse(origin);
  }

  public Position getNearestHome(Position origin) {
    Comparator<Position> dropOffComparator =
        Comparator.<Position>comparingInt(dropoff -> haliteGrid.distance(origin, dropoff))
            .thenComparingDouble(dropoff -> -influenceDifferenceAtPoint(dropoff.x, dropoff.y))
            .thenComparingInt(dropoff -> dropoff.x * 100 + dropoff.y);

    Position closestDropoff = myDropoffsMap.keySet()
        .stream()
        .min(dropOffComparator)
        .get();

//    return closestDropoff;
    return myDropoffsMap.keySet()
        .stream()
        .filter(pos -> influenceDifferenceAtPoint(pos.x, pos.y) > -3.0 || distance(pos, origin) <= 2)
        .min(dropOffComparator)
        .orElse(closestDropoff);
  }

  public double goHomeCost(Position destination) {
    Position nearestHome = getNearestHome(destination);
    return myDropoffsMap.get(nearestHome).costCache.get(destination.x, destination.y) * 0.10;
  }

  public double influenceDifferenceAtPoint(int x, int y) {
    return myInfluenceMap.get(x, y) - enemyInfluenceMap.get(x, y);
  }
}

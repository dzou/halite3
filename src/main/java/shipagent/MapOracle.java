package shipagent;

import com.google.common.collect.ImmutableMap;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Contains all rich information about the game state.
 */
public class MapOracle {

  public final PlayerId myPlayerId;
  public final Grid<Integer> haliteGrid;
  public final int turnsRemaining;
  public final Collection<Ship> myShips;
  public final Collection<Ship> enemyShips;

  public final Map<Position, DjikstraGrid> myDropoffsMap;

  public final ImmutableMap<Position, Ship> myShipPositionsMap;
  public final ImmutableMap<Position, Ship> enemyShipPositionsMap;

  public final Grid<Double> myInfluenceMap;
  public final Grid<Double> enemyInfluenceMap;

  public final Grid<Integer> myThreatMap;
  public final Grid<Integer> enemyThreatMap;

  public final Grid<Integer> killMap;
  public final Grid<Integer> inspireMap;

  public final Grid<Double> shipHaliteDensityMap;
  public final Grid<Double> haliteDensityMap;

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

    this.myInfluenceMap = InfluenceMaps.buildShipInfluenceMap(myShips, haliteGrid);
    this.enemyInfluenceMap = InfluenceMaps.buildShipInfluenceMap(enemyShips, haliteGrid);

    this.myThreatMap = InfluenceMaps.threatMap(myShips, haliteGrid);
    this.enemyThreatMap = InfluenceMaps.threatMap(enemyShips, haliteGrid);

    this.killMap = InfluenceMaps.killMap(enemyShips, playerBases, haliteGrid, this.myThreatMap, this.myInfluenceMap, this.enemyInfluenceMap);
    this.inspireMap = InfluenceMaps.inspiredMap(enemyShips, haliteGrid);

    this.shipHaliteDensityMap = InfluenceMaps.shipHaliteDensityMap(haliteGrid, myShips);
    this.haliteDensityMap = InfluenceMaps.haliteDensityMap(haliteGrid);
  }

  public Optional<Ship> orderDropOff(Position projectedDropOffLoc) {
    myDropoffsMap.put(projectedDropOffLoc, DjikstraGrid.create(haliteGrid, projectedDropOffLoc));
    return Optional.ofNullable(myShipPositionsMap.get(projectedDropOffLoc));
  }

  Position getNearestHome(Position shipPosition) {
    Comparator<Map.Entry<Position, DjikstraGrid>> dropOffComparator =
        Comparator.<Map.Entry<Position, DjikstraGrid>>comparingInt(entry -> haliteGrid.distance(shipPosition, entry.getKey()))
            .thenComparingDouble(entry -> -myInfluenceMap.get(entry.getKey().x, entry.getKey().y))
            .thenComparingInt(entry -> entry.getKey().x * 100 + entry.getKey().y);

    return myDropoffsMap.entrySet()
        .stream()
        .min(dropOffComparator)
        .get()
        .getKey();
  }

  double goHomeCost(Position destination) {
    Position nearestHome = getNearestHome(destination);
    return myDropoffsMap.get(nearestHome).costCache.get(destination.x, destination.y) * 0.10;
  }

  boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, getNearestHome(ship.position)) + 5 + (shipCount / 5) >= turnsRemaining;
  }

  double getInfluenceAtPoint(int x, int y) {
    return myInfluenceMap.get(x, y) + enemyInfluenceMap.get(x, y);
  }

  boolean friendlyControlPoint(int x, int y) {
    double myInfluence = myInfluenceMap.get(x, y);
    Ship myShip = myShipPositionsMap.get(Position.at(x, y));
    if (myShip != null) {
      myInfluence -= InfluenceMaps.getCrowdFactor(myShip, x, y, haliteGrid);
    }

    return myInfluence >= enemyInfluenceMap.get(x, y);
  }
}

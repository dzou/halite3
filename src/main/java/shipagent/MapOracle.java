package shipagent;

import com.google.common.collect.ImmutableMap;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
import map.TriangulationGrid;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Contains all rich information about the game state.
 */
public class MapOracle {

  private static final int ENEMY_COVER_RANGE = 3;

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

  // public final Grid<Double> exploreGrid;

  public final Grid<Integer> enemyThreatMap;

  public final Grid<Integer> inspireMap;

  public final Grid<Double> shipHaliteDensityMap;
  public final Grid<Double> haliteDensityMap;

  public final TriangulationGrid enemyShipCovers;

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

    // this.exploreGrid = InfluenceMaps.buildExploreMap(myShips, haliteGrid);
    this.myInfluenceMap = InfluenceMaps.buildShipInfluenceMap(myShips, haliteGrid);
    this.enemyInfluenceMap = InfluenceMaps.buildShipInfluenceMap(enemyShips, haliteGrid);

    this.enemyThreatMap = InfluenceMaps.threatMap(enemyShips, haliteGrid);

    this.inspireMap = InfluenceMaps.inspiredMap(enemyShips, haliteGrid);

    this.shipHaliteDensityMap = InfluenceMaps.shipHaliteDensityMap(haliteGrid, myShips);
    this.haliteDensityMap = InfluenceMaps.haliteDensityMap(haliteGrid);

    this.enemyShipCovers = new TriangulationGrid(enemyShips, ENEMY_COVER_RANGE);
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
    return haliteGrid.distance(ship.position, getNearestHome(ship.position)) + 5 + (shipCount / (5 * myDropoffsMap.size())) >= turnsRemaining;
  }

  double influenceSumAtPoint(int x, int y) {
    return myInfluenceMap.get(x, y) + enemyInfluenceMap.get(x, y);
  }

  double influenceDifferenceAtPoint(int x, int y) {
    return myInfluenceMap.get(x, y) - enemyInfluenceMap.get(x, y);
  }

  Collection<Ship> getEnemiesInNeighborhood(Position origin, int distance, int enemyCount) {
    PriorityQueue<Ship> nearestEnemyShips = new PriorityQueue<>(
        Comparator.comparingInt(e -> -haliteGrid.distance(origin, e.position)));

    for (Ship enemy : enemyShips) {
      if (haliteGrid.distance(origin, enemy.position) <= distance) {
        nearestEnemyShips.add(enemy);
        if (nearestEnemyShips.size() > enemyCount) {
          nearestEnemyShips.remove();
        }
      }
    }

    return nearestEnemyShips;
  }
}

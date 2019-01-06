package shipagent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
  public final HashSet<Position> fakeDropoffs;
  public final Set<Position> allExistingDropoffs;

  public final ImmutableMap<Position, Ship> myShipPositionsMap;
  public final ImmutableMap<Position, Ship> enemyShipPositionsMap;

  public final Grid<Double> myInfluenceMap;
  public final Grid<Double> enemyInfluenceMap;

  public final Grid<Integer> enemyThreatMap;

  public final Grid<Integer> inspireMap;

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
    this.fakeDropoffs = new HashSet<>();

    this.myInfluenceMap = InfluenceMaps.buildShipInfluenceMap(myShips, haliteGrid);
    this.enemyInfluenceMap = InfluenceMaps.buildShipInfluenceMap(enemyShips, haliteGrid);

    this.enemyThreatMap = InfluenceMaps.threatMap(enemyShips, haliteGrid);

    this.inspireMap = InfluenceMaps.inspiredMap(enemyShips, haliteGrid);

    this.enemyShipCovers = new TriangulationGrid(enemyShips, ENEMY_COVER_RANGE);

    this.haliteSum = haliteGrid.stream().mapToInt(n -> n).sum();
    this.averageHaliteOnMap = haliteGrid.stream()
        .mapToInt(n -> n)
        .average()
        .orElse(0.0);
  }

  public boolean isNearFakeDropoff(Position explorePosition, int distance) {
    return fakeDropoffs.stream().anyMatch(dropoff -> haliteGrid.distance(explorePosition, dropoff) <= distance);
  }

  public int distance(Position origin, Position destination) {
    return haliteGrid.distance(origin, destination);
  }

  public boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, getNearestHome(ship.position)) + 5 + (shipCount / (5 * myDropoffsMap.size())) >= turnsRemaining;
  }

  public Optional<Ship> orderDropOff(Position projectedDropOffLoc) {
    myDropoffsMap.put(projectedDropOffLoc, DjikstraGrid.create(haliteGrid, projectedDropOffLoc));
    fakeDropoffs.add(projectedDropOffLoc);
    return Optional.ofNullable(myShipPositionsMap.get(projectedDropOffLoc));
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

    return closestDropoff;
//    return myDropoffsMap.keySet()
//        .stream()
//        .filter(pos -> influenceDifferenceAtPoint(pos.x, pos.y) >= 0.0)
//        .min(dropOffComparator)
//        .orElse(closestDropoff);
  }

  public double goHomeCost(Position destination) {
    Position nearestHome = getNearestHome(destination);
    return myDropoffsMap.get(nearestHome).costCache.get(destination.x, destination.y) * 0.10;
  }

  public double influenceDifferenceAtPoint(int x, int y) {
    return myInfluenceMap.get(x, y) - enemyInfluenceMap.get(x, y);
  }
}

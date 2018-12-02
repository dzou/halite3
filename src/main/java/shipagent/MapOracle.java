package shipagent;

import com.google.common.collect.ImmutableMap;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;

import java.util.*;
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

  public final Grid<Double> shipInfluenceMap;
  public final Grid<Integer> enemyThreatMap;
  public final Grid<Integer> killMap;
  public final Grid<Integer> inspireMap;
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

    this.myDropoffsMap = playerBases.get(myPlayerId)
        .stream()
        .collect(Collectors.toMap(pos -> pos, pos -> DjikstraGrid.create(haliteGrid, pos)));

    HashSet<Ship> allShips = new HashSet<>();
    allShips.addAll(myShips);
    allShips.addAll(enemyShips);

    this.shipInfluenceMap = InfluenceMaps.buildShipInfluenceMap(allShips, haliteGrid);
    this.enemyThreatMap = InfluenceMaps.threatMap(enemyShips, haliteGrid);
    this.killMap = InfluenceMaps.killMap(myShips, enemyShips, playerBases, haliteGrid);
    this.inspireMap = InfluenceMaps.inspiredMap(enemyShips, haliteGrid);
    this.haliteDensityMap = InfluenceMaps.haliteDensityMap(haliteGrid, myShips);
  }

  public Optional<Ship> orderDropOff(Position projectedDropOffLoc) {
    myDropoffsMap.put(projectedDropOffLoc, DjikstraGrid.create(haliteGrid, projectedDropOffLoc));
    return Optional.ofNullable(myShipPositionsMap.get(projectedDropOffLoc));
  }

  Position getNearestHome(Position shipPosition) {
    return myDropoffsMap.keySet().stream()
        .min(Comparator.comparingInt(basePosition -> haliteGrid.distance(shipPosition, basePosition)))
        .get();
  }

  double goHomeCost(Position destination) {
    Position nearestHome = getNearestHome(destination);
    return myDropoffsMap.get(nearestHome).costCache.get(destination.x, destination.y) * 0.10;
  }

  boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, getNearestHome(ship.position)) + 5 + (shipCount / 5) >= turnsRemaining;
  }
}

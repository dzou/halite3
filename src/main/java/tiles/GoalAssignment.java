package tiles;

import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.Zone;
import matching.BipartiteGraph;
import matching.HungarianAlgorithm;
import shipagent.MapOracle;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class GoalAssignment {

  private final MapOracle mapOracle;

  final GoalFilter goalFilter;
  final TileScorer tileScorer;
  final ZoneScorer zoneScorer;

  final Map<Position, Position> shipAssignments;
  final Set<Position> tappedPositions;

  public GoalAssignment(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.goalFilter = new GoalFilter(mapOracle);
    this.tileScorer = new TileScorer(mapOracle);
    this.zoneScorer = new ZoneScorer(mapOracle);

    BipartiteGraph graph = new BipartiteGraph();

    for (Ship ship : mapOracle.myShips) {
      HashMap<Position, Double> shipDestinations = new HashMap<>();

      for (TileScoreEntry tileScoreEntry : goalFilter.bestTiles) {
        shipDestinations.put(tileScoreEntry.position, tileScorer.localGoalScore(ship, Direction.STILL, tileScoreEntry.position));
      }

      graph.addSingleCapacityNode(
          ship.position,
          shipDestinations,
          1 + mapOracle.myShips.size() / goalFilter.bestTiles.size());
    }

    for (Position destination : graph.getDestinations()) {
      if (mapOracle.enemyInfluenceMap.get(destination.x, destination.y) > 0.0) {
        int prevCapacity = graph.getCapacity(destination);
        graph.setCapacity(destination, prevCapacity * 3);
      }
    }

    HungarianAlgorithm alg = new HungarianAlgorithm(graph);

    this.shipAssignments = alg.processMatches();
    this.tappedPositions = alg.getTappedDestinations();

    this.shipAssignments.entrySet().forEach(e -> Log.log("ship: " + e.getKey() + " -> " + e.getValue()));
    Log.log(this.tappedPositions.toString());
  }

  public double mineScore(Ship ship) {
    return tileScorer.mineScore(ship);
  }

  public TileScoreEntry scoreLocalTile(Ship ship, Direction dir) {
    TileScoreEntry mineScoreEntry = new TileScoreEntry(
        ship.position, mapOracle.haliteGrid.get(ship.position.x, ship.position.y), 0.0);

    if (dir == Direction.STILL) {
      mineScoreEntry = new TileScoreEntry(
          ship.position, mapOracle.haliteGrid.get(ship.position.x, ship.position.y), tileScorer.mineScore(ship));
      if (mapOracle.myDropoffsMap.containsKey(ship.position)) {
        return mineScoreEntry;
      }
    }

    Position assignedJob = shipAssignments.get(ship.position);
    TileScoreEntry assignedTileEntry = new TileScoreEntry(
        assignedJob,
        mapOracle.haliteGrid.get(assignedJob.x, assignedJob.y),
        tileScorer.localGoalScore(ship, dir, assignedJob));

    TileScoreEntry localTileEntry =
        goalFilter.getLocalMoves(ship, dir).stream()
            .filter(pos -> !tappedPositions.contains(pos))
            .map(pos -> new TileScoreEntry(pos, mapOracle.haliteGrid.get(pos.x, pos.y), tileScorer.localGoalScore(ship, dir, pos)))
            .max(Comparator.comparingDouble(entry -> entry.score))
            .orElse(new TileScoreEntry(ship.position, mapOracle.haliteGrid.get(ship.position.x, ship.position.y), 0.0));

    return Stream.of(mineScoreEntry, assignedTileEntry, localTileEntry)
        .max(Comparator.comparingDouble(entry -> entry.score))
        .get();
  }


  public ZoneScoreEntry scoreZone(Ship ship, Direction dir) {
    return new ZoneScoreEntry(Zone.EMPTY, 0.0);
//    if (dir == Direction.STILL /* && mapOracle.myDropoffsMap.keySet().contains(ship.position) */) {
//      return new ZoneScoreEntry(Zone.EMPTY, 0.0);
//    }
//
//    ZoneScoreEntry bestEntry =
//        goalFilter.getZonesInDirection(ship.position, dir).stream()
//            .filter(zone -> isViableAssignment(ship, zone.bestTile().tilePosition))
//            .map(zone -> new ZoneScoreEntry(zone, zoneScorer.zoneScore(ship, dir, zone)))
//            .max(Comparator.comparingDouble(entry -> entry.score))
//            .orElse(new ZoneScoreEntry(Zone.EMPTY, 0.0));
//
//    if (bestEntry.zone == Zone.EMPTY) {
//      Log.log("Goal filter returned no zones for " + ship.position + " going " + dir);
//    }
//
//    return bestEntry;
  }

  private boolean isViableAssignment(Ship ship, Position goal) {
    return shipAssignments.get(ship.position).equals(goal) /* || freeGoals.contains(goal) */;
  }
}

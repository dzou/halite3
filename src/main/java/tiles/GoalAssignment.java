package tiles;

import com.google.common.collect.ImmutableMap;
import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import matching.BipartiteGraph;
import matching.HungarianAlgorithm;
import shipagent.MapOracle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoalAssignment {

  private static final Position NULL_JOB = Position.at(-1, -1);

  private final MapOracle mapOracle;

  final GoalFilter goalFilter;
  final TileScorer tileScorer;
  final SafetyScorer safetyScorer;

  final Map<Position, Position> shipAssignments;
  final Set<Position> tappedPositions;

  public GoalAssignment(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.safetyScorer = new SafetyScorer(mapOracle);
    this.goalFilter = new GoalFilter(mapOracle);
    this.tileScorer = new TileScorer(mapOracle);

    BipartiteGraph graph = new BipartiteGraph();

    for (Ship ship : mapOracle.myShips) {
      HashMap<Position, Double> shipDestinations = new HashMap<>();

      List<TileScoreEntry> safeGoals = goalFilter.bestTiles.stream()
          .filter(entry -> safetyScorer.isSafeShipMove(ship, entry.position))
          .collect(Collectors.toList());

      for (TileScoreEntry tileScoreEntry : safeGoals) {
        shipDestinations.put(tileScoreEntry.position, tileScorer.localGoalScore(ship, Direction.STILL, tileScoreEntry.position));
      }

      graph.addSingleCapacityNode(
          ship.position,
          shipDestinations,
          1 + mapOracle.myShips.size() / goalFilter.bestTiles.size());

      graph.addSingleCapacityNode(
          ship.position, ImmutableMap.of(NULL_JOB, 0.0), 999);
    }

    for (Position destination : graph.getDestinations()) {
      int prevCapacity = graph.getCapacity(destination);
      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) < 0) {
        graph.setCapacity(destination, prevCapacity * 3);
      }
    }

    long startTime = System.currentTimeMillis();

    HungarianAlgorithm alg = new HungarianAlgorithm(graph);

    this.shipAssignments = alg.processMatches();
    this.tappedPositions = alg.getTappedDestinations();

    long endTime = System.currentTimeMillis();
    Log.log("Goal matching time: " + (endTime - startTime));

//    this.shipAssignments.entrySet().forEach(e -> Log.log("ship: " + e.getKey() + " -> " + e.getValue()));
//    Log.log(this.tappedPositions.toString());
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

    double score;
    if (assignedJob == NULL_JOB) {
      score = 0;
    } else {
      score = DjikstraGrid.isInDirection(ship.position, assignedJob, dir, mapOracle.haliteGrid)
          ? tileScorer.localGoalScore(ship, dir, assignedJob)
          : 0;
    }

    TileScoreEntry assignedTileEntry = new TileScoreEntry(
        assignedJob,
        mapOracle.haliteGrid.get(assignedJob.x, assignedJob.y),
        score);

    TileScoreEntry localTileEntry =
        goalFilter.getLocalMoves(ship, dir).stream()
            .filter(pos -> !tappedPositions.contains(pos)
                || mapOracle.haliteGrid.distance(pos, ship.position) <= 1 && mapOracle.myShipPositionsMap.containsKey(pos))
            .filter(pos -> safetyScorer.isSafeShipMove(ship, pos))
            .map(pos -> new TileScoreEntry(pos, mapOracle.haliteGrid.get(pos.x, pos.y), tileScorer.localGoalScore(ship, dir, pos)))
            .max(Comparator.comparingDouble(entry -> entry.score))
            .orElse(new TileScoreEntry(ship.position, mapOracle.haliteGrid.get(ship.position.x, ship.position.y), 0.0));

    return Stream.of(mineScoreEntry, assignedTileEntry, localTileEntry)
        .max(Comparator.comparingDouble(entry -> entry.score))
        .get();
  }

}

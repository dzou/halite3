package tiles;

import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
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

  final Map<Position, Position> shipAssignments;
  final Set<Position> tappedPositions;

  public GoalAssignment(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.goalFilter = new GoalFilter(mapOracle);
    this.tileScorer = new TileScorer(mapOracle);

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
      int prevCapacity = graph.getCapacity(destination);
      if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) < 0) {
        graph.setCapacity(destination, prevCapacity * 3);
      } else if (mapOracle.isNearFakeDropoff(destination, 5)) {
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
    double score = isInDirection(ship.position, assignedJob, dir, mapOracle.haliteGrid)
        ? tileScorer.localGoalScore(ship, dir, assignedJob)
        : 0;

    if (mapOracle.isNearFakeDropoff(assignedJob, 5)) {
      score *= 3.0;
    }

    TileScoreEntry assignedTileEntry = new TileScoreEntry(
        assignedJob,
        mapOracle.haliteGrid.get(assignedJob.x, assignedJob.y),
        score);


    TileScoreEntry localTileEntry =
        goalFilter.getLocalMoves(ship, dir).stream()
            .filter(pos -> !tappedPositions.contains(pos)) /* || mapOracle.haliteGrid.distance(pos, ship.position) <= 1 ) */ // && mapOracle.myShipPositionsMap.containsKey(pos))
            .map(pos -> new TileScoreEntry(pos, mapOracle.haliteGrid.get(pos.x, pos.y), tileScorer.localGoalScore(ship, dir, pos)))
            .max(Comparator.comparingDouble(entry -> entry.score))
            .orElse(new TileScoreEntry(ship.position, mapOracle.haliteGrid.get(ship.position.x, ship.position.y), 0.0));

    return Stream.of(mineScoreEntry, assignedTileEntry, localTileEntry)
        .max(Comparator.comparingDouble(entry -> entry.score))
        .get();
  }

  static boolean isInDirection(Position origin, Position destination, Direction dir, Grid<Integer> haliteGrid) {
    if (dir == Direction.STILL) {
      return true;
    }

    int dx = DjikstraGrid.getAxisDirection(origin.x, destination.x, haliteGrid.width);
    int dy = DjikstraGrid.getAxisDirection(origin.y, destination.y, haliteGrid.height);

    if (dir == Direction.NORTH && dy <= 0
        || dir == Direction.SOUTH && dy >= 0
        || dir == Direction.WEST && dx <= 0
        || dir == Direction.EAST && dx >= 0
        || dir == Direction.STILL) {
      return true;
    } else {
      return false;
    }
  }
}

package tiles;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
import matching.BipartiteGraph;
import matching.HungarianAlgorithm;
import shipagent.MapOracle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoalAssignment {

  static final int LOCAL_SEARCH_RANGE = 4;

  private static final int MAX_GOALS = 30;

  private static final Position NULL_JOB = Position.at(-1, -1);

  private final MapOracle mapOracle;
  private final Grid<ArrayList<TileWalk>> tileValueGrid;

  final GoalFilter goalFilter;
  final GoalFinder goalFinder;
  final TileScorer tileScorer;
  final SafetyScorer safetyScorer;

  public final Map<Position, Position> shipAssignments;
  final Set<Position> untappedJobs;
  final Set<Position> tappedJobs;

  public GoalAssignment(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.tileValueGrid = TileValueGrid.create(mapOracle.haliteGrid, mapOracle.inspireMap);

    this.safetyScorer = new SafetyScorer(mapOracle);
    this.goalFilter = new GoalFilter(mapOracle.haliteGrid);
    this.goalFinder = new GoalFinder(mapOracle, tileValueGrid);
    this.tileScorer = new TileScorer(mapOracle, tileValueGrid);

    List<TileScoreEntry> bestGoals = goalFinder.getBestPositions(MAX_GOALS);

    BipartiteGraph graph = new BipartiteGraph();
    for (Ship ship : mapOracle.myShips) {
      HashMap<Position, Double> shipDestinations = new HashMap<>();

      List<TileScoreEntry> safeGoals = bestGoals.stream()
          // .filter(entry -> safetyScorer.isGoodTrade(ship, entry.position) || mapOracle.distance(entry.position, ship.position) > LOCAL_SEARCH_RANGE)
          // .filter(entry -> mapOracle.influenceDifferenceAtPoint(entry.position.x, entry.position.y) >= 0)
          .collect(Collectors.toList());

      for (TileScoreEntry tileScoreEntry : safeGoals) {
        shipDestinations.put(tileScoreEntry.position, tileScorer.oneWayTileScore(ship, tileScoreEntry.position));
      }

      graph.addSingleCapacityNode(
          ship.position,
          shipDestinations,
          1 + mapOracle.myShips.size() / bestGoals.size());

      graph.addSingleCapacityNode(
          ship.position, ImmutableMap.of(NULL_JOB, 0.0), 999);
    }

    for (Position destination : graph.getDestinations()) {
      int prevCapacity = graph.getCapacity(destination);
      int enemyStrength = (int) (mapOracle.enemyInfluenceMap.get(destination.x, destination.y) + 1);
      graph.setCapacity(destination, prevCapacity + enemyStrength);
    }

    long startTime = System.currentTimeMillis();

    HungarianAlgorithm alg = new HungarianAlgorithm(graph);

    this.shipAssignments = alg.processMatches();
    this.tappedJobs = alg.getTappedDestinations();
    this.untappedJobs = bestGoals.stream()
        .map(t -> t.position)
        .filter(pos -> !tappedJobs.contains(pos))
        .collect(ImmutableSet.toImmutableSet());

    long endTime = System.currentTimeMillis();
    Log.log("Goal matching time: " + (endTime - startTime));

    /** Debugging **/
    Grid<Character> bestGoalGrid = new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, '.');
    for (TileScoreEntry goal : bestGoals) {
      bestGoalGrid.set(goal.position.x, goal.position.y, '*');
    }
    Log.log(bestGoalGrid.toString());

    Log.log("untapped jobs: " + untappedJobs);

//    this.shipAssignments.entrySet().forEach(e -> Log.log("ship: " + e.getKey() + " -> " + e.getValue()));
//    Log.log(this.tappedPositions.toString());
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
          ? tileScorer.roundTripTileScore(ship, dir, assignedJob)
          : 0;
    }

    TileScoreEntry assignedTileEntry = new TileScoreEntry(
        assignedJob,
        mapOracle.haliteGrid.get(assignedJob.x, assignedJob.y),
        score);

    TileScoreEntry localTileEntry =
        goalFilter.getLocalMoves(ship.position, dir, LOCAL_SEARCH_RANGE).stream()
            .filter(pos -> !tappedJobs.contains(pos) || mapOracle.haliteGrid.distance(pos, ship.position) <= 1)
                /* && mapOracle.myShipPositionsMap.containsKey(pos) */
            // .filter(pos -> mapOracle.i(pos.x, pos.y) >= 0)
            // .filter(pos -> safetyScorer.isGoodTrade(ship, pos))
            .map(pos -> new TileScoreEntry(pos, mapOracle.haliteGrid.get(pos.x, pos.y), tileScorer.roundTripTileScore(ship, dir, pos)))
            .max(Comparator.comparingDouble(entry -> entry.score))
            .orElse(new TileScoreEntry(ship.position, mapOracle.haliteGrid.get(ship.position.x, ship.position.y), 0.0));

    return Stream.of(mineScoreEntry, assignedTileEntry, localTileEntry)
        .max(Comparator.comparingDouble(entry -> entry.score))
        .get();
  }

}

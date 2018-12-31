package tiles;

import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.Zone;
import matching.BipartiteGraph;
import matching.HungarianAlgorithm;
import shipagent.MapOracle;

import java.util.*;
import java.util.stream.Collectors;

public class GoalAssignment {

  private final MapOracle mapOracle;

  final GoalFilter goalFilter;
  final TileScorer tileScorer;
  final ZoneScorer zoneScorer;

  final Map<Position, Position> shipAssignments;
  final Set<Position> freeGoals;

  public GoalAssignment(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.goalFilter = new GoalFilter(mapOracle);
    this.tileScorer = new TileScorer(mapOracle);
    this.zoneScorer = new ZoneScorer(mapOracle);

    BipartiteGraph graph = new BipartiteGraph();

    for (Ship ship : mapOracle.myShips) {
      HashMap<Position, Double> shipDestinations = new HashMap<>();

      for (Zone zone : goalFilter.getZonesInDirection(ship.position, Direction.STILL)) {
        Position zonePos = zone.bestTile().tilePosition;
        shipDestinations.put(zonePos, zoneScorer.zoneScore(ship, Direction.STILL, zone));
      }
      // dummy assignment
      shipDestinations.put(Zone.EMPTY_POSITION, 0.0);

      graph.addSingleCapacityNode(ship.position, shipDestinations);
    }

    List<Zone> topZones = goalFilter.bestZones.stream().limit(5).collect(Collectors.toList());
    for (Zone bestZone : topZones) {
      graph.setCapacity(bestZone.bestTile().tilePosition, mapOracle.myShips.size() / 5 + 1);
    }

//    for (Position dest : graph.getDestinations()) {
//      if (mapOracle.haliteGrid.get(dest.x, dest.y) > 500 && mapOracle.enemyInfluenceMap.get(dest.x, dest.y) > 0.25) {
//        graph.setCapacity(dest, 10);
//      }
//    }

    // dummy assignment
    graph.setCapacity(Zone.EMPTY_POSITION, 999);

    HungarianAlgorithm alg = new HungarianAlgorithm(graph);

    this.shipAssignments = alg.processMatches();
    this.freeGoals = alg.getPositionsWithCapacity();

    this.shipAssignments.entrySet().forEach(e -> Log.log("ship: " + e.getKey() + " -> " + e.getValue()));
    Log.log(this.freeGoals.toString());
  }

  public double mineScore(Ship ship) {
    return tileScorer.mineScore(ship);
  }

  public TileScoreEntry scoreLocalTile(Ship ship, Direction dir) {
    TileScoreEntry bestEntry =
        goalFilter.getLocalMoves(ship, dir).stream()
            .map(pos -> new TileScoreEntry(pos, tileScorer.localGoalScore(ship, dir, pos)))
            .max(Comparator.comparingDouble(entry -> entry.score))
            .orElse(new TileScoreEntry(ship.position, -999));

    if (bestEntry.score <= -999) {
      Log.log("Goal filter returned no tiles for " + ship.position + " going " + dir);
    }

    if (dir == Direction.STILL) {
      TileScoreEntry mineScoreEntry = new TileScoreEntry(ship.position, tileScorer.mineScore(ship));
      // 2nd cond = if ship is on a dropoff just move it
      if (mineScoreEntry.score > bestEntry.score || mapOracle.myDropoffsMap.keySet().contains(ship.position)) {
        return mineScoreEntry;
      }
    }

    return bestEntry;
  }

  public ZoneScoreEntry scoreZone(Ship ship, Direction dir) {
    if (dir == Direction.STILL /* && mapOracle.myDropoffsMap.keySet().contains(ship.position) */) {
      return new ZoneScoreEntry(Zone.EMPTY, 0.0);
    }

    ZoneScoreEntry bestEntry =
        goalFilter.getZonesInDirection(ship.position, dir).stream()
            .filter(zone -> isViableAssignment(ship, zone.bestTile().tilePosition))
            .map(zone -> new ZoneScoreEntry(zone, zoneScorer.zoneScore(ship, dir, zone)))
            .max(Comparator.comparingDouble(entry -> entry.score))
            .orElse(new ZoneScoreEntry(Zone.EMPTY, 0.0));

    if (bestEntry.zone == Zone.EMPTY) {
      Log.log("Goal filter returned no zones for " + ship.position + " going " + dir);
    }

    return bestEntry;
  }

  private boolean isViableAssignment(Ship ship, Position goal) {
    return shipAssignments.get(ship.position).equals(goal) /* || freeGoals.contains(goal) */;
  }
}

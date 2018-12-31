package tiles;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
import map.Zone;
import shipagent.MapOracle;

import java.util.*;
import java.util.stream.Collectors;

public class GoalFilter {

  static final int LOCAL_DISTANCE = 4;

  static final int ZONE_LIMIT = 20;

  static final int TILE_LIMIT = 200;

  private final MapOracle mapOracle;

  final List<Zone> bestZones;

  final List<TileScoreEntry> bestTiles;

  public GoalFilter(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.bestZones = getBestZones(mapOracle, ZONE_LIMIT);
    this.bestTiles = getBestPositions(mapOracle, TILE_LIMIT);

    debugPrint();
  }

  private void debugPrint() {
    Grid<Integer> goodTiles = new Grid<>(mapOracle.haliteGrid.width, mapOracle.haliteGrid.height, 0);
    for (TileScoreEntry e : bestTiles) {
      goodTiles.set(e.position.x, e.position.y, 1);
    }
    for (Position p : mapOracle.myDropoffsMap.keySet()) {
      goodTiles.set(p.x, p.y, 9);
    }
    Log.log(goodTiles.toString() + "\n");
  }

  Set<Position> getLocalMoves(Ship ship, Direction d) {
    ImmutableSet.Builder<Position> localPositions = ImmutableSet.builder();

    int xStart = (d == Direction.EAST) ? 1 : -LOCAL_DISTANCE;
    int xEnd = (d == Direction.WEST) ? -1 : LOCAL_DISTANCE;

    int yStart = (d == Direction.SOUTH) ? 1 : -LOCAL_DISTANCE;
    int yEnd = (d == Direction.NORTH) ? -1 : LOCAL_DISTANCE;

    for (int y = yStart; y <= yEnd; y++) {
      for (int x = Math.max(xStart, -LOCAL_DISTANCE + Math.abs(y));
           x <= Math.min(xEnd, LOCAL_DISTANCE - Math.abs(y));
           x++) {
        int dx = ship.position.x + x;
        int dy = ship.position.y + y;
        localPositions.add(mapOracle.haliteGrid.normalize(Position.at(dx, dy)));
      }
    }

    return localPositions.build();
  }

  Set<Zone> getZonesInDirection(Position origin, Direction d) {
    ImmutableSet.Builder<Zone> filteredPositions = ImmutableSet.builder();

    for (Zone zone : bestZones) {

      if (mapOracle.haliteGrid.distance(origin, zone.bestTile().tilePosition) <= LOCAL_DISTANCE) {
        continue;
      }

      int deltaX = DjikstraGrid.getAxisDirection(origin.x, zone.bestTile().tilePosition.x, this.mapOracle.haliteGrid.width);
      int deltaY = DjikstraGrid.getAxisDirection(origin.y, zone.bestTile().tilePosition.y, this.mapOracle.haliteGrid.height);

      if (d == Direction.EAST && deltaX >= 0
          || d == Direction.WEST && deltaX <= 0
          || d == Direction.NORTH && deltaY <= 0
          || d == Direction.SOUTH && deltaY >= 0
          || d == Direction.STILL) {
        filteredPositions.add(zone);
      }
    }

    return filteredPositions.build();
  }

  private static List<TileScoreEntry> getBestPositions(MapOracle mapOracle, int count) {
    PriorityQueue<TileScoreEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(t -> t.score));

    for (int y = 0; y < mapOracle.haliteGrid.height; y++) {
      for (int x = 0; x < mapOracle.haliteGrid.width; x++) {
        Position curr = Position.at(x, y);
        int distanceHome = mapOracle.distance(curr, mapOracle.getNearestHome(curr)) + 1;

        double inspireMultiplier = (mapOracle.inspireMap.get(x, y) > 1) ? 1.8 : 1.0;

        double score = (inspireMultiplier * mapOracle.haliteGrid.get(x, y) - mapOracle.goHomeCost(curr)) / distanceHome;
        queue.offer(new TileScoreEntry(curr, mapOracle.haliteGrid.get(x, y), score));

        if (queue.size() > count) {
          queue.poll();
        }
      }
    }

    ArrayList<TileScoreEntry> results = new ArrayList<>();
    while (!queue.isEmpty()) {
      results.add(queue.poll());
    }
    Collections.reverse(results);


    int idx = 0;
    int currSum = 0;
    for (idx = 0; idx < results.size() && shouldKeepGoing(mapOracle, currSum, idx); idx++) {
      currSum += results.get(idx).haliteOnTile;
    }

    return results.subList(0, idx);
  }

  private static boolean shouldKeepGoing(MapOracle mapOracle, int currSum, int idx) {
    if (idx < 5) {
      return true;
    }

    if (mapOracle.haliteSum == 0) {
      return false;
    }

    return 1.0 * currSum / mapOracle.haliteSum < 0.10;
  }

  private static List<Zone> getBestZones(MapOracle mapOracle, int count) {
    return mapOracle.zoneGrid.zones.stream()
        .sorted(Comparator.<Zone>comparingDouble(z -> scoreZone(mapOracle, z)).reversed())
        .limit(count)
        .collect(Collectors.toList());
  }

  private static double scoreZone(MapOracle mapOracle, Zone zone) {
    Position zonePos = zone.bestTile().tilePosition;
    Position nearestHome = mapOracle.getNearestHome(zonePos);
    return (zone.topThreeSum() - mapOracle.goHomeCost(zonePos)) / mapOracle.haliteGrid.distance(zonePos, nearestHome);
  }
}

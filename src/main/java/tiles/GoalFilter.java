package tiles;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import shipagent.MapOracle;

import java.util.*;

public class GoalFilter {

  static final int LOCAL_DISTANCE = 4;

  static final int TILE_LIMIT = 200;

  private final MapOracle mapOracle;

  final List<TileScoreEntry> bestTiles;

  public GoalFilter(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
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

  private static List<TileScoreEntry> getBestPositions(MapOracle mapOracle, int count) {
    PriorityQueue<TileScoreEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(t -> t.score));

    for (int y = 0; y < mapOracle.haliteGrid.height; y++) {
      for (int x = 0; x < mapOracle.haliteGrid.width; x++) {
        Position curr = Position.at(x, y);
        int distanceHome = mapOracle.distance(curr, mapOracle.getNearestHome(curr)) + 1;

        double inspireMultiplier = (mapOracle.inspireMap.get(x, y) > 1) ? 2.0 : 1.0;

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

    return 1.0 * currSum / mapOracle.haliteSum < 0.15;
  }
}

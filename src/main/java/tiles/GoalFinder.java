package tiles;

import hlt.Log;
import hlt.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import map.Grid;
import shipagent.MapOracle;

public class GoalFinder {

  private final MapOracle mapOracle;

  private final Grid<ArrayList<TileWalk>> tileValueGrid;

  public GoalFinder(MapOracle mapOracle, Grid<ArrayList<TileWalk>> tileValueGrid) {
    this.mapOracle = mapOracle;
    this.tileValueGrid = tileValueGrid;
  }

  /** Returns at most {@code maxCount} of the best positions to consider, usually fewer. */
  public List<TileScoreEntry> getBestPositions(int maxCount) {
    PriorityQueue<TileScoreEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(t -> t.score));

    for (int y = 0; y < mapOracle.haliteGrid.height; y++) {
      for (int x = 0; x < mapOracle.haliteGrid.width; x++) {
        ArrayList<TileWalk> tileWalks = tileValueGrid.get(x, y);
        TileWalk bestWalk = tileWalks.get(tileWalks.size() - 1);

        Position curr = Position.at(x, y);
        int turnsSpent =
            mapOracle.distance(bestWalk.endpoint, mapOracle.getNearestHome(bestWalk.endpoint))
                // + tileWalks.size()
                + 1;

        double score = (bestWalk.haliteGain - mapOracle.goHomeCost(curr)) / turnsSpent;
        queue.offer(new TileScoreEntry(curr, mapOracle.haliteGrid.get(x, y), score));

        if (queue.size() > maxCount) {
          queue.poll();
        }
      }
    }

    ArrayList<TileScoreEntry> results = new ArrayList<>();
    while (!queue.isEmpty()) {
      results.add(queue.poll());
    }
    Collections.reverse(results);

    return limitResultsToPercentile(results);
  }

  private List<TileScoreEntry> limitResultsToPercentile(List<TileScoreEntry> results) {
    int currSum = 0;

    int idx = 0;
    for (; idx < results.size() && shouldKeepGoing(currSum, idx); idx++) {
      currSum += results.get(idx).haliteOnTile;
    }

    return results.subList(0, idx);
  }

  private boolean shouldKeepGoing(int currSum, int idx) {
    if (idx < 10) {
      return true;
    }

    if (mapOracle.haliteSum == 0) {
      return false;
    }

    return (1.0 * currSum / mapOracle.haliteSum) < 0.25;
  }
}

package map;

import com.google.common.collect.ImmutableList;
import hlt.Position;

import java.util.*;

public class Zone {
  public static final Zone EMPTY = new Zone(Position.at(-1, -1), 0, ImmutableList.of());

  public final Position center;
  public final int haliteSum;
  public final List<Tile> bestHaliteTiles;

  Zone(Position center, int haliteSum, List<Tile> bestHaliteTiles) {
    this.center = center;
    this.haliteSum = haliteSum;
    this.bestHaliteTiles = bestHaliteTiles;
  }

  public Tile bestTile() {
    return bestHaliteTiles.get(0);
  }

  public int topThreeSum() {
    return bestHaliteTiles.stream().mapToInt(t -> t.haliteOnTile).sum();
  }

  @Override
  public String toString() {
    return "{" + haliteSum + "}-" + bestHaliteTiles.toString();
  }

  public static Zone create(int xCorner, int yCorner, Grid<Integer> haliteGrid, int zoneSize) {
    int haliteSum = 0;
    PriorityQueue<Tile> bestHaliteQueue = new PriorityQueue<>(Comparator.comparingInt(t -> t.haliteOnTile));

    for (int y = yCorner; y < yCorner + zoneSize; y++) {
      for (int x = xCorner; x < xCorner + zoneSize; x++) {
        int haliteAtTile = haliteGrid.get(x, y);
        haliteSum += haliteAtTile;

        bestHaliteQueue.add(new Tile(Position.at(x, y), haliteAtTile));
        if (bestHaliteQueue.size() > 3) {
          bestHaliteQueue.poll();
        }
      }
    }


    ArrayList<Tile> bestHaliteTiles = new ArrayList<>();
    while (!bestHaliteQueue.isEmpty()) {
      bestHaliteTiles.add(bestHaliteQueue.poll());
    }
    Collections.reverse(bestHaliteTiles);

    return new Zone(
        Position.at(xCorner + zoneSize / 2, yCorner + zoneSize / 2),
        haliteSum,
        bestHaliteTiles);
  }
}

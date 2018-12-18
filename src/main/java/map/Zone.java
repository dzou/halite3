package map;

import hlt.Position;

import java.util.*;

public class Zone {
  public final Position center;
  public final int haliteSum;
  public final List<Integer> bestHaliteTiles;

  Zone(Position center, int haliteSum, List<Integer> bestHaliteTiles) {
    this.center = center;
    this.haliteSum = haliteSum;
    this.bestHaliteTiles = bestHaliteTiles;
  }

  public double getMinedAmount() {
    return bestHaliteTiles.stream().mapToInt(i -> i).sum() * 0.58;
  }

  public static Zone create(int xCorner, int yCorner, Grid<Integer> haliteGrid, int zoneSize) {
    int haliteSum = 0;

    PriorityQueue<Integer> bestHaliteQueue = new PriorityQueue<>();

    for (int y = yCorner; y < yCorner + zoneSize; y++) {
      for (int x = xCorner; x < xCorner + zoneSize; x++) {
        int haliteAtTile = haliteGrid.get(x, y);
        haliteSum += haliteAtTile;

        bestHaliteQueue.add(haliteAtTile);
        if (bestHaliteQueue.size() > 3) {
          bestHaliteQueue.poll();
        }
      }
    }


    ArrayList<Integer> bestHaliteTiles = new ArrayList<>();
    while (!bestHaliteQueue.isEmpty()) {
      bestHaliteTiles.add(bestHaliteQueue.poll());
    }
    Collections.reverse(bestHaliteTiles);

    return new Zone(
        Position.at(xCorner + zoneSize / 2, yCorner + zoneSize / 2),
        haliteSum,
        bestHaliteTiles);
  }

  @Override
  public String toString() {
    return center + "" + haliteSum + " " + bestHaliteTiles.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Zone zone = (Zone) o;
    return haliteSum == zone.haliteSum &&
        Objects.equals(center, zone.center) &&
        Objects.equals(bestHaliteTiles, zone.bestHaliteTiles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(center, haliteSum, bestHaliteTiles);
  }
}

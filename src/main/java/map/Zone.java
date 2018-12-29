package map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Position;

import java.util.*;
import java.util.stream.Collectors;

public class Zone {
  public static final Position EMPTY_POSITION = Position.at(-1, -1);
  public static final Zone EMPTY = new Zone(Position.at(-1, -1), 0, ImmutableList.of(), ImmutableSet.of());

  public final Position center;
  public final int haliteSum;
  public final List<Tile> bestHaliteTiles;
  public final Set<Position> corners;
  public final Set<Position> centerPoints;

  Zone(Position center, int haliteSum, List<Tile> bestHaliteTiles, Set<Position> corners) {
    this.center = center;
    this.haliteSum = haliteSum;
    this.bestHaliteTiles = bestHaliteTiles;
    this.corners = corners;

    if (!bestHaliteTiles.isEmpty()) {
      this.centerPoints = Direction.ALL_CARDINALS.stream()
          .map(d -> bestHaliteTiles.get(0).tilePosition.directionalOffset(d))
          .collect(ImmutableSet.toImmutableSet());
    } else {
      this.centerPoints = ImmutableSet.of();
    }
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
        bestHaliteTiles,
        ImmutableSet.of(
            Position.at(xCorner, yCorner),
            Position.at(xCorner, yCorner + zoneSize - 1),
            Position.at(xCorner + zoneSize - 1, yCorner),
            Position.at(xCorner + zoneSize - 1, yCorner + zoneSize - 1)
        ));
  }
}

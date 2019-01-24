package map;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Position;

import java.util.*;

public class LocalCostGrid {

  final Map<Direction, int[][]> costCacheMap;
  final Position absoluteCenter;
  final Grid<Integer> haliteGrid;
  final int radius;

  LocalCostGrid(
      Map<Direction, int[][]> costCacheMap,
      Position absoluteCenter,
      Grid<Integer> haliteGrid,
      int radius) {

    this.costCacheMap = costCacheMap;
    this.absoluteCenter = absoluteCenter;
    this.haliteGrid = haliteGrid;
    this.radius = radius;
  }

  public int maxDistance() {
    int[][] costCache = costCacheMap.get(Direction.STILL);
    return Arrays.stream(costCache).flatMapToInt(Arrays::stream).max().orElse(0);
  }

  public double averageDistance() {
    int[][] costCache = costCacheMap.get(Direction.STILL);
    return Arrays.stream(costCache).flatMapToInt(Arrays::stream).average().orElse(0.0);
  }

  public int getCostToDest(Position destination, Direction d) {
    Position mappedPosition = getMappedPosition(destination, absoluteCenter, radius, haliteGrid);

    int[][] costCache = costCacheMap.get(d);
    int costToMove = (d != Direction.STILL) ? haliteGrid.get(absoluteCenter.x, absoluteCenter.y) : 0;
    return costCache[mappedPosition.y][mappedPosition.x] + costToMove - haliteGrid.get(destination.x, destination.y);
  }

  private static Position getMappedPosition(
      Position other,
      Position absoluteCenter,
      int radius,
      Grid<Integer> haliteGrid) {

    int xDelta = DjikstraGrid.getAxisDirection(absoluteCenter.x, other.x, haliteGrid.width);
    int yDelta = DjikstraGrid.getAxisDirection(absoluteCenter.y, other.y, haliteGrid.height);

    return Position.at(radius + xDelta, radius + yDelta);
  }

  public static LocalCostGrid create(
      Grid<Integer> haliteGrid, Position center, int radius, Set<Position> myShipPositions) {

    Set<Position> mappedShipPositions = myShipPositions.stream()
        .filter(p -> !p.equals(center) && haliteGrid.distance(center, p) <= radius)
        .map(p -> getMappedPosition(p, center, radius, haliteGrid))
        .collect(ImmutableSet.toImmutableSet());

    Map<Direction, int[][]> costCacheMap = new HashMap<>();
    Position mappedCenter = Position.at(radius, radius);

    for (Direction d : Direction.values()) {
      int[][] subgrid = createSubgrid(haliteGrid, center, radius);
      costCacheMap.put(d, buildCostCache(subgrid, mappedCenter.directionalOffset(d), mappedShipPositions));
    }

    return new LocalCostGrid(costCacheMap, center, haliteGrid, radius);
  }

  static int[][] buildCostCache(int[][] subgrid, Position mappedOrigin, Set<Position> mappedShipPositions) {
    int[][] costCache = new int[subgrid.length][subgrid.length];

    HashSet<Position> visited = new HashSet<>();

    Queue<Position> queue = new ArrayDeque<>();
    queue.offer(mappedOrigin);

    while (!queue.isEmpty()) {
      Position curr = queue.poll();

      if (curr.y < 0 || curr.y >= subgrid.length || curr.x < 0 || curr.x >= subgrid.length) {
        continue;
      }

      if (visited.contains(curr)) {
        continue;
      }

      visited.add(curr);

      int cost = subgrid[curr.y][curr.x];

      int prevCost = getPrev(curr, mappedOrigin).stream()
          .mapToInt(p -> costCache[p.y][p.x])
          .min()
          .orElse(0);

      int shipCrowdCost = mappedShipPositions.contains(curr) ? 1 : 0;

      costCache[curr.y][curr.x] = cost + prevCost + shipCrowdCost;
      for (Position neighbor : curr.getUnnormalizedNeighbors()) {
        queue.offer(neighbor);
      }
    }

    return costCache;
  }

  static int[][] createSubgrid(Grid<Integer> haliteGrid, Position center, int radius) {
    int[][] subgrid = new int[2 * radius + 1][2 * radius + 1];

    for (int dy = -radius; dy <= radius; dy++) {
      for (int dx = -radius; dx <= radius; dx++) {
        int subGridX = dx + radius;
        int subGridY = dy + radius;
        subgrid[subGridY][subGridX] = haliteGrid.get(center.x + dx, center.y + dy);
      }
    }

    return subgrid;
  }

  private static Set<Position> getPrev(Position curr, Position home) {
    ImmutableSet.Builder<Position> neighbors = ImmutableSet.builder();

    int xDelta = curr.x - home.x;
    int yDelta = curr.y - home.y;

    if (xDelta < 0) {
      neighbors.add(curr.directionalOffset(Direction.EAST));
    } else if (xDelta > 0) {
      neighbors.add(curr.directionalOffset(Direction.WEST));
    }

    if (yDelta < 0) {
      neighbors.add(curr.directionalOffset(Direction.SOUTH));
    } else if (yDelta > 0) {
      neighbors.add(curr.directionalOffset(Direction.NORTH));
    }

    return neighbors.build();
  }
}

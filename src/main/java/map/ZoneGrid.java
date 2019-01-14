package map;

import com.google.common.collect.ImmutableList;
import hlt.Position;
import shipagent.MapOracle;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ZoneGrid {

  private static final int ZONE_RANGE = 2;

  private final Grid<Zone> zoneGrid;

  private final Grid<Integer> haliteGrid;

  ZoneGrid(Grid<Zone> zoneGrid, Grid<Integer> haliteGrid) {
    this.zoneGrid = zoneGrid;
    this.haliteGrid = haliteGrid;
  }

  public List<ZonePlan> getZonePlans(int x, int y, MapOracle mapOracle) {
    Zone zone = zoneGrid.get(x, y);

    return ImmutableList.of(
        shortZonePlan(zone, mapOracle),
        mediumZonePlan(zone, mapOracle),
        longZonePlan(zone, mapOracle));
  }

  Zone getZone(int x, int y) {
    return zoneGrid.get(x, y);
  }

  private ZonePlan shortZonePlan(Zone z, MapOracle mapOracle) {
    double haliteGained = 0.58 * haliteGrid.get(z.origin.x, z.origin.y);
    int turnsSpent = 3;

    if (mapOracle.inspireMap.get(z.origin.x, z.origin.y) > 1) {
      haliteGained *= 2.0;
    }

    return new ZonePlan(haliteGained, turnsSpent);
  }

  private ZonePlan mediumZonePlan(Zone z, MapOracle mapOracle) {
    Position bestNeighbor = z.bestPositions.get(0).position;

    double haliteGained = 0.58 * haliteGrid.get(z.origin.x, z.origin.y);
    if (mapOracle.inspireMap.get(z.origin.x, z.origin.y) > 1) {
      haliteGained *= 2.0;
    }
    haliteGained += 0.44 * z.bestPositions.get(0).halite;

    int turnsSpent = 5 + 2 * mapOracle.distance(z.origin, bestNeighbor);

    return new ZonePlan(haliteGained, turnsSpent);
  }

  private ZonePlan longZonePlan(Zone z, MapOracle mapOracle) {
    double haliteGained = 0.58 * haliteGrid.get(z.origin.x, z.origin.y);
    if (mapOracle.inspireMap.get(z.origin.x, z.origin.y) > 1) {
      haliteGained *= 2.0;
    }
    haliteGained += z.bestPositions.stream().mapToInt(zp -> zp.halite).sum();

    int turnsSpent = 9 + 2 * z.bestPositions.stream().mapToInt(zp -> mapOracle.distance(zp.position, z.origin)).sum();

    return new ZonePlan(haliteGained, turnsSpent);
  }

  public static ZoneGrid create(Grid<Integer> haliteGrid) {
    Grid<Zone> zones = new Grid<>(haliteGrid.width, haliteGrid.height, null);

    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        zones.set(x, y, calculateZone(Position.at(x, y), haliteGrid));
      }
    }

    return new ZoneGrid(zones, haliteGrid);
  }

  private static Zone calculateZone(Position origin, Grid<Integer> haliteGrid) {
    PriorityQueue<ZonePosition> queue = new PriorityQueue<>(
        Comparator.comparingDouble(zp -> 1.0 * zp.halite / (haliteGrid.distance(origin, zp.position) + 1)));

    for (int dy = -ZONE_RANGE; dy <= ZONE_RANGE; dy++) {
      for (int dx = -ZONE_RANGE + Math.abs(dy); dx <= ZONE_RANGE - Math.abs(dy); dx++) {
        int neighborX = origin.x + dx;
        int neighborY = origin.y + dy;

        if (dx == 0 && dy == 0) {
          continue;
        }

        ZonePosition zp = new ZonePosition(
            haliteGrid.normalize(neighborX, neighborY), haliteGrid.get(neighborX, neighborY));
        queue.offer(zp);

        if (queue.size() > 3) {
          queue.poll();
        }
      }
    }

    List<ZonePosition> zonePositions = queue.stream()
        .sorted(Comparator.comparingInt(zp -> -zp.halite))
        .collect(ImmutableList.toImmutableList());
    return new Zone(origin, zonePositions);
  }

  @Override
  public String toString() {
    return zoneGrid.toString();
  }

  static class Zone {
    public Position origin;
    public final List<ZonePosition> bestPositions;

    Zone(Position origin,
        List<ZonePosition> bestPositions) {
      this.origin = origin;
      this.bestPositions = bestPositions;
    }

    @Override
    public String toString() {
      return bestPositions.toString();
    }
  }

  static class ZonePosition {
    final Position position;
    final int halite;

    ZonePosition(Position position, int halite) {
      this.position = position;
      this.halite = halite;
    }

    @Override
    public String toString() {
      return "" + position + halite;
    }
  }
}

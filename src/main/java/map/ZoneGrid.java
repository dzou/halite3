package map;

import com.google.common.collect.ImmutableList;
import hlt.Direction;
import hlt.Position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ZoneGrid {

  private static int ZONE_SIZE = 4;

  private final Grid<Integer> haliteGrid;

  public final Grid<Zone> zones;

  public ZoneGrid(Grid<Integer> haliteGrid) {
    this.haliteGrid = haliteGrid;
    this.zones = new Grid<>(
        (haliteGrid.width + ZONE_SIZE - 1) / ZONE_SIZE,
        (haliteGrid.height + ZONE_SIZE - 1) / ZONE_SIZE,
        null);

    for (int y = 0; y < zones.height; y++) {
      for (int x = 0; x < zones.width; x++) {
        int cornerX = x * ZONE_SIZE;
        int cornerY = y * ZONE_SIZE;

        Zone zone = Zone.create(cornerX, cornerY, haliteGrid, ZONE_SIZE);

        this.zones.set(x, y, zone);
      }
    }
  }

//  public List<Zone> getBestZones(Position origin, Direction d, int count) {
//    ArrayList<Zone> zonesInDirection = zonesInDirection(origin, d);
//
//    return zonesInDirection.stream()
//        .sorted(Comparator.comparingDouble(z -> -z.getMinedAmount()))
//        .limit(count)
//        .collect(ImmutableList.toImmutableList());
//  }

  public ArrayList<Zone> zonesInDirection(Position origin, Direction d) {
    int homeZoneX = origin.x / ZONE_SIZE;
    int homeZoneY = origin.y / ZONE_SIZE;

    int xStart = homeZoneX - (zones.width / 2);
    int xEnd = homeZoneX + (zones.width / 2);

    int yStart = homeZoneY - (zones.height / 2);
    int yEnd = homeZoneY + (zones.height / 2);

    if (d == Direction.EAST) {
      xStart = homeZoneX;
    } else if (d == Direction.WEST) {
      xEnd = homeZoneX;
    } else if (d == Direction.SOUTH) {
      yStart = homeZoneY;
    } else if (d == Direction.NORTH) {
      yEnd = homeZoneY;
    }

    ArrayList<Zone> results = new ArrayList<>();

    for (int y = yStart; y <= yEnd; y++) {
      for (int x = xStart; x <= xEnd; x++) {
        Zone goalZone = zones.get(x, y);

        int deltaX = Math.abs(DjikstraGrid.getAxisDirection(origin.x, goalZone.center.x, this.haliteGrid.width));
        int deltaY = Math.abs(DjikstraGrid.getAxisDirection(origin.y, goalZone.center.y, this.haliteGrid.height));

        if ((d == Direction.NORTH || d == Direction.SOUTH) && deltaX > 2 * deltaY) {
          continue;
        }

        if ((d == Direction.EAST || d == Direction.WEST) && deltaY > 2 * deltaX) {
          continue;
        }

        results.add(goalZone);
      }
    }

    return results;
  }

  @Override
  public String toString() {
    return zones.toString();
  }
}

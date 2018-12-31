package tiles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Zone;
import shipagent.MapOracle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GoalFilter {

  static final int LOCAL_DISTANCE = 4;

  static final int ZONE_LIMIT = 20;

  private final MapOracle mapOracle;

  final List<Zone> bestZones;

  public GoalFilter(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.bestZones = getBestZones(mapOracle, ZONE_LIMIT);
  }

  Set<Position> getLocalMoves(Ship ship, Direction d) {
    ImmutableSet.Builder<Position> localPositions = ImmutableSet.builder();

    int xStart = (d == Direction.EAST) ? 0 : -LOCAL_DISTANCE;
    int xEnd = (d == Direction.WEST) ? 0 : LOCAL_DISTANCE;

    int yStart = (d == Direction.SOUTH) ? 0 : -LOCAL_DISTANCE;
    int yEnd = (d == Direction.NORTH) ? 0 : LOCAL_DISTANCE;

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

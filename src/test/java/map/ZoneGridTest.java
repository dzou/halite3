package map;

import hlt.Position;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ZoneGridTest {

//  @Test
//  public void testZoneDirections() {
//    Grid<Integer> grid = new Grid<>(12, 12, 100);
//    ZoneGrid zoneGrid = new ZoneGrid(grid);
//
//    System.out.println(zoneGrid);
//
//    List<Zone> zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.WEST);
//    assertThat(zones).hasSize(3);
//    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
//        Position.at(10, 2), Position.at(10, 6), Position.at(10, 10));
//
//    zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.EAST);
//    assertThat(zones).hasSize(3);
//    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
//        Position.at(6, 2), Position.at(6, 6), Position.at(6, 10));
//
//    zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.NORTH);
//    assertThat(zones).hasSize(3);
//    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
//        Position.at(2, 10), Position.at(6, 10), Position.at(10, 10));
//
//    zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.SOUTH);
//    assertThat(zones).hasSize(3);
//    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
//        Position.at(2, 6), Position.at(6, 6), Position.at(10, 6));
//
//    zones = zoneGrid.zonesInDirection(Position.at(0, 0), Direction.WEST);
//    assertThat(zones).hasSize(2);
//    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
//        Position.at(10, 2), Position.at(10, 10));
//
//    zones.forEach(z -> System.out.println(z));
//  }

  @Test
  public void testDenseGrid() {
    Integer[][] rawGrid = {
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {1, 1, 3, 3, 4, 4, 4, 4},
        {1, 1, 3, 3, 4, 1, 1, 7},
        {1, 1, 3, 3, 4, 1, 1, 5},
        {1, 1, 3, 3, 4, 4, 4, 6},
    };

    Grid<Integer> grid = new Grid<>(rawGrid);
    ZoneGrid zoneGrid = new ZoneGrid(grid);

    System.out.println(zoneGrid);

    Zone zone = zoneGrid.zones.get(1, 1);

    assertThat(zone.center).isEqualTo(Position.at(6, 6));
    assertThat(zone.haliteSum).isEqualTo(58);
    assertThat(zone.bestHaliteTiles).containsExactly(
        new Tile(Position.at(7, 5), 7),
        new Tile(Position.at(7, 6), 5),
        new Tile(Position.at(7, 7), 6));

    assertThat(zone.bestTile()).isEqualTo(new Tile(Position.at(7, 5), 7));
  }
}

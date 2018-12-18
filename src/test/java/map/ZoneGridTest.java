package map;

import com.google.common.collect.ImmutableList;
import hlt.Direction;
import hlt.Position;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class ZoneGridTest {

  @Test
  public void testZoneDirections() {
    Grid<Integer> grid = new Grid<>(12, 12, 100);
    ZoneGrid zoneGrid = new ZoneGrid(grid);

    System.out.println(zoneGrid);

    List<Zone> zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.WEST);
    assertThat(zones).hasSize(3);
    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
        Position.at(10, 2), Position.at(10, 6), Position.at(10, 10));

    zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.EAST);
    assertThat(zones).hasSize(3);
    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
        Position.at(6, 2), Position.at(6, 6), Position.at(6, 10));

    zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.NORTH);
    assertThat(zones).hasSize(3);
    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
        Position.at(2, 10), Position.at(6, 10), Position.at(10, 10));

    zones = zoneGrid.zonesInDirection(Position.at(2, 2), Direction.SOUTH);
    assertThat(zones).hasSize(3);
    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
        Position.at(2, 6), Position.at(6, 6), Position.at(10, 6));

    zones = zoneGrid.zonesInDirection(Position.at(0, 0), Direction.WEST);
    assertThat(zones).hasSize(2);
    assertThat(zones.stream().map(z -> z.center).collect(Collectors.toList())).containsExactly(
        Position.at(10, 2), Position.at(10, 10));

    zones.forEach(z -> System.out.println(z));
  }

  @Test
  public void testDenseGrid() {
    Integer[][] rawGrid = {
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {1, 1, 3, 3, 4, 4, 4, 4},
        {1, 1, 3, 3, 4, 1, 1, 4},
        {1, 1, 3, 3, 4, 1, 1, 5},
        {1, 1, 3, 3, 4, 4, 4, 6},
    };

    Grid<Integer> grid = new Grid<>(rawGrid);
    ZoneGrid zoneGrid = new ZoneGrid(grid);

    System.out.println(zoneGrid);

    Grid<Zone> expectedGrid = new Grid<>(new Zone[][]
        {
            {
                new Zone(Position.at(2, 2), 32, ImmutableList.of(2, 2, 2)),
                new Zone(Position.at(6, 2), 48, ImmutableList.of(8, 8, 8))
            },
            {
                new Zone(Position.at(2, 6), 32, ImmutableList.of(3, 3, 3)),
                new Zone(Position.at(6, 6), 55, ImmutableList.of(6, 5, 4))
            }
        });

    assertThat(zoneGrid.zones).isEqualTo(expectedGrid);
  }
}

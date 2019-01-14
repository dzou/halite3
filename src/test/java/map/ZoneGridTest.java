package map;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class ZoneGridTest {

  @Test
  public void testZoneCreation() {
    Integer[][] rawGrid = {
        {2, 4, 2, 2, 2, 1, 1, 5},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 8, 2, 1, 1, 8},
        {1, 1, 3, 3, 4, 4, 4, 4},
        {1, 1, 3, 3, 4, 1, 1, 7},
        {1, 1, 3, 3, 4, 1, 1, 5},
        {1, 1, 3, 3, 4, 4, 4, 6},
    };
    Grid<Integer> haliteGrid = new Grid<>(rawGrid);
    ZoneGrid zoneGrid = ZoneGrid.create(haliteGrid);

    ZoneGrid.Zone zone = zoneGrid.getZone(0, 0);

    ImmutableList<Integer> halites = zone.bestPositions.stream()
        .map(zonePosition -> zonePosition.halite)
        .collect(ImmutableList.toImmutableList());

    assertThat(halites).containsExactly(6, 5, 4, 8);
    System.out.println(zoneGrid);
  }
}

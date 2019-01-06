package map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import shipagent.MapOracle;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class HaliteDensityGridTest {

  @Test
  public void testHaliteDensityGrid() {
    Grid<Integer> haliteGrid = new Grid<>(24, 24, 0);
    haliteGrid.set(10, 10, 2000);
    haliteGrid.set(9, 9, 2000);
    haliteGrid.set(10, 9, 2000);
    haliteGrid.set(4, 4, 500);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(16, 12, 400),
        ship(12, 19, 900)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(0, 0, 1000)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    HaliteDensityGrid haliteDensityGrid = HaliteDensityGrid.create(mapOracle);

    assertThat(haliteDensityGrid.getDropoffCandidate().get()).isEqualTo(Position.at(10, 9));

    System.out.println(haliteDensityGrid);
  }
}

package shipagent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class MapOracleTest {
  @Test
  public void testCompareInfluences() {

    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(0, 2, 200),
        ship(0, 0, 200),
        ship(2, 0, 200)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500),
        ship(3, 2, 500)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    System.out.println(mapOracle.influenceDifferenceAtPoint(1, 1));
    assertThat(mapOracle.influenceDifferenceAtPoint(1, 1)).isGreaterThan(0.0);
  }
}

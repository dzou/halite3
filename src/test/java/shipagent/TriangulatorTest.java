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

public class TriangulatorTest {

  @Test
  public void testOrientToAxes() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(10, 10, 200)
    );

//    ImmutableList<Ship> enemyShips = ImmutableList.of(
//        ship(11, 9, 0),
//        ship(10, 0, 0),
//        ship(20, 10, 0),
//        ship(0, 20, 0)
//    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(0, 0, 0),
        ship(20, 0, 0),
        ship(0, 20, 0),
        ship(20, 20, 0)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    Triangulator triangulator = new Triangulator(mapOracle);

    Triangulator.Party party = triangulator.triangulateTarget(Position.at(10, 10), enemyShips).get();
    assertThat(party.triangulationMoves).hasSize(4);
    System.out.println(party);
  }

}

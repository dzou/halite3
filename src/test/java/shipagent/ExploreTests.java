package shipagent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;

import static util.TestUtil.ship;

public class ExploreTests {

  @Test
  public void testExploreTriangles() {
    Grid<Integer> haliteGrid = new Grid<>(64, 64, 0);

    haliteGrid.set(30, 0, 1000);
    haliteGrid.set(32, 0, 1000);

    haliteGrid.set(63, 30, 1000);

    Ship myShip = ship(31, 31, 0);

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        ImmutableList.of(myShip),
        ImmutableList.of(),
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    MoveScorer scorer = new MoveScorer(mapOracle);
    scorer.getDecisions(myShip).stream().forEach(s -> System.out.println(s));
  }
}

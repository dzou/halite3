package shipagent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;

import java.util.Comparator;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class ExploreTests {

  @Test
  public void testExploreScoring() {

    Grid<Integer> haliteGrid = new Grid<>(64, 64, 0);

    for (int i = 0; i < 64; i++) {
      if (i % 2 == 0) {
        haliteGrid.set(i, 0, 100);
      } else {
        haliteGrid.set(i, 0, 500);
      }
    }

    for (int i = 0; i < 64; i++) {
      if (i % 2 == 0) {
        haliteGrid.set(i, 63, 100);
      } else {
        haliteGrid.set(i, 63, 200);
      }
    }

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

    Direction best = scorer.getDecisions(myShip).stream().max(Comparator.comparingDouble(d -> d.scoreVector.explorePotentialScore)).get().direction;
    assertThat(best).isEqualTo(Direction.NORTH);
  }

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

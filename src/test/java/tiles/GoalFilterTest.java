package tiles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;
import shipagent.MapOracle;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class GoalFilterTest {

  private static final String STILL_FILTER =
      "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 1 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ 1 1 1 1 1 1 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 1 1 1 3 _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n";


  private static final String WEST_FILTER =
      "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ 1 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ 1 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ 1 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ 1 _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n" +
          "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ \n";

  private static final List<Position> GOOD_POSITIONS =
      ImmutableList.of(
          Position.at(3, 3),
          Position.at(18, 16),
          Position.at(20, 3),
          Position.at(3, 20),
          Position.at(20, 20));

  @Test
  public void testTileFilterSet() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 40);
    GOOD_POSITIONS.forEach(p -> haliteGrid.set(p.x, p.y, 500));

    Ship myShip = ship(15, 15, 200);

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        ImmutableList.of(myShip),
        ImmutableList.of(),
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(4, 4))));
    GoalFilter goalFilter = new GoalFilter(mapOracle);

    for (Direction d : Direction.values()) {
      System.out.println(display(goalFilter, myShip, d));
    }

    Grid<Character> filterMap = display(goalFilter, myShip, Direction.STILL);
    assertThat(filterMap.toString()).isEqualTo(STILL_FILTER);

    filterMap = display(goalFilter, myShip, Direction.WEST);
    assertThat(filterMap.toString()).isEqualTo(WEST_FILTER);
  }

  Grid<Character> display(GoalFilter filter, Ship ship, Direction dir) {
    ArrayList<Position> affectedPositions = new ArrayList<>();
    affectedPositions.addAll(filter.getLocalMoves(ship, dir));

    Grid<Character> result = new Grid<>(32, 32, '_');
    for (Position p : affectedPositions) {
      if (GOOD_POSITIONS.contains(p)) {
        result.set(p.x, p.y, '3');
      } else {
        result.set(p.x, p.y, '1');
      }
    }

    return result;
  }
}

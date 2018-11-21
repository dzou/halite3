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
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static hlt.Direction.*;
import static util.TestUtil.ship;

public class MoveScorerTest {

  @Test
  public void testKillScore() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(2, 0, 0),
        ship(0, 2, 0),
        ship(4, 2, 0),
        ship(2, 4, 0)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500)
    );

    MoveScorer scorer = new MoveScorer(
        haliteGrid,
        Position.at(0, 0),
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    Set<Decision> decisionSet = scorer.getDecisions(ship(2, 0));
    assertThat(getBest(decisionSet)).isEqualTo(SOUTH);

    decisionSet = scorer.getDecisions(ship(0, 2));
    assertThat(getBest(decisionSet)).isEqualTo(EAST);
    decisionSet.stream().forEach(s -> System.out.println(s));
  }

  @Test
  public void testAvoidCrowding() {
    Grid<Integer> haliteGrid = new Grid<>(21, 21, 0);
    haliteGrid.set(10, 0, 1000);
    haliteGrid.set(0, 10, 500);
    haliteGrid.set(20, 10, 700);
    haliteGrid.set(10, 20, 100);


    Ship s1 = ship(10, 0);
    Ship s2 = ship(10, 1);
    Ship me = ship(10, 10);
    ImmutableList<Ship> myShips = ImmutableList.of(s1, s2, me);

    // System.out.println(InfluenceMaps.buildShipInfluenceMap(myShips, haliteGrid));

    MoveScorer moveScorer = new MoveScorer(haliteGrid, Position.at(0, 0), 9999, myShips, ImmutableList.of(), ImmutableMap.of());

    Set<Decision> decisionSet = moveScorer.getDecisions(me);
    decisionSet.stream().forEach(s -> System.out.println(s));
    assertThat(getBest(decisionSet)).isEqualTo(EAST);
  }

  @Test
  public void testShouldWaitToMine() {
    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 1000, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {500, 0, 0, 0, 0, 0, 0, 0, 700},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 100, 0, 0, 0, 0},
    };
    Grid<Integer> haliteGrid = new Grid<>(rawHaliteGrid);

    Ship s1 = ship(4, 0, 600);
    Ship s2 = ship(5, 0, 0);
    Ship s3 = ship(5, 1, 0);
    Ship s4 = ship(4, 1, 0);

    Ship me = ship(4, 4);

    ImmutableList<Ship> myShips = ImmutableList.of(s1, s2, s3, s4, me);

    MoveScorer moveScorer = new MoveScorer(haliteGrid, Position.at(0, 0), 9999, myShips, ImmutableList.of(), ImmutableMap.of());

    Set<Decision> decisionSet = moveScorer.getDecisions(s3);
    decisionSet.stream().forEach(s -> System.out.println(s));
    assertThat(getBest(decisionSet) == NORTH || getBest(decisionSet) == WEST).isTrue();

    decisionSet = moveScorer.getDecisions(s2);
    assertThat(getBest(decisionSet)).isEqualTo(WEST);

  }

  @Test
  public void testDirectionalMoveScoreFilter() {
    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 1000, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {500, 0, 0, 0, 0, 0, 0, 0, 700},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 100, 0, 0, 0, 0},
    };

    Grid<Integer> haliteGrid = new Grid<>(rawHaliteGrid);

    Ship ship = ship(4, 4);
    ImmutableList<Ship> myShips = ImmutableList.of(ship);

    MoveScorer moveScorer = new MoveScorer(haliteGrid, Position.at(0, 0), 9999, myShips, ImmutableList.of(), ImmutableMap.of());

    Set<Decision> decisionSet = moveScorer.getDecisions(ship);

    List<Direction> dirList = decisionSet.stream()
        .sorted(Comparator.comparingDouble(d -> d.scoreVector.exploreScore))
        .map(d -> d.direction)
        .collect(ImmutableList.toImmutableList());
    assertThat(dirList)
        .containsExactly(SOUTH, WEST, EAST, STILL, NORTH)
        .inOrder();

    // decisionSet.stream().forEach(s -> System.out.println(s));
  }

  @Test
  public void testShipBasicStayScore() {

    Integer[][] haliteField = {
        {100, 100, 100, 100, 100},
        {100, 100, 100, 200, 100},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
    };
    Grid<Integer> grid = new Grid<>(haliteField);

    Ship ship = ship(3, 1, 250);
    ImmutableList<Ship> myShips = ImmutableList.of(ship);

    MoveScorer moveScorer = new MoveScorer(grid, Position.at(0, 0), 9999, myShips, ImmutableList.of(), ImmutableMap.of());

    Set<Decision> decisionSet = moveScorer.getDecisions(ship);

    // decisionSet.stream().forEach(s -> System.out.println(s));
    Direction bestDecision = decisionSet.stream().max(Comparator.comparingDouble(d -> d.scoreVector.score())).get().direction;
    assertThat(bestDecision).isEqualTo(STILL);
  }

  private static Direction getBest(Set<Decision> decisions) {
    return decisions.stream().max(Comparator.comparingDouble(d -> d.scoreVector.score())).get().direction;
  }
}

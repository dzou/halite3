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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static hlt.Direction.*;
import static util.TestUtil.ship;

public class MoveScorerTest {

  @Test
  public void testKillScore2() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(2, 0, 0),
        ship(0, 2, 0),
        ship(2, 4, 0)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    MoveScorer scorer = new MoveScorer(mapOracle);

    Set<Decision> decisionSet = scorer.getDecisions(ship(2, 0));
    assertThat(getBest(decisionSet)).isEqualTo(SOUTH);

    decisionSet = scorer.getDecisions(ship(0, 2));
    assertThat(getBest(decisionSet)).isEqualTo(EAST);

    decisionSet = scorer.getDecisions(ship(2, 4));
    decisionSet.stream().forEach(s -> System.out.println(s));
  }


  @Test
  public void testKillScore() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(1, 0, 0),
        ship(3, 0, 0)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 0, 500)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    MoveScorer scorer = new MoveScorer(mapOracle);

    Set<Decision> decisionSet = scorer.getDecisions(ship(1, 0));
    assertThat(getBest(decisionSet)).isEqualTo(EAST);

    decisionSet = scorer.getDecisions(ship(3, 0));
    assertThat(getBest(decisionSet)).isNotEqualTo(WEST);
  }

  @Test
  public void testInspireBonus() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);
    haliteGrid.set(1, 1, 500);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(1, 1)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(0, 0, 500),
        ship(2, 0, 500)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    MoveScorer scorer = new MoveScorer(mapOracle);

    assertThat(scorer.scorePosition(ship(1, 1), Position.at(1, 1)).tileScore()).isEqualTo(375.0);
  }

  @Test
  public void testAvoidCrowding2() {
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

    Ship me = ship(4, 4, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(s1, s2, s3, s4, me);

    MapOracle mapOracle = new MapOracle(new PlayerId(0), haliteGrid, 9999, myShips, ImmutableList.of(), ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    MoveScorer moveScorer = new MoveScorer(mapOracle);

    // Set<Decision> decisionSet = moveScorer.getDecisions(s3);
    // assertThat(getBest(decisionSet) == EAST || getBest(decisionSet) == SOUTH).isTrue();

    Set<Decision> decisionSet = moveScorer.getDecisions(s2);
    decisionSet.stream().forEach(s -> System.out.println(s));
    assertThat(getBest(decisionSet)).isEqualTo(WEST);
  }

  @Test
  public void testShipInfluenceScoring() {
    Grid<Integer> haliteGrid = new Grid<>(21, 21, 0);

    List<Ship> ships = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        ships.add(ship(i, j));
      }
    }

    System.out.println(InfluenceMaps.buildShipInfluenceMap(ships, haliteGrid));

//    MoveScorer moveScorer = new MoveScorer(haliteGrid, Position.at(0, 0), 9999, ships, ImmutableList.of(), ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
//
//    Set<Decision> decisionSet = moveScorer.getDecisions(me);
//    decisionSet.stream().forEach(s -> System.out.println(s));
//    assertThat(getBest(decisionSet)).isEqualTo(EAST);
  }

  @Test
  public void testLocalMoveScore() {
    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 200, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 200, 0, 0, 0, 0},
        {50, 0, 0, 0, 50, 0, 0, 0, 70},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 10, 0, 0, 0, 0},
    };

    Grid<Integer> haliteGrid = new Grid<>(rawHaliteGrid);

    Ship ship = ship(4, 4, 100);
    ImmutableList<Ship> myShips = ImmutableList.of(ship);

    MapOracle mapOracle = new MapOracle(new PlayerId(0), haliteGrid, 9999, myShips, ImmutableList.of(), ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    MoveScorer moveScorer = new MoveScorer(mapOracle);

    Set<Decision> decisions = moveScorer.getDecisions(ship);
    decisions.stream().forEach(s -> System.out.println(s));

    Direction best = decisions.stream()
        .max(Comparator.comparingDouble(d -> d.scoreVector.tileScore()))
        .get()
        .direction;

    assertThat(best).isEqualTo(Direction.NORTH);
  }

  @Test
  public void testExplorePotentialGreedyFirst() {
    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 0,   0, 0, 0, 0},
        {0, 0, 0, 0, 800, 0, 0, 0, 0},
        {0, 0, 0, 0, 0,   0, 0, 0, 0},
        {0, 0, 0, 0, 0,   0, 0, 0, 0},
        {0, 600,  0, 0,   0, 0, 0, 700, 0},
        {0, 0, 0, 0, 0,   0, 0, 0, 0},
        {0, 0, 0, 0, 0,   0, 0, 0, 0},
        {0, 0, 0, 0, 100, 0, 0, 0, 0},
        {0, 0, 0, 0, 0,   0, 0, 0, 0},
    };

    Grid<Integer> haliteGrid = new Grid<>(rawHaliteGrid);

    Ship ship = ship(4, 4);
    ImmutableList<Ship> myShips = ImmutableList.of(ship);

    MapOracle mapOracle = new MapOracle(new PlayerId(0), haliteGrid, 9999, myShips, ImmutableList.of(), ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    MoveScorer moveScorer = new MoveScorer(mapOracle);

    Set<Decision> decisionSet = moveScorer.getDecisions(ship);
    decisionSet.stream().forEach(s -> System.out.println(s));

    List<Direction> dirList = decisionSet.stream()
        .sorted(Comparator.comparingDouble(d -> d.scoreVector.tileScore()))
        .map(d -> d.direction)
        .collect(ImmutableList.toImmutableList());
    assertThat(dirList)
        .containsExactly(SOUTH, EAST, STILL, WEST, NORTH)
        .inOrder();

  }

  @Test
  public void testShipBasicStayScore() {

    Integer[][] haliteField = {
        {100, 100, 100, 100, 100},
        {100, 100, 100, 200, 50},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
    };
    Grid<Integer> grid = new Grid<>(haliteField);

    Ship ship = ship(3, 1, 250);
    ImmutableList<Ship> myShips = ImmutableList.of(ship);

    MapOracle mapOracle = new MapOracle(new PlayerId(0), grid, 9999, myShips, ImmutableList.of(), ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    MoveScorer moveScorer = new MoveScorer(mapOracle);

    Set<Decision> decisionSet = moveScorer.getDecisions(ship);

    decisionSet.stream().forEach(s -> System.out.println(s));
    Direction bestDecision = decisionSet.stream().max(Comparator.comparingDouble(d -> d.scoreVector.score())).get().direction;
    assertThat(bestDecision).isEqualTo(STILL);
  }

  private static Direction getBest(Set<Decision> decisions) {
    return decisions.stream().max(Comparator.comparingDouble(d -> d.scoreVector.score())).get().direction;
  }
}

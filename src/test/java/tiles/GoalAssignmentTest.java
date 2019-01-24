package tiles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;
import org.junit.Test;
import shipagent.MapOracle;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static hlt.Direction.*;
import static util.TestUtil.ship;

public class GoalAssignmentTest {

  @Test
  public void testGoalAssignmentsAreGood() {
    Grid<Integer> haliteGrid = new Grid<>(21, 21, 0);
    haliteGrid.set(10, 0, 1000);
    haliteGrid.set(0, 10, 500);
    haliteGrid.set(20, 10, 700);
    haliteGrid.set(10, 20, 100);

    Ship s1 = ship(10, 0, 500);
    Ship s2 = ship(10, 1, 500);
    Ship me = ship(10, 10);
    ImmutableList<Ship> myShips = ImmutableList.of(s1, s2, me);

    MapOracle mapOracle = new MapOracle(new PlayerId(0), haliteGrid, 9999, myShips, ImmutableList.of(), ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(10, 10))));
    GoalAssignment assignment = new GoalAssignment(mapOracle);

    System.out.println(assignment.shipAssignments);
    System.out.println(assignment.tappedJobs);
    System.out.println(assignment.tileScorer.tileValueGrid.get(0, 10));
    System.out.println(assignment.tileScorer.tileValueGrid.get(20, 10));

    assertThat(assignment.shipAssignments).isEqualTo(ImmutableMap.of(
        Position.at(10, 10), Position.at(20, 10),
        Position.at(10, 0), Position.at(10, 0),
        Position.at(10, 1), Position.at(10, 20)));
  }


  @Test
  public void testLocalMiningCorrectValues() {

    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 50, 100, 50, 0, 0, 0},
        {0, 0, 0, 50, 200, 550, 0, 0, 0},
        {0, 0, 0, 550, 100, 300, 0, 0, 0},
        {0, 0, 0, 100, 400, 300, 0, 0, 0},
        {0, 0, 0, 100, 500, 100, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
    };
    Grid<Integer> haliteGrid = new Grid<>(rawHaliteGrid);

    Ship myShip = ship(4, 4, 217);

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        ImmutableList.of(myShip),
        ImmutableList.of(),
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));
    GoalAssignment goalAssignment = new GoalAssignment(mapOracle);

    for (Direction d : Direction.values()) {
      TileScoreEntry entry = goalAssignment.scoreLocalTile(myShip, d);
      System.out.println(d + ": " + entry);
    }
    System.out.println(goalAssignment.tileScorer.tileValueGrid.get(5, 3));
    System.out.println(goalAssignment.tileScorer.tileValueGrid.get(4, 3));
    System.out.println(goalAssignment.tileScorer.tileValueGrid.get(5, 4));

    ImmutableMap<Direction, Double> moveScoreMap = Arrays.stream(Direction.values())
        .collect(ImmutableMap.toImmutableMap(d -> d, d -> goalAssignment.scoreLocalTile(myShip, d).score));

    assertThat(moveScoreMap.get(NORTH)).isGreaterThan(moveScoreMap.get(EAST));
  }

  @Test
  public void testLocalMiningCorrectPrioritization() {
    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 20, 50, 20, 40, 700, 0, 0},
        {0, 0, 30, 50, 25, 25, 20, 0, 0},
        {99, 0, 20, 50, 30, 60, 40, 0, 0},
        {0, 0, 40, 10, 30, 30, 33, 0, 0},
        {0, 0, 10, 10, 20, 40, 70, 0, 0},
        {0, 0, 0, 0, 500, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
    };
    Grid<Integer> haliteGrid = new Grid<>(rawHaliteGrid);

    Ship myShip = ship(4, 4, 200);

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        ImmutableList.of(myShip),
        ImmutableList.of(),
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(4, 4))));
    GoalAssignment goalAssignment = new GoalAssignment(mapOracle);

    for (Direction d : Direction.values()) {
      TileScoreEntry entry = goalAssignment.scoreLocalTile(myShip, d);
      System.out.println(d + ": " + entry);
    }

    ImmutableMap<Direction, Double> moveScoreMap = Arrays.stream(Direction.values())
        .collect(ImmutableMap.toImmutableMap(d -> d, d -> goalAssignment.scoreLocalTile(myShip, d).score));

    assertThat(moveScoreMap.get(NORTH)).isGreaterThan(moveScoreMap.get(EAST));
    assertThat(moveScoreMap.get(EAST)).isGreaterThan(moveScoreMap.get(SOUTH));
    assertThat(moveScoreMap.get(SOUTH)).isGreaterThan(moveScoreMap.get(WEST));
  }

  @Test
  public void testCorrectDirectionalAwareness() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    boolean x = DjikstraGrid.isInDirection(Position.at(0, 0), Position.at(0, 21), NORTH, haliteGrid);
    assertThat(x).isTrue();

    x = DjikstraGrid.isInDirection(Position.at(0, 0), Position.at(0, 21), SOUTH, haliteGrid);
    assertThat(x).isFalse();

    x = DjikstraGrid.isInDirection(Position.at(14, 16), Position.at(5, 21), SOUTH, haliteGrid);
    assertThat(x).isTrue();

    x = DjikstraGrid.isInDirection(Position.at(14, 16), Position.at(5, 21), WEST, haliteGrid);
    assertThat(x).isTrue();

    x = DjikstraGrid.isInDirection(Position.at(14, 16), Position.at(5, 21), EAST, haliteGrid);
    assertThat(x).isFalse();

    x = DjikstraGrid.isInDirection(Position.at(14, 16), Position.at(5, 21), NORTH, haliteGrid);
    assertThat(x).isFalse();
  }
}

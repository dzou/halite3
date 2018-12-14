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
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static hlt.Direction.*;
import static util.TestUtil.ship;

public class LocalMiningTest {

  @Test
  public void testLocalMiningEstimation() {

    Integer[][] rawHaliteGrid = {
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  50,  100,  50,  0,  0,  0},
        { 0,  0,  0,  50,  200,  550,  0,  0,  0},
        { 0,  0,  0,  550,  300,  300,  0,  0,  0},
        { 0,  0,  0,  100,  400,  300,  0,  0,  0},
        { 0,  0,  0,  100,  500,  100,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
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
    MoveScorer scorer = new MoveScorer(mapOracle);

    Set<Decision> decisionSet = scorer.getDecisions(myShip);
    decisionSet.stream().forEach(d -> System.out.println(d));

    assertThat(
        decisionSet.stream().max(Comparator.comparingDouble(d -> d.scoreVector.localMoveScore)).get().direction).isEqualTo(STILL);

    ImmutableMap<Direction, Double> scoreMap =
        decisionSet.stream().collect(ImmutableMap.toImmutableMap(d -> d.direction, d-> d.scoreVector.localMoveScore));
    assertThat(scoreMap.get(NORTH)).isGreaterThan(scoreMap.get(EAST));
  }

  @Test
  public void testLocalMiningMoveCostsAccurate() {

    Integer[][] rawHaliteGrid = {
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  50,  300,  50,  0,  0,  0},
        { 0,  0,  0,  50,  200,  550,  0,  0,  0},
        { 0,  0,  0,  550,  100,  300,  0,  0,  0},
        { 0,  0,  0,  100,  400,  300,  0,  0,  0},
        { 0,  0,  0,  100,  500,  100,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0,  0,  0},
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
    MoveScorer scorer = new MoveScorer(mapOracle);

    Set<Decision> decisionSet = scorer.getDecisions(myShip);
    decisionSet.stream().forEach(d -> System.out.println(d));

    assertThat(
        decisionSet.stream().max(Comparator.comparingDouble(d -> d.scoreVector.localMoveScore)).get().direction).isEqualTo(WEST);

    ImmutableMap<Direction, Double> scoreMap =
        decisionSet.stream().collect(ImmutableMap.toImmutableMap(d -> d.direction, d-> d.scoreVector.localMoveScore));
    assertThat(scoreMap.get(NORTH)).isGreaterThan(scoreMap.get(EAST));
  }
}

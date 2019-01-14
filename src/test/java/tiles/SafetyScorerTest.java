package tiles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;
import shipagent.MapOracle;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class SafetyScorerTest {

  @Test
  public void shipIsUnsafe_basic() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    Ship myShip = ship(0, 0, 0);
    Ship enemy = ship(2, 0, 0);

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        ImmutableList.of(myShip),
        ImmutableList.of(enemy),
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(16, 16))));

    SafetyScorer safetyScorer = new SafetyScorer(mapOracle);
    assertThat(safetyScorer.safetyScore(myShip, Position.at(1, 0))).isEqualTo(0.0);
  }

  @Test
  public void shipIsSafe_outNumber() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    Ship myShip = ship(0, 0, 0);
    Ship ally = ship(31, 0, 0);
    Ship enemy = ship(1, 0, 0);

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        ImmutableList.of(myShip, ally),
        ImmutableList.of(enemy),
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(16, 16))));

    SafetyScorer safetyScorer = new SafetyScorer(mapOracle);
    assertThat(safetyScorer.safetyScore(myShip, Position.at(0, 0))).isEqualTo(0.0);
  }
}

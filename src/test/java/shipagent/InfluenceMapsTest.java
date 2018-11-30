package shipagent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class InfluenceMapsTest {

  @Test
  public void testRetreatMap() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(2, 0, 100),
        ship(0, 2, 200),
        ship(2, 4, 300),
        ship(4, 2, 300)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500)
    );

    Map<PlayerId, Set<Position>> playerBases = ImmutableMap.of(
        new PlayerId(0),
        ImmutableSet.of(Position.at(8, 23), Position.at(0, 0)));

    Grid<Integer> killMap = InfluenceMaps.killMap(myShips, enemyShips, playerBases, haliteGrid);
    System.out.println(killMap);
  }

  @Test
  public void testKillMapIgnoreEnemyCrowded() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(2, 0, 100),
        ship(0, 2, 200),
        ship(2, 4, 300)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500),
        ship(3, 2, 100),
        ship(4, 2, 200)
    );

    Map<PlayerId, Set<Position>> playerBases = ImmutableMap.of(
        new PlayerId(0),
        ImmutableSet.of(Position.at(8, 23), Position.at(0, 0)));


    Grid<Integer> killMap = InfluenceMaps.killMap(myShips, enemyShips, playerBases, haliteGrid);
    System.out.println(killMap);
  }

  @Test
  public void testKillMap() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(2, 0, 100),
        ship(0, 2, 200),
        ship(2, 4, 300),
        ship(4, 2, 300)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500)
    );

    Map<PlayerId, Set<Position>> playerBases = ImmutableMap.of(
        new PlayerId(0),
        ImmutableSet.of(Position.at(8, 23), Position.at(0, 0)));


    Grid<Integer> killMap = InfluenceMaps.killMap(myShips, enemyShips, playerBases, haliteGrid);
    assertThat(killMap.get(2, 1)).isEqualTo(300);
    assertThat(killMap.get(1, 2)).isEqualTo(300);
  }

  @Test
  public void testKillMapBasicAttempt() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(2, 0, 100),
        ship(0, 2, 200),
        ship(2, 4, 300)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500)
    );

    Map<PlayerId, Set<Position>> playerBases = ImmutableMap.of(
        new PlayerId(0),
        ImmutableSet.of(Position.at(8, 23), Position.at(0, 0)));

    Grid<Integer> killMap = InfluenceMaps.killMap(myShips, enemyShips, playerBases, haliteGrid);
    System.out.println(killMap);
    assertThat(killMap.get(2, 1)).isEqualTo(300);
    assertThat(killMap.get(1, 2)).isEqualTo(300);
  }

  @Test
  public void testCannotKill() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(0, 2, 200),
        ship(2, 4, 300),
        ship(2, 4, 300)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(2, 2, 500)
    );

    Map<PlayerId, Set<Position>> playerBases = ImmutableMap.of(
        new PlayerId(0),
        ImmutableSet.of(Position.at(8, 23), Position.at(0, 0)));

    Grid<Integer> killMap = InfluenceMaps.killMap(myShips, enemyShips, playerBases, haliteGrid);
    // System.out.println(killMap);
    assertThat(killMap.get(2, 1)).isEqualTo(0);
    assertThat(killMap.get(1, 2)).isEqualTo(0);
  }

  @Test
  public void testShipInfluenceMap() {
    Grid<Integer> haliteGrid = new Grid<>(32, 32, 0);

    ImmutableList<Ship> ships = ImmutableList.of(
        ship(5, 5, 0),
        ship(6, 6, 0),
        ship(14, 14, 500)
    );

    Grid<Double> infGrid = InfluenceMaps.buildShipInfluenceMap(ships, haliteGrid);
    // System.out.println(infGrid);

    assertThat(infGrid.get(5, 5)).isWithin(0.001).of(1.2);
    assertThat(infGrid.get(6, 6)).isWithin(0.001).of(1.2);
    assertThat(infGrid.get(14, 14)).isWithin(0.001).of(0.5);
  }

  @Test
  public void testEnemyDangerMaps() {
    Grid<Integer> haliteGrid = new Grid<>(5, 5, 0);

    ImmutableList<Ship> ships = ImmutableList.of(
        ship(0, 0, 100),
        ship(1, 0, 500),
        ship(3, 3, 100)
    );

    Grid<Integer> enemyMap = InfluenceMaps.threatMap(ships, haliteGrid);

    Integer[][] expectedRaw = {
        {100, 100, 500, -1, 100},
        {100, 500, -1, -1, -1},
        {-1, -1, -1, 100, -1},
        {-1, -1, 100, 100, 100},
        {100, 500, -1, 100, -1}
    };
    Grid<Integer> expected = new Grid<>(expectedRaw);
    assertThat(enemyMap).isEqualTo(expected);
    // System.out.println(enemyMap);
  }

  @Test
  public void testInspireMaps() {
    Grid<Integer> haliteGrid = new Grid<>(16, 16, 0);

    ImmutableList<Ship> ships = ImmutableList.of(
        ship(0, 0, 100),
        ship(6, 2, 500)
    );

    Grid<Integer> inspireMap = InfluenceMaps.inspiredMap(ships, haliteGrid);
    assertThat(inspireMap.get(4, 0)).isEqualTo(2);
    assertThat(inspireMap.get(3, 1)).isEqualTo(2);
    assertThat(inspireMap.get(2, 2)).isEqualTo(2);

    // System.out.println(inspireMap);
  }
}

package tiles;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

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

import shipagent.MapOracle;

public class TileValueGridTest {

  @Test
  public void testTileValuesBasicGrid() {
    Integer[][] rawGrid = new Integer[][] {
        {400, 200, 100},
        {500, 150, 200},
        {  0,   0,  20}
    };
    Grid<Integer> haliteGrid = new Grid<>(rawGrid);
    Grid<Integer> inspireGrid = new Grid<>(3, 3, 0);

    Grid<ArrayList<TileWalk>> tileValueGrid =
        TileValueGrid.create(haliteGrid, inspireGrid);

    System.out.println(tileValueGrid);

  }


  @Test
  public void testTileValuesBasicGrid2() {
    Integer[][] rawHaliteGrid = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
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

    MapOracle mapOracle = new MapOracle(new PlayerId(0), haliteGrid, 9999, myShips, ImmutableList.of(), ImmutableMap
        .of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    TileScorer tileScorer = new TileScorer(
        mapOracle,
        TileValueGrid.create(mapOracle.haliteGrid, mapOracle.inspireMap));

    double x = tileScorer.roundTripTileScore(ship, Direction.NORTH, Position.at(4, 3));
    System.out.println(x);

    x = tileScorer.roundTripTileScore(ship, Direction.STILL, Position.at(4, 4));
    System.out.println(x);
  }
}

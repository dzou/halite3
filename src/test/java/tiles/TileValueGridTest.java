package tiles;

import map.Grid;
import org.junit.Test;

import java.util.ArrayList;

public class TileValueGridTest {

  @Test
  public void testTileValuesBasicGrid() {
    Integer[][] rawGrid = new Integer[][] {
        {400, 200, 100},
        {500, 150, 200},
        {  0,   0,  20}
    };
    Grid<Integer> haliteGrid = new Grid<>(rawGrid);

    Grid<ArrayList<TileWalk>> tileValueGrid = TileValueGrid.create(haliteGrid);

    System.out.println(tileValueGrid);

  }
}

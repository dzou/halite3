package matching;

import grid.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import shipagent.ShipRouter;

import static util.TestUtil.ship;

public class BipartiteGraphTest {

  @Test
  public void testGraphLoading() {
    Integer[][] simpleGrid = {
        {3, 4, 3, 1},
        {2, 1, 1, 1},
        {4, 5, 8, 2},
        {4, 5, 3, 1}
    };
    Grid<Integer> grid = new Grid(simpleGrid);
    ShipRouter router = new ShipRouter(grid, Position.at(0, 3));

    Ship s1 = ship(0, 0);
    Ship s2 = ship(2, 0);

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
  }
}

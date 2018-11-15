package matching;

import grid.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import shipagent.ShipRouter;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class BipartiteGraphTest {

  @Test
  public void testGraphLoading() {
    Integer[][] simpleGrid = {
        {1000, 500, 1000, 1000},
        {500, 500, 500, 1000},
        {100, 100, 100, 100},
        {100, 100, 100, 100},
    };
    Grid<Integer> grid = new Grid(simpleGrid);
    ShipRouter router = new ShipRouter(grid, Position.at(0, 3));

    Ship s1 = ship(0, 0, 200);
    Ship s2 = ship(2, 0, 200);

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    bipartiteGraph.addShip(s1, router.getDecisions(s1));
    bipartiteGraph.addShip(s2, router.getDecisions(s2));

    System.out.println(bipartiteGraph);

    assertThat(bipartiteGraph.ships).hasSize(2);
    assertThat(bipartiteGraph.destinations).hasSize(8);
  }
}

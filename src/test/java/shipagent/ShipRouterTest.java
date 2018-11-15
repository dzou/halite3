package shipagent;

import com.google.common.collect.ImmutableList;
import grid.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import util.TestUtil;

public class ShipRouterTest {

  @Test
  public void testShipRouting() {

    Integer[][] haliteField = {
        {100, 100, 100, 100, 100},
        {100, 100, 100, 200, 100},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
    };
    Grid<Integer> grid = new Grid<>(haliteField);

    ShipRouter shipRouter = new ShipRouter(grid, Position.at(2, 2));
    Ship ship = TestUtil.ship(2, 2);

    shipRouter.routeShips(ImmutableList.of(ship));
  }

}

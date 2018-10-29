package shipagent;

import com.google.common.collect.ImmutableList;
import grid.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;

import java.util.Map;

import static util.TestUtil.ship;

public class ShipRouterTest {

  @Test
  public void testShipRouting() {
    // 9 x 9 map.
    Integer[][] rawGrid = {
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 7, 00, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10},
    };

    Position base = Position.at(2, 2);
    Grid<Integer> grid = new Grid(rawGrid);
    ShipRouter shipRouter = new ShipRouter(grid, base);

    ImmutableList<Ship> ships = ImmutableList.of(
        ship(1, 2, 2),
        ship(8, 0)
    );

    Map<Ship, GatherDecision> mappings =  shipRouter.routeShips(ships);

    mappings.entrySet().forEach(e -> System.out.println(e.getKey() + " + " + e.getValue()));
  }

}

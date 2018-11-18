package shipagent;

import com.google.common.collect.ImmutableList;
import grid.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import util.TestUtil;

import java.util.HashSet;

import static util.TestUtil.ship;

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

  @Test
  public void testShipRouterScoring() {

    Integer[][] simpleGrid = {
        {000, 500, 000, 500},
        {000, 000, 500, 000},
        {000, 100, 000, 100},
        {000, 000, 000, 000},
    };
    Grid<Integer> grid = new Grid(simpleGrid);
    ShipRouter router = new ShipRouter(grid, Position.at(0, 3));

    Ship s4 = ship(3, 1, 000);
    HashSet<Decision> decisions = router.getDecisions(s4);

    decisions.forEach(d -> System.out.println(d.direction + d.destination.toString() + ": " + d.score));
  }

}

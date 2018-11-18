package shipagent;

import com.google.common.collect.ImmutableList;
import map.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import util.TestUtil;

import java.util.HashSet;
import java.util.Set;

import static util.TestUtil.ship;

public class ShipRouterTest {

  @Test
  public void testShipRoutingScores() {

    Integer[][] haliteField = {
        {100, 100, 100, 100, 100},
        {100, 100, 100, 200, 100},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
    };
    Grid<Integer> grid = new Grid<>(haliteField);

    ShipRouter shipRouter = new ShipRouter(grid, Position.at(0, 0), 9999);
    Ship ship = TestUtil.ship(3, 1, 250);

    Set<Decision> decisionSet = shipRouter.getDecisions(ship, 1);
    decisionSet.stream().forEach(s -> System.out.println(s));
  }


}

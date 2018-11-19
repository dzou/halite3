package shipagent;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import map.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import util.TestUtil;

import java.util.Comparator;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
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

    Ship ship = ship(3, 1, 250);

    ShipRouter shipRouter = new ShipRouter(grid, Position.at(0, 0), 9999, ImmutableSet.of(ship), ImmutableSet.of());

    Set<Decision> decisionSet = shipRouter.getDecisions(ship);

    decisionSet.stream().forEach(s -> System.out.println(s));
    Direction bestDecision = decisionSet.stream().max(Comparator.comparingDouble(d -> d.scoreVector.score())).get().direction;
    assertThat(bestDecision).isEqualTo(Direction.STILL);
  }

  @Test
  public void testScoreDecisionsWithEnemies() {

    Integer[][] haliteField = {
        {100, 100, 100, 100, 100},
        {100, 100, 1000, 200, 100},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
    };
    Grid<Integer> grid = new Grid<>(haliteField);

    Ship myShip = ship(2, 2, 350);
    Ship enemyShip = ship(2, 1, 100);

    ShipRouter shipRouter = new ShipRouter(
        grid, Position.at(0, 0), 9999, ImmutableSet.of(myShip), ImmutableSet.of(enemyShip));

    Set<Decision> decisionSet = shipRouter.getDecisions(myShip);

    System.out.println(shipRouter.routeShips());

    decisionSet.stream().forEach(s -> System.out.println(s));
//    Direction bestDecision = decisionSet.stream().max(Comparator.comparingDouble(d -> d.score)).get().direction;
//    assertThat(bestDecision).isEqualTo(Direction.STILL);
  }

}

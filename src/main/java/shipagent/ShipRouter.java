package shipagent;

import grid.Grid;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.GravityGrids;

import java.util.*;

/**
 * This guy tells ships what to do.
 */
public class ShipRouter {

  private final Position home;

  private final Grid<Integer> haliteGrid;
  private final Grid<Double> gravityGrid;

  public ShipRouter(Grid<Integer> haliteGrid, Position home) {
    this.haliteGrid = haliteGrid;
    this.home = home;
    this.gravityGrid = GravityGrids.createGravityGrid(haliteGrid);
  }

  private Decision makeDecision(Ship ship) {
    HashSet<Decision> allDecisions = new HashSet<>();
    allDecisions.add(new Decision(
        Direction.STILL,
        ship.position,
        gravityGrid.get(ship.position.x, ship.position.y)
            + haliteGrid.get(ship.position.x, ship.position.y) * 0.25));


    if (ship.halite >= haliteGrid.get(ship.position.x, ship.position.y) / 10) {
      for (Direction offset : Direction.ALL_CARDINALS) {
        Position neighbor = ship.position.directionalOffset(offset);
        Decision decision = new Decision(
            offset,
            neighbor,
            gravityGrid.get(neighbor.x, neighbor.y));
        allDecisions.add(decision);
      }
    }

    return allDecisions.stream().max(Comparator.comparingDouble(d -> d.score)).get();
  }

  public Map<Ship, Decision> routeShips(Collection<Ship> ships) {
    HashMap<Ship, Decision> shipDecisions = new HashMap<>();
    for (Ship ship : ships) {
      shipDecisions.put(ship, makeDecision(ship));
    }
    return shipDecisions;
  }
}

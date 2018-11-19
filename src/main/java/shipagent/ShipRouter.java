package shipagent;

import map.Grid;
import hlt.*;
import matching.BipartiteGraph;
import matching.Edge;

import java.util.*;

/**
 * This guy tells ships what to do.
 */
public class ShipRouter {

  private final Grid<Integer> haliteGrid;

  private final Position home;

  private final Collection<Ship> myShips;

  private final MoveScorer moveScorer;

  public ShipRouter(
      Grid<Integer> haliteGrid,
      Position home,
      int turnsRemaining,
      Collection<Ship> myShips,
      Collection<Ship> enemyShips) {

    this.haliteGrid = haliteGrid;
    this.home = home;
    this.myShips = myShips;
    this.moveScorer = new MoveScorer(haliteGrid, home, turnsRemaining, myShips, enemyShips);
  }

  public HashMap<Ship, Position> routeShips() {
    HashSet<Edge> result = new HashSet<>();

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    for (Ship ship : myShips) {
      if (moveScorer.isTimeToEndGame(ship, myShips.size()) && haliteGrid.distance(ship.position, home) <= 1) {
        result.add(Edge.manualEdge(ship, home));
      } else {
        HashSet<Decision> decisions = getDecisions(ship);
        bipartiteGraph.addShip(ship, decisions);

        Log.log("SHIP " + ship.id);
        for (Decision d : decisions) {
          Log.log(d.toString());
        }
      }
    }
    result.addAll(bipartiteGraph.matchShipsToDestinations());

    HashMap<Ship, Position> shipDecisions = new HashMap<>();
    for (Edge e : result) {
      shipDecisions.put(e.start.ship.get(), e.destination.position);
    }
    return shipDecisions;
  }

  public HashSet<Decision> getDecisions(Ship ship) {
    HashSet<Decision> allDecisions = new HashSet<>();
    allDecisions.add(new Decision(
        Direction.STILL,
        ship.position,
        moveScorer.scorePosition(ship, ship.position)));

    if (ship.halite >= haliteGrid.get(ship.position.x, ship.position.y) / 10) {
      for (Direction offset : Direction.ALL_CARDINALS) {
        Position neighbor = haliteGrid.normalize(ship.position.directionalOffset(offset));
        Decision decision = new Decision(
            offset,
            neighbor,
            moveScorer.scorePosition(ship, neighbor));
        allDecisions.add(decision);
      }
    }

    return allDecisions;
  }

}

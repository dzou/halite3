package shipagent;

import hlt.Log;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import matching.BipartiteGraph;
import matching.Edge;

import java.util.*;

/**
 * This guy tells ships what to do.
 */
public class ShipRouter {

  private final Grid<Integer> haliteGrid;

  private final Collection<Ship> myShips;

  private final MoveScorer moveScorer;

  public ShipRouter(
      PlayerId playerId,
      Grid<Integer> haliteGrid,
      int turnsRemaining,
      Collection<Ship> myShips,
      Collection<Ship> enemyShips,
      Map<PlayerId, Set<Position>> playerBases) {

    this.haliteGrid = haliteGrid;
    this.myShips = myShips;
    this.moveScorer = new MoveScorer(playerId, haliteGrid, turnsRemaining, myShips, enemyShips, playerBases);
  }

  public HashMap<Ship, Position> routeShips() {
    HashSet<Edge> result = new HashSet<>();

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    for (Ship ship : myShips) {
      Position home = moveScorer.getNearestHome(ship.position);
      if (moveScorer.isTimeToEndGame(ship, myShips.size()) && haliteGrid.distance(ship.position, home) <= 1) {
        result.add(Edge.manualEdge(ship, home));
      } else {
        HashSet<Decision> decisions = moveScorer.getDecisions(ship);
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
}

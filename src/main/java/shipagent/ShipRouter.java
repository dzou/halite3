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

  private final MapOracle mapOracle;

  private final MoveScorer moveScorer;

  public ShipRouter(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.moveScorer = new MoveScorer(mapOracle);
  }

  public HashMap<Ship, Position> routeShips() {
    return routeShips(Collections.emptySet());
  }

  public HashMap<Ship, Position> routeShips(Set<Ship> excludeShips) {
    HashSet<Edge> result = new HashSet<>();

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    for (Ship ship : mapOracle.myShips) {
      if (excludeShips.contains(ship)) {
        continue;
      }

      Position home = mapOracle.getNearestHome(ship.position);
      if (mapOracle.isTimeToEndGame(ship, mapOracle.myShips.size())
          && mapOracle.haliteGrid.distance(ship.position, home) <= 1) {
        result.add(Edge.manualEdge(ship, home));
      } else {
        HashSet<Decision> decisions = moveScorer.getDecisions(ship);
        bipartiteGraph.addShip(ship, decisions);

        Log.log("SHIP " + ship.id + " " + ship.position);
        Log.log("Home: " + mapOracle.getNearestHome(ship.position));
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

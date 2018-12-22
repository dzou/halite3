package shipagent;

import com.google.common.collect.ImmutableMap;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import matching.BipartiteGraph;
import matching.HungarianAlgorithm;

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
    HashMap<Ship, Position> shipDecisions = new HashMap<>();
    BipartiteGraph bipartiteGraph = new BipartiteGraph();

    for (Ship ship : mapOracle.myShips) {
      if (excludeShips.contains(ship)) {
        continue;
      }

      Position home = mapOracle.getNearestHome(ship.position);
      if (mapOracle.isTimeToEndGame(ship, mapOracle.myShips.size())
          && mapOracle.haliteGrid.distance(ship.position, home) <= 1) {
        shipDecisions.put(ship, home);
      } else {
        HashSet<Decision> decisions = moveScorer.getDecisions(ship);
        ImmutableMap<Position, Double> decisionScoreMap = decisions
            .stream()
            .collect(ImmutableMap.toImmutableMap(d -> d.destination, d -> d.scoreVector.score()));
        bipartiteGraph.addNode(ship.position, decisionScoreMap);

//        Log.log("SHIP " + ship.id + " " + ship.position);
//        Log.log("Home: " + mapOracle.getNearestHome(ship.position));
//        for (Decision d : decisions) {
//          Log.log(d.toString());
//        }
      }
    }

    long startTime = System.currentTimeMillis();

    HungarianAlgorithm matchingAlg = new HungarianAlgorithm(bipartiteGraph);
    Map<Position, Position> matches = matchingAlg.processMatches();
    for (Map.Entry<Position, Position> entry : matches.entrySet()) {
      shipDecisions.put(mapOracle.myShipPositionsMap.get(entry.getKey()), entry.getValue());
    }

    long endTime = System.currentTimeMillis();
    Log.log("Matching time taken: " + (endTime - startTime));

    return shipDecisions;
  }
}

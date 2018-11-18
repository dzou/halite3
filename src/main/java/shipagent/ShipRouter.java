package shipagent;

import grid.Grid;
import hlt.*;
import map.GravityGrids;
import matching.BipartiteGraph;
import matching.Edge;

import java.util.*;

/**
 * This guy tells ships what to do.
 */
public class ShipRouter {

  private final Position home;

  private final Grid<Integer> haliteGrid;

  public ShipRouter(Grid<Integer> haliteGrid, Position home) {
    this.haliteGrid = haliteGrid;
    this.home = home;
  }

  public HashMap<Ship, Position> routeShips(Collection<Ship> ships) {
    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    for (Ship ship : ships) {
      HashSet<Decision> decisions = getDecisions(ship);
      bipartiteGraph.addShip(ship, decisions);

      if (ship.halite == 1000) {
        Log.log("ship: " + ship.id);
        for (Decision d : decisions) {
          Log.log(d.toString());
        }
      }
    }

    HashSet<Edge> edges = bipartiteGraph.matchShipsToDestinations();

    HashMap<Ship, Position> shipDecisions = new HashMap<>();
    for (Edge e : edges) {
      shipDecisions.put(e.start.ship.get(), e.destination.position);
    }
    return shipDecisions;
  }

  public HashSet<Decision> getDecisions(Ship ship) {
    HashSet<Decision> allDecisions = new HashSet<>();
    allDecisions.add(new Decision(
        Direction.STILL,
        ship.position,
        scorePosition(ship, ship.position)));

    if (ship.halite >= haliteGrid.get(ship.position.x, ship.position.y) / 10) {
      for (Direction offset : Direction.ALL_CARDINALS) {
        Position neighbor = haliteGrid.normalize(ship.position.directionalOffset(offset));
        Decision decision = new Decision(
            offset,
            neighbor,
            scorePosition(ship, neighbor));
        allDecisions.add(decision);
      }
    }

    return allDecisions;
  }

  private double scorePosition(Ship ship, Position destination) {
    int haliteUnderShip = haliteGrid.get(ship.position.x, ship.position.y);
    int haliteCost = haliteUnderShip / 10;

    double score = 0;

    if (ship.position.equals(destination)) {
      int haliteCollected = Math.min(Constants.MAX_HALITE - ship.halite, haliteUnderShip / 4);
      score += haliteCollected;
    } else {
      double bestHaliteRate = -9999999;
      for (int y = 0; y < haliteGrid.height; y++) {
        for (int x = 0; x < haliteGrid.width; x++) {
          double haliteCollectedEstimate = Math.min(
              Constants.MAX_HALITE - ship.halite, haliteGrid.get(x, y) * 0.77);
          int turnsFromDest = haliteGrid.distance(x, y, destination.x, destination.y) + 5;

          double haliteRate = (haliteCollectedEstimate - haliteCost) / turnsFromDest;
          if (haliteRate > bestHaliteRate) {
            bestHaliteRate = haliteRate;
          }
        }
      }

      double moveScore = Math.max(
          bestHaliteRate,    // Move elsewhere and mine
          1.0 * (ship.halite - haliteCost) / (2.0 * haliteGrid.distance(destination, home) + 4)
      );

      score += moveScore;
    }


    return score;
  }
}

package shipagent;

import map.DjikstraGrid;
import map.Grid;
import hlt.*;
import matching.BipartiteGraph;
import matching.Edge;

import java.util.*;

/**
 * This guy tells ships what to do.
 */
public class ShipRouter {

  private final Position home;

  private final Grid<Integer> haliteGrid;

  private final DjikstraGrid djikstraGrid;

  private final int turnsRemaining;

  public ShipRouter(Grid<Integer> haliteGrid, Position home, int turnsRemaining) {
    this.haliteGrid = haliteGrid;
    this.home = home;
    this.djikstraGrid = DjikstraGrid.create(haliteGrid, home);
    this.turnsRemaining = turnsRemaining;
  }

  public HashMap<Ship, Position> routeShips(Collection<Ship> ships) {
    HashSet<Edge> result = new HashSet<>();

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    for (Ship ship : ships) {
      if (isTimeToEndGame(ship, ships.size()) && haliteGrid.distance(ship.position, home) <= 1) {
        result.add(Edge.manualEdge(ship, home));
      } else {
        HashSet<Decision> decisions = getDecisions(ship, ships.size());
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

  public HashSet<Decision> getDecisions(Ship ship, int totalShipCount) {
    HashSet<Decision> allDecisions = new HashSet<>();
    allDecisions.add(new Decision(
        Direction.STILL,
        ship.position,
        scorePosition(ship, ship.position, totalShipCount)));

    if (ship.halite >= haliteGrid.get(ship.position.x, ship.position.y) / 10) {
      for (Direction offset : Direction.ALL_CARDINALS) {
        Position neighbor = haliteGrid.normalize(ship.position.directionalOffset(offset));
        Decision decision = new Decision(
            offset,
            neighbor,
            scorePosition(ship, neighbor, totalShipCount));
        allDecisions.add(decision);
      }
    }

    return allDecisions;
  }

  private double scorePosition(Ship ship, Position destination, int totalShipCount) {
    boolean endTheGame = isTimeToEndGame(ship, totalShipCount);

    double homeScore = goHomeScore(ship, destination, endTheGame);
    double mineScore = endTheGame ? 0 : goMineScore(ship, destination);
    return Math.max(homeScore, mineScore);
  }

  private boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, home) + 5 + (shipCount / 5) >= turnsRemaining;
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    int haliteCostToHome = endTheGame
        ? 0 : djikstraGrid.costCache.get(ship.position.x, ship.position.y) / 10;

    double moveHomeOpportunityCost = 1.0 * ship.halite / Constants.MAX_HALITE;
    return moveHomeOpportunityCost * (ship.halite - haliteCostToHome) / (haliteGrid.distance(destination, home) + 5);
  }

  private double goMineScore(Ship ship, Position destination) {
    if (ship.position.equals(destination)) {
      return Math.min(Constants.MAX_HALITE - ship.halite, haliteGrid.get(ship.position.x, ship.position.y) / 3);
    }


//    if (turnsRemaining < 150) {
//      Log.log("haliteCostTOHome Ship " + ship.position);
//      Log.log("Cost: " + haliteCostToHome);
//    }

    double bestHaliteRate = -9999999;
    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        double haliteCollectedEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, haliteGrid.get(x, y) * 0.58);
        int turnsFromDest = haliteGrid.distance(x, y, destination.x, destination.y) + 3;

        double tollAfterMining = 0.1 * (haliteGrid.get(x, y) - haliteCollectedEstimate);

        double haliteRate = (haliteCollectedEstimate - tollAfterMining) / turnsFromDest;
        if (haliteRate > bestHaliteRate) {
          bestHaliteRate = haliteRate;
        }
      }
    }

    return bestHaliteRate;
  }


}

package shipagent;

import grid.Grid;
import hlt.*;
import map.GravityGrids;

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

  public Map<Ship, Decision> routeShips(Collection<Ship> ships) {
    HashMap<Ship, Decision> shipDecisions = new HashMap<>();
    for (Ship ship : ships) {
      shipDecisions.put(ship, makeDecision(ship));
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

  private Decision makeDecision(Ship ship) {
    HashSet<Decision> allDecisions = getDecisions(ship);

    Log.log("Moves for ship: " + ship.id);
    for (Decision d : allDecisions) {
      Log.log(d.toString());
    }
    return allDecisions.stream().max(Comparator.comparingDouble(d -> d.score)).get();
  }



  private double scorePosition(Ship ship, Position destination) {
    int haliteUnderShip = haliteGrid.get(ship.position.x, ship.position.y);

    double score = 0;

    if (ship.position.equals(destination)) {
      int haliteCollected = Math.min(Constants.MAX_HALITE - ship.halite, haliteUnderShip / 4);
      score += haliteCollected;
    } else {
      double bestHaliteRate = -9999999;
      for (int y = 0; y < haliteGrid.height; y++) {
        for (int x = 0; x < haliteGrid.width; x++) {
          double haliteCollectedEstimate = Math.min(
              Constants.MAX_HALITE - ship.halite, haliteGrid.get(x, y) * 0.58);
          int turnsFromDest = haliteGrid.distance(x, y, destination.x, destination.y) + 3;

          double haliteRate = haliteCollectedEstimate / turnsFromDest;
          if (haliteRate > bestHaliteRate) {
            bestHaliteRate = haliteRate;
          }
        }
      }

      double moveScore = Math.max(
          bestHaliteRate,    // Move elsewhere and mine
          1.0 * ship.halite / (haliteGrid.distance(destination, home) + 3)
      );
      score += moveScore - haliteUnderShip / 10;
    }


    return score;
  }
}

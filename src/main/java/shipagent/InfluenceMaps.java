package shipagent;

import hlt.Constants;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;

import java.util.*;

public class InfluenceMaps {

  public static Grid<Double> buildShipInfluenceMap(Collection<Ship> myShips, Grid<Integer> haliteGrid) {
    Grid<Double> influenceMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);

    for (Ship ship : myShips) {
      for (int y = -5; y <= 5; y++) {
        for (int x = -5 + Math.abs(y); x <= 5 - Math.abs(y); x++) {
          int dx = ship.position.x + x;
          int dy = ship.position.y + y;

          double miningPotential =
              Math.pow(0.5, haliteGrid.distance(dx, dy, ship.position.x, ship.position.y))
                  * (Constants.MAX_HALITE - ship.halite) / Constants.MAX_HALITE;

          double prev = influenceMap.get(dx, dy);
          influenceMap.set(dx, dy, prev + miningPotential);
        }
      }
    }

    return influenceMap;
  }

  public static Grid<Integer> threatMap(Collection<Ship> enemyShips, Grid<Integer> haliteGrid) {
    Grid<Integer> enemyDangerMap = new Grid<>(haliteGrid.width, haliteGrid.height, -1);

    for (Ship enemyShip : enemyShips) {
      for (int y = 0; y < haliteGrid.height; y++) {
        for (int x = 0; x < haliteGrid.width; x++) {

          HashSet<Position> influencedPositions = new HashSet<>();
          influencedPositions.add(enemyShip.position);
          for (Position neighbor : haliteGrid.getNeighbors(enemyShip.position)) {
            influencedPositions.add(neighbor);
          }

          for (Position pos : influencedPositions) {
            int prev = enemyDangerMap.get(pos.x, pos.y);
            if (prev == -1 || enemyShip.halite < prev) {
              enemyDangerMap.set(pos.x, pos.y, enemyShip.halite);
            }
          }
        }
      }
    }

    return enemyDangerMap;
  }

  public static Grid<Integer> inspiredMap(Collection<Ship> enemyShips, Grid<Integer> haliteGrid) {
    Grid<Integer> inspiredMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0);

    for (Ship enemyShip : enemyShips) {
      for (int y = -4; y <= 4; y++) {
        for (int x = -4 + Math.abs(y); x <= 4 - Math.abs(y); x++) {
          int dx = enemyShip.position.x + x;
          int dy = enemyShip.position.y + y;

          int prev = inspiredMap.get(dx, dy);
          inspiredMap.set(dx, dy, prev + 1);
        }
      }
    }

    return inspiredMap;
  }

  static Grid<Integer> retreatMap(Collection<Ship> ships, Map<PlayerId, Set<Position>> playerDropOffs, Grid<Integer> haliteGrid) {
    BaseManager baseManager = new BaseManager(playerDropOffs, haliteGrid);

    Grid<Integer> retreatMap = new Grid<>(haliteGrid.width, haliteGrid.height, -1);

    for (Ship ship : ships) {
      ArrayList<Position> goHomePositions = baseManager.findGoHomeDirections(ship);

      for (Position position : goHomePositions) {
        int prev = retreatMap.get(position.x, position.y);
        retreatMap.set(position.x, position.y, prev == -1 ? ship.halite : Math.min(prev, ship.halite));
      }
    }

    return retreatMap;
  }

  static Grid<Integer> killMap(Collection<Ship> myShips, Collection<Ship> enemyShips, Grid<Integer> haliteGrid) {
    Grid<Integer> killMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0);

    Grid<Integer> myThreatMap = threatMap(myShips, haliteGrid);

    for (Ship enemy : enemyShips) {
      boolean shouldBeKilled = false;
      int bestDifference = 0;

      HashSet<Position> neighbors = haliteGrid.getNeighbors(enemy.position);

      for (Position neighbor : neighbors) {
        int myThreat = myThreatMap.get(neighbor.x, neighbor.y);
        if (myThreat == -1) {
          shouldBeKilled = false;
          break;
        } else if (myThreat < enemy.halite) {
          shouldBeKilled = true;
          int diff = enemy.halite - myThreat;
          if (diff > bestDifference) {
            bestDifference = diff;
          }
        }
      }

      if (shouldBeKilled) {
        neighbors.add(enemy.position);
        for (Position neighbor : neighbors) {
          int prev = killMap.get(neighbor.x, neighbor.y);
          killMap.set(neighbor.x, neighbor.y, Math.max(prev, bestDifference));
        }
      }
    }

    return killMap;
  }
}

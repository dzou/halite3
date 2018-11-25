package shipagent;

import hlt.Constants;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;

import java.util.*;

public class InfluenceMaps {

  public static final int SHIP_INFLUENCE_RANGE = 10;

  public static double getCrowdFactor(Ship ship, int dx, int dy, Grid<Integer> haliteGrid) {
    double distance = 2 * haliteGrid.distance(dx, dy, ship.position.x, ship.position.y) + 1;
    double miningPotential = (1.0 * Constants.MAX_HALITE - ship.halite) / (distance * Constants.MAX_HALITE);
    return miningPotential;
  }

  public static Grid<Double> buildShipInfluenceMap(Collection<Ship> myShips, Grid<Integer> haliteGrid) {
    Grid<Double> influenceMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);

    int influenceRange = Math.min(haliteGrid.width / 2, SHIP_INFLUENCE_RANGE);

    for (Ship ship : myShips) {
      for (int y = -influenceRange; y <= influenceRange; y++) {
        for (int x = -influenceRange + Math.abs(y); x <= influenceRange - Math.abs(y); x++) {
          int dx = ship.position.x + x;
          int dy = ship.position.y + y;

          double miningPotential = getCrowdFactor(ship, dx, dy, haliteGrid);
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
      for (int y = -Constants.INSPIRATION_RADIUS; y <= Constants.INSPIRATION_RADIUS; y++) {
        for (int x = -Constants.INSPIRATION_RADIUS + Math.abs(y); x <= Constants.INSPIRATION_RADIUS - Math.abs(y); x++) {
          int dx = enemyShip.position.x + x;
          int dy = enemyShip.position.y + y;

          int prev = inspiredMap.get(dx, dy);
          inspiredMap.set(dx, dy, prev + 1);
        }
      }
    }

    return inspiredMap;
  }

//  static Grid<Integer> retreatMap(
//      Collection<Ship> ships, Map<PlayerId, Set<Position>> playerDropOffs, Grid<Integer> haliteGrid) {
//
//    BaseManager baseManager = new BaseManager(playerDropOffs, haliteGrid);
//
//    Grid<Integer> retreatMap = new Grid<>(haliteGrid.width, haliteGrid.height, -1);
//
//    for (Ship ship : ships) {
//      ArrayList<Position> goHomePositions = baseManager.findGoHomeDirections(ship);
//
//      for (Position position : goHomePositions) {
//        int prev = retreatMap.get(position.x, position.y);
//        retreatMap.set(position.x, position.y, prev == -1 ? ship.halite : Math.min(prev, ship.halite));
//      }
//    }
//
//    return retreatMap;
//  }

  static Grid<Integer> killMap(
      Collection<Ship> myShips,
      Collection<Ship> enemyShips,
      Map<PlayerId, Set<Position>> playerDropOffs,
      Grid<Integer> haliteGrid) {

    Grid<Integer> killMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0);

    BaseManager baseManager = new BaseManager(playerDropOffs, haliteGrid);
    Grid<Integer> myThreatMap = threatMap(myShips, haliteGrid);

    for (Ship enemy : enemyShips) {
      int bestDifference = 0;

      Set<Position> enemyNeighbors = haliteGrid.getNeighbors(enemy.position);
      List<Position> positionsToCover = baseManager.findGoHomeDirections(enemy);
      HashSet<Position> coveredPositions = new HashSet<>();

      for (Position neighbor : enemyNeighbors) {
        if (myThreatMap.get(neighbor.x, neighbor.y) != -1 && myThreatMap.get(neighbor.x, neighbor.y) <= enemy.halite) {
          int diff = enemy.halite - myThreatMap.get(neighbor.x, neighbor.y);
          if (diff > bestDifference) {
            bestDifference = diff;
          }
          coveredPositions.add(neighbor);
        }
      }

      if (coveredPositions.size() >= 3 && coveredPositions.containsAll(positionsToCover)) {
        for (Position neighbor : coveredPositions) {
          int prev = killMap.get(neighbor.x, neighbor.y);
          killMap.set(neighbor.x, neighbor.y, Math.max(prev, bestDifference));
        }
      }
    }

    return killMap;
  }
}

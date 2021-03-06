package shipagent;

import hlt.Constants;
import hlt.Position;
import hlt.Ship;
import map.Grid;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;

public class InfluenceMaps {

  public static final int HALITE_DENSITY_RANGE = 4;

  public static final int SHIP_INFLUENCE_RANGE = 4;

  public static Grid<Integer> getControlMap(
      Grid<Integer> haliteGrid,
      Collection<Position> alliedPositions,
      Collection<Position> enemyPositions) {

    Grid<Integer> controlMap = new Grid<>(
        haliteGrid.width, haliteGrid.height, 0);

    ArrayDeque<Position> queue = new ArrayDeque<>();
    for (Position myPoint : alliedPositions) {
      queue.addLast(myPoint);
      controlMap.set(myPoint.x, myPoint.y, 1);
    }

    for (Position enemyPoint : enemyPositions) {
      queue.addLast(enemyPoint);
      controlMap.set(enemyPoint.x, enemyPoint.y, -1);
    }

//    for (Ship myShip : myShips) {
//      queue.addLast(myShip.position);
//      controlMap.set(myShip.position.x, myShip.position.y, 1);
//    }
//
//    for (Ship enemyShip : enemyShips) {
//      queue.addLast(enemyShip.position);
//      controlMap.set(enemyShip.position.x, enemyShip.position.y, -1);
//    }

    while (!queue.isEmpty()) {
      Position curr = queue.removeFirst();
      int rootShipType = controlMap.get(curr.x, curr.y);

      for (Position neighbor : haliteGrid.getNeighbors(curr)) {
        int neighborShipType = controlMap.get(neighbor.x, neighbor.y);
        if (neighborShipType == 0) {
          controlMap.set(neighbor.x, neighbor.y, rootShipType);
          queue.addLast(neighbor);
        }
      }
    }

    return controlMap;
  }

  public static double getInfluenceFactor(Ship ship, int dx, int dy, Grid<Integer> haliteGrid) {
    int distance = haliteGrid.distance(dx, dy, ship.position.x, ship.position.y);
    if (distance > SHIP_INFLUENCE_RANGE) {
      return 0;
    } else {
      double decayFactor = 1; // Math.max(1, distance);
      double miningPotential = (1.0 * Constants.MAX_HALITE - ship.halite) / (decayFactor * Constants.MAX_HALITE);
      return miningPotential;
    }
  }

  public static Grid<Double> buildShipInfluenceMap(Collection<Ship> myShips, Grid<Integer> haliteGrid) {
    Grid<Double> influenceMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);

    int influenceRange = Math.min(haliteGrid.width / 2, SHIP_INFLUENCE_RANGE);

    for (Ship ship : myShips) {
      for (int y = -influenceRange; y <= influenceRange; y++) {
        for (int x = -influenceRange + Math.abs(y); x <= influenceRange - Math.abs(y); x++) {
          int dx = ship.position.x + x;
          int dy = ship.position.y + y;

          double miningPotential = getInfluenceFactor(ship, dx, dy, haliteGrid);
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

//  static Grid<Integer> killMap(
//      Collection<Ship> enemyShips,
//      Map<PlayerId, Set<Position>> playerDropOffs,
//      Grid<Integer> haliteGrid,
//      Grid<Integer> myThreatMap,
//      Grid<Double> myInfluenceMap,
//      Grid<Double> enemyInfluenceMap) {
//
//    Grid<Integer> killMap = new Grid<>(haliteGrid.width, haliteGrid.height, 0);
//    BaseManager baseManager = new BaseManager(playerDropOffs, haliteGrid);
//
//    for (Ship enemy : enemyShips) {
//      double enemyInfluenceAtPoint =
//          enemyInfluenceMap.get(enemy.position.x, enemy.position.y)
//              - getCrowdFactor(enemy, enemy.position.x, enemy.position.y, haliteGrid);
//      double myInfluenceAtPoint = myInfluenceMap.get(enemy.position.x, enemy.position.y);
//      if (enemyInfluenceAtPoint > myInfluenceAtPoint) {
//        continue;
//      }
//
//      List<Position> positionsToCover = baseManager.findGoHomeDirections(enemy);
//      int bestDifference = 0;
//      int covered = 0;
//
//      for (Position neighbor : positionsToCover) {
//        if (myThreatMap.get(neighbor.x, neighbor.y) != -1
//            && myThreatMap.get(neighbor.x, neighbor.y) <= enemy.halite * 0.5) {
//          int diff = enemy.halite - myThreatMap.get(neighbor.x, neighbor.y);
//          if (diff > bestDifference) {
//            bestDifference = diff;
//          }
//          covered += 1;
//        }
//      }
//
//      for (Position neighbor : positionsToCover) {
//        int prev = killMap.get(neighbor.x, neighbor.y);
//        killMap.set(neighbor.x, neighbor.y, Math.max(prev, bestDifference));
//      }
//      if (covered >= positionsToCover.size()) {
//        killMap.set(enemy.position.x, enemy.position.y, bestDifference / 2);
//      }
//    }
//
//    return killMap;
//  }

  public static Grid<Double> shipHaliteDensityMap(Grid<Integer> haliteGrid, Collection<Ship> myShips) {
    Grid<Double> densityGrid = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);
    for (Ship ship : myShips) {
      for (int dy = -HALITE_DENSITY_RANGE; dy <= HALITE_DENSITY_RANGE; dy++) {
        for (int dx = -HALITE_DENSITY_RANGE + Math.abs(dy); dx <= HALITE_DENSITY_RANGE - Math.abs(dy); dx++) {
          int neighborX = ship.position.x + dx;
          int neighborY = ship.position.y + dy;

          double shipContribution =
              ship.halite / (haliteGrid.distance(ship.position.x, ship.position.y, neighborX, neighborY) + 1);
          double prev = densityGrid.get(neighborX, neighborY);

          densityGrid.set(neighborX, neighborY, prev + shipContribution);
        }
      }
    }

    return densityGrid;
  }

  public static Grid<Double> haliteDensityMap(Grid<Integer> haliteGrid) {
    Grid<Double> densityGrid = new Grid<>(haliteGrid.width, haliteGrid.height, 0.0);
    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        densityGrid.set(x, y, getHaliteDensity(x, y, haliteGrid));
      }
    }

    return densityGrid;
  }

  private static double getHaliteDensity(int x, int y, Grid<Integer> haliteGrid) {
    int densitySum = 0;

    for (int dy = -HALITE_DENSITY_RANGE; dy <= HALITE_DENSITY_RANGE; dy++) {
      for (int dx = -HALITE_DENSITY_RANGE + Math.abs(dy); dx <= HALITE_DENSITY_RANGE - Math.abs(dy); dx++) {
        int neighborX = x + dx;
        int neighborY = y + dy;
        densitySum += 1.0 * haliteGrid.get(neighborX, neighborY) / (haliteGrid.distance(x, y, neighborX, neighborY) + 1);
      }
    }

    return densitySum;
  }
}

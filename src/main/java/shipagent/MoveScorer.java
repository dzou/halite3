package shipagent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.*;
import map.DjikstraGrid;
import map.Grid;

import java.util.*;

public class MoveScorer {

  private static final int LOCAL_DISTANCE = 4;

  private final MapOracle mapOracle;

  public MoveScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
  }

  public HashSet<Decision> getDecisions(Ship ship) {
    HashSet<Decision> allDecisions = new HashSet<>();
    allDecisions.add(new Decision(
        Direction.STILL,
        ship.position,
        scorePosition(ship, ship.position)));

    if (ship.halite >= mapOracle.haliteGrid.get(ship.position.x, ship.position.y) / 10) {
      for (Direction offset : Direction.ALL_CARDINALS) {
        Position neighbor = mapOracle.haliteGrid.normalize(ship.position.directionalOffset(offset));
        Decision decision = new Decision(
            offset,
            neighbor,
            scorePosition(ship, neighbor));
        allDecisions.add(decision);
      }
    }

    return allDecisions;
  }

  DecisionVector scorePosition(Ship ship, Position destination) {
    boolean endTheGame = mapOracle.isTimeToEndGame(ship, mapOracle.myShips.size());

    double homeScore = goHomeScore(ship, destination, endTheGame);
    double localMoveScore = localMoveScore(ship, destination);
    double explorePotentialScore = explorePotentialScore(ship, destination);
    double enemyInfluence = getEnemyInfluence(ship, destination);
    double killScore = killScore(ship, destination);
    double shipCrowdScore = crowdScore(ship, destination);

    if (!endTheGame) {
      return new DecisionVector(
          homeScore,
          localMoveScore,
          explorePotentialScore,
          enemyInfluence,
          killScore,
          shipCrowdScore);
    } else {
      return new DecisionVector(
          homeScore,
          0,
          0,
          enemyInfluence,
          killScore,
          0);
    }
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    double haliteCostToHome = endTheGame ? 0 : mapOracle.goHomeCost(destination);

    double moveHomeOpportunityCost = 1.0 * (ship.halite * ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE * Constants.MAX_HALITE);
    return moveHomeOpportunityCost * (ship.halite - haliteCostToHome) / (mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) + 5);
  }

  private double crowdScore(Ship ship, Position destination) {
    double shipInfluence = mapOracle.shipInfluenceMap.get(destination.x, destination.y)
        - InfluenceMaps.getCrowdFactor(ship, destination.x, destination.y, mapOracle.haliteGrid);
    double crowdMultiplier = 1.0 - (1.0 * ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE);
    return crowdMultiplier * shipInfluence;
  }

//  private double crowdScore(Ship currentShip, Position destination) {
//
//    Direction d = haliteGrid.calculateDirection(currentShip.position, destination);
//
//    double count = 0;
//
//    for (Ship ship : myShips) {
//      int xDelta = DjikstraGrid.getAxisDirection(currentShip.position.x, ship.position.x, haliteGrid.width);
//      int yDelta = DjikstraGrid.getAxisDirection(currentShip.position.y, ship.position.y, haliteGrid.height);
//
//      if (haliteGrid.distance(currentShip.position, ship.position) <= SHIP_CROWDING_RANGE
//          && isRightDelta(xDelta, yDelta, d)
//          && !ship.equals(currentShip)) {
//        count += 1.0 * ship.halite / Constants.MAX_HALITE;
//      }
//    }
//
//    return count;
//  }
//
//  private static boolean isRightDelta(int xDelta, int yDelta, Direction direction) {
//    if (direction == Direction.EAST && xDelta > 0
//        || direction == Direction.WEST && xDelta < 0
//        || direction == Direction.NORTH && yDelta < 0
//        || direction == Direction.SOUTH && yDelta > 0
//        || direction == Direction.STILL && xDelta == 0 && yDelta == 0) {
//      return true;
//    } else {
//      return false;
//    }
//  }

  private double localMoveScore(Ship ship, Position destination) {
    Direction d = mapOracle.haliteGrid.calculateDirection(ship.position, destination);
    if (d == Direction.STILL) {
      double haliteMinedPotential = mapOracle.haliteGrid.get(ship.position.x, ship.position.y) * 0.25;
      double actualHaliteMined = Math.min(
          Constants.MAX_HALITE - ship.halite,
          mapOracle.inspireMap.get(ship.position.x, ship.position.y) > 1 ? haliteMinedPotential * 3 : haliteMinedPotential);
      return actualHaliteMined;
    }

    int xStart = (d == Direction.EAST) ? 1 : -LOCAL_DISTANCE;
    int xEnd = (d == Direction.WEST) ? -1 : LOCAL_DISTANCE;

    int yStart = (d == Direction.SOUTH) ? 1 : -LOCAL_DISTANCE;
    int yEnd = (d == Direction.NORTH) ? -1 : LOCAL_DISTANCE;

    Position projectionDestination = Position.at(0, 0).directionalOffset(d);
    Grid<Integer> subGrid = mapOracle.haliteGrid.subGrid(ship.position, LOCAL_DISTANCE);
    DjikstraGrid subGridCosts = DjikstraGrid.create(subGrid, projectionDestination);

    double bestHaliteRate = -999999;
    for (int y = yStart; y <= yEnd; y++) {
      for (int x = Math.max(xStart, -LOCAL_DISTANCE + Math.abs(y));
           x <= Math.min(xEnd, LOCAL_DISTANCE - Math.abs(y));
           x++) {

        int dx = ship.position.x + x;
        int dy = ship.position.y + y;

        int haliteOnTile = mapOracle.haliteGrid.get(dx, dy);
        Ship shipOnTile = mapOracle.myShipPositionsMap.get(Position.at(dx, dy));
        if (shipOnTile != null) {
          int haliteCapacity = Constants.MAX_HALITE - shipOnTile.halite;
          haliteOnTile = Math.max(0, haliteOnTile - haliteCapacity);
        }

        double haliteMined = Math.min(Constants.MAX_HALITE - ship.halite, haliteOnTile * 0.44);
        int turnsFromDest = mapOracle.haliteGrid.distance(dx, dy, destination.x, destination.y) + 2 + 1;

        double haliteReward = Math.min(
            Constants.MAX_HALITE - ship.halite,
            mapOracle.inspireMap.get(dx, dy) > 1 ? haliteMined * 2.0 : haliteMined);

        double tollAfterMining = Math.max(0, mapOracle.goHomeCost(destination) - haliteMined * 0.10);
        double tollToTile = (subGridCosts.costCache.get(x, y)
            - subGrid.get(x, y)
            + subGrid.get(projectionDestination.x, projectionDestination.y)
            /* + mapOracle.haliteGrid.get(ship.position.x, ship.position.y) */) * 0.10;

        // int tollToLeaveTile = (x != 0 || y != 0) ? mapOracle.haliteGrid.get(ship.position.x, ship.position.y) / 40 : 0;

        double haliteRate = (haliteReward - tollAfterMining - tollToTile) / turnsFromDest;
        if (haliteRate > bestHaliteRate) {
          bestHaliteRate = haliteRate;
        }
      }
    }

    return bestHaliteRate;
  }

  private double explorePotentialScore(Ship ship, Position destination) {
    Direction d = mapOracle.haliteGrid.calculateDirection(ship.position, destination);
    if (d == Direction.STILL) {
      return 0.0;
    }

    int xStart = ship.position.x - (mapOracle.haliteGrid.width / 2);
    int xEnd = ship.position.x + (mapOracle.haliteGrid.height / 2);

    int yStart = ship.position.y - (mapOracle.haliteGrid.height / 2);
    int yEnd = ship.position.y + (mapOracle.haliteGrid.height / 2);

    if (d == Direction.EAST) {
      xStart = ship.position.x + 1;
    } else if (d == Direction.WEST) {
      xEnd = ship.position.x - 1;
    } else if (d == Direction.SOUTH) {
      yStart = ship.position.y + 1;
    } else if (d == Direction.NORTH) {
      yEnd = ship.position.y - 1;
    }

    PriorityQueue<Double> topNRates = new PriorityQueue<>();
    for (int y = yStart; y <= yEnd; y++) {
      for (int x = xStart; x <= xEnd; x++) {
        double inspireMultiplier = mapOracle.inspireMap.get(x, y) > 1 ? 2.5 : 1.0;

        double crowdFactor = mapOracle.shipInfluenceMap.get(x, y)
            - InfluenceMaps.getCrowdFactor(ship, x, y, mapOracle.haliteGrid);
        double crowdMultiplier = Math.max(0, 1.0 - crowdFactor);
        double adjustedHalite = mapOracle.haliteGrid.get(x, y) * inspireMultiplier * crowdMultiplier;

        double haliteCollectedEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, mapOracle.haliteGrid.get(x, y) * 0.58);
        double haliteRewardEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, adjustedHalite * 0.58);

        int turnsFromDest = mapOracle.haliteGrid.distance(x, y, destination.x, destination.y) + 3;

        // double moveToll = turnsFromDest <= 4 ? 0 : mapOracle.haliteGrid.get(destination.x, destination.y) / 10;

        double tollAfterMining = Math.min(
            mapOracle.haliteGrid.get(x, y),
            mapOracle.goHomeCost(Position.at(x, y)) - haliteCollectedEstimate * 0.10);

        double haliteRate = (haliteRewardEstimate - tollAfterMining) / turnsFromDest;

        topNRates.add(haliteRate);
        if (topNRates.size() > 15) {
          topNRates.poll();
        }
      }
    }

    double haliteRateSum = topNRates.stream().mapToDouble(j -> j).sum();
    int haliteRateCount = Math.max(1, topNRates.size());

    return haliteRateSum / haliteRateCount;
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    int enemyMinHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);
    int diff = enemyMinHalite - ship.halite;
    if (enemyMinHalite > -1 && diff <= 0 && mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) > 2) {
      return diff;
    } else {
      return 0;
    }
  }

  private double killScore(Ship ship, Position destination) {
    int killPotentialGain = mapOracle.killMap.get(destination.x, destination.y);
    return killPotentialGain;
  }
}


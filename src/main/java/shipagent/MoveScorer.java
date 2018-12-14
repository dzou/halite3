package shipagent;

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

    if (!endTheGame) {
      return new DecisionVector(
          homeScore,
          localMoveScore,
          explorePotentialScore,
          enemyInfluence,
          killScore);
    } else {
      return new DecisionVector(
          homeScore,
          0,
          0,
          enemyInfluence,
          killScore);
    }
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    double haliteCostToHome = endTheGame ? 0 : mapOracle.goHomeCost(destination);

    double payload = ship.halite;
    if (destination.equals(ship.position)) {
      payload += mineScore(ship);
    }

    double moveHomeOpportunityCost = 1.0 * (ship.halite * ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE * Constants.MAX_HALITE);
    return moveHomeOpportunityCost * (payload - haliteCostToHome)
        / (mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) + 4);
  }

  private double mineScore(Ship ship) {
    double haliteMinedPotential = mapOracle.haliteGrid.get(ship.position.x, ship.position.y) * 0.25;
    double actualHaliteMined = Math.min(
        Constants.MAX_HALITE - ship.halite,
        mapOracle.inspireMap.get(ship.position.x, ship.position.y) > 1 ? haliteMinedPotential * 3 : haliteMinedPotential);
    return actualHaliteMined;
  }

  private double localMoveScore(Ship ship, Position destination) {
    Direction d = mapOracle.haliteGrid.calculateDirection(ship.position, destination);

    if (d == Direction.STILL) {
      return mineScore(ship);
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
        if (shipOnTile != null && Math.abs(dx) + Math.abs(dy) > 1) {
          int haliteCapacity = Constants.MAX_HALITE - shipOnTile.halite;
          haliteOnTile = Math.max(haliteOnTile / 2, haliteOnTile - haliteCapacity);
        }

        double haliteMined = Math.min(Constants.MAX_HALITE - ship.halite, haliteOnTile * 0.44);
        int turnsFromDest = mapOracle.haliteGrid.distance(dx, dy, destination.x, destination.y) + 2 + 1;

        double haliteReward = Math.min(
            Constants.MAX_HALITE - ship.halite,
            mapOracle.inspireMap.get(dx, dy) > 1 ? haliteMined * 2.0 : haliteMined);

        // double tollAfterMining = Math.max(0, mapOracle.goHomeCost(Position.at(dx, dy)) - haliteMined * 0.10);
        double tollToTile = (subGridCosts.costCache.get(x, y)
            - haliteMined
            + subGrid.get(projectionDestination.x, projectionDestination.y)
            + ((d == Direction.STILL) ? 0 : subGrid.get(0, 0))) * 0.10;


        double haliteRate = (haliteReward - tollToTile) / turnsFromDest;
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
    int xEnd = ship.position.x + (mapOracle.haliteGrid.width / 2);

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
        if (mapOracle.haliteGrid.distance(x, y, ship.position.x, ship.position.y) <= LOCAL_DISTANCE) {
          continue;
        }

//        Position exploreGoal = Position.at(x, y);
//        Position nearestHome = mapOracle.getNearestHome(exploreGoal);

        double haliteCollectedEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, mapOracle.haliteGrid.get(x, y));

        // make sure this is not infinity
        int turnsFromDest = mapOracle.haliteGrid.distance(x, y, destination.x, destination.y) + 5;

//        double tollAfterMining = Math.min(
//            haliteCollectedEstimate,
//            mapOracle.goHomeCost(Position.at(x, y)) - haliteCollectedEstimate * 0.10);

        double inspireMultiplier = mapOracle.inspireMap.get(x, y) > 1 ? 1.5 : 1.0;

        double crowdFactor = // mapOracle.influenceSumAtPoint(x, y)
            mapOracle.myInfluenceMap.get(x, y)
            - InfluenceMaps.getInfluenceFactor(ship, x, y, mapOracle.haliteGrid);
        double crowdMultiplier = 1.0 - Math.min(0.75, crowdFactor * 0.25);

//        double crowdFactor = mapOracle.exploreGrid.get(x, y)
//            - InfluenceMaps.getExploreFactor(ship, x, y, mapOracle.haliteGrid);
//        double crowdMultiplier = Math.max(0, 1.0 - crowdFactor);

        double haliteRate = crowdMultiplier * inspireMultiplier * haliteCollectedEstimate / turnsFromDest;

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

//  private double getEnemyInfluence(Ship ship, Position destination) {
//    double inf;
//    if (mapOracle.enemyShipPositionsMap.containsKey(destination)) {
//      inf = mapOracle.influenceDifferenceAtPoints(ship.position.x, ship.position.y, destination.x, destination.y);
//    } else {
//      inf = mapOracle.influenceDifferenceAtPoint(destination.x, destination.y);
//    }
//
//
//    if (inf < 0) {
//      return -1.0 * ship.halite;
//    } else {
//      return 0;
//    }

//    int enemyMinHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);
//    int diff = enemyMinHalite - ship.halite;
//    if (enemyMinHalite > -1 && diff <= 0 && mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) > 1) {
//      return diff;
//    } else {
//      return 0;
//    }
//  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    int enemyMinHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);

    if (enemyMinHalite == -1
        || mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) > 0 && ship.halite < enemyMinHalite
        || mapOracle.haliteGrid.distance(mapOracle.getNearestHome(destination), ship.position) <= 1) {
      return 0;
    } else {
      return -ship.halite;
    }
  }

  private double killScore(Ship ship, Position destination) {
//    int killPotentialGain = mapOracle.killMap.get(destination.x, destination.y);
//    return killPotentialGain;
    return 0;
  }
}


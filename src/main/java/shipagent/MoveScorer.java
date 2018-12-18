package shipagent;

import hlt.*;
import map.DjikstraGrid;
import map.Grid;
import map.Zone;

import java.util.*;

public class MoveScorer {

  private static final int LOCAL_DISTANCE = 5;

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

    double moveHomeOpportunityCost = 1.0 * (ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE);
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

        Position localDest = Position.at(dx, dy);
        if (mapOracle.influenceDifferenceAtPoint(dx, dy) < 0
            && mapOracle.enemyShipCovers.isPositionCovered(localDest)) {
          continue;
        }

        int haliteOnTile = mapOracle.haliteGrid.get(dx, dy);

        Ship shipOnTile = mapOracle.myShipPositionsMap.get(localDest);
        if (shipOnTile != null && Math.abs(dx) + Math.abs(dy) > 1) {
          int haliteCapacity = Constants.MAX_HALITE - shipOnTile.halite;
          haliteOnTile = Math.max(haliteOnTile / 2, haliteOnTile - haliteCapacity);
        }

        double haliteMined = Math.min(Constants.MAX_HALITE - ship.halite, haliteOnTile * 0.44);
        int turnsFromDest = mapOracle.haliteGrid.distance(dx, dy, destination.x, destination.y) + 2 + 1;

        double haliteReward = Math.min(
            Constants.MAX_HALITE - ship.halite,
            mapOracle.inspireMap.get(dx, dy) > 1 ? haliteMined * 2.2 : haliteMined);

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

  private double scoreZone(Ship ship, Position destination, Zone zone) {
    double inspireMultiplier = mapOracle.inspireMap.get(zone.center.x, zone.center.y) > 1 ? 1.5 : 1.0;

    double crowdFactor = mapOracle.myInfluenceMap.get(zone.center.x, zone.center.y)
        - InfluenceMaps.getInfluenceFactor(ship, zone.center.x, zone.center.y, mapOracle.haliteGrid);
    double crowdMultiplier = 1.0 - Math.min(0.5, crowdFactor * crowdFactor * 0.25);

    double haliteCollectedEstimate = Math.min(
        Constants.MAX_HALITE - ship.halite,
        inspireMultiplier * crowdMultiplier * zone.getMinedAmount());

    Position nearestHome = mapOracle.getNearestHome(zone.center);
    int turnsFromDest = mapOracle.haliteGrid.distance(destination, zone.center)
        + mapOracle.haliteGrid.distance(zone.center, nearestHome)
        + 9;

    return inspireMultiplier * crowdMultiplier * haliteCollectedEstimate / turnsFromDest;
  }

  private double explorePotentialScore(Ship ship, Position destination) {
    Direction d = mapOracle.haliteGrid.calculateDirection(ship.position, destination);
    if (d == Direction.STILL) {
      return 0.0;
    }

    double averageRate =
        mapOracle.zoneGrid.zonesInDirection(ship.position, d)
            .stream()
            .map(z -> scoreZone(ship, destination, z))
            .sorted(Comparator.reverseOrder())
            .limit(3)
            .mapToDouble(i -> i)
            .average()
            .orElse(0);

//            .stream()
//            .mapToDouble(z -> scoreZone(ship, destination, z))
//            .max()
//            .orElse(0);

    return averageRate;
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    int enemyMinHalite = mapOracle.enemyThreatMap.get(destination.x, destination.y);
    double influenceDifference = mapOracle.influenceDifferenceAtPoint(destination.x, destination.y);

    Position nearestHome = mapOracle.getNearestHome(destination);
    if (mapOracle.haliteGrid.distance(nearestHome, destination) <= 1) {
      return Math.max(0.0, enemyMinHalite);
    }

    if (influenceDifference > 0.0) {
      if (enemyMinHalite != -1) {
        return Math.min(0, enemyMinHalite - 1.3 * ship.halite);
      } else {
        return 0;
      }
    } else {
      if (enemyMinHalite != -1) {
        double multiplier = 0.75 + (destination.equals(ship.position) ? 0.25 : 0.0);
        return multiplier * -ship.halite;
      } else if (mapOracle.enemyShipCovers.isPositionCovered(destination)) {
        return (0.5 - influenceDifference * 0.01) * -ship.halite;
      } else {
        return 0;
      }
    }
  }

  private double killScore(Ship ship, Position destination) {
//    int killPotentialGain = mapOracle.killMap.get(destination.x, destination.y);
//    return killPotentialGain;
    return 0;
  }
}


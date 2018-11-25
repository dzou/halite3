package shipagent;

import com.google.common.collect.ImmutableSet;
import hlt.*;
import map.DjikstraGrid;
import map.Grid;

import java.util.*;

public class MoveScorer {

  private static final int LOCAL_DISTANCE = 4;

  private static final int SHIP_CROWDING_RANGE = 8;

  private final Grid<Integer> haliteGrid;
  private final Position home;
  private final int turnsRemaining;
  private final ImmutableSet<Ship> myShips;

  final DjikstraGrid djikstraGrid;

  final Grid<Double> shipInfluenceMap;
  final Grid<Integer> enemyThreatMap;
  final Grid<Integer> killMap;
  final Grid<Integer> inspireMap;


  public MoveScorer(
      Grid<Integer> haliteGrid,
      Position home,
      int turnsRemaining,
      Collection<Ship> myShips,
      Collection<Ship> enemyShips,
      Map<PlayerId, Set<Position>> playerDropOffs) {

    this.haliteGrid = haliteGrid;
    this.home = home;
    this.turnsRemaining = turnsRemaining;
    this.myShips = ImmutableSet.copyOf(myShips);

    this.djikstraGrid = DjikstraGrid.create(haliteGrid, home);

    HashSet<Ship> allShips = new HashSet<>();
    allShips.addAll(myShips);
    allShips.addAll(enemyShips);

    this.shipInfluenceMap = InfluenceMaps.buildShipInfluenceMap(allShips, haliteGrid);
    this.enemyThreatMap = InfluenceMaps.threatMap(enemyShips, haliteGrid);
    this.killMap = InfluenceMaps.killMap(myShips, enemyShips, playerDropOffs, haliteGrid);
    this.inspireMap = InfluenceMaps.inspiredMap(enemyShips, haliteGrid);
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

  DecisionVector scorePosition(Ship ship, Position destination) {
    boolean endTheGame = isTimeToEndGame(ship, myShips.size());

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
    double haliteCostToHome = endTheGame
        ? 0 : djikstraGrid.costCache.get(destination.x, destination.y) * 0.10;

    double moveHomeOpportunityCost = 1.0 * (ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE);
    return moveHomeOpportunityCost * (ship.halite - haliteCostToHome) / (haliteGrid.distance(destination, home) + 5);
  }

  private double crowdScore(Ship ship, Position destination) {
    double shipInfluence = shipInfluenceMap.get(destination.x, destination.y)
        - InfluenceMaps.getCrowdFactor(ship, destination.x, destination.y, haliteGrid);
    return shipInfluence;
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

  private static boolean isRightDelta(int xDelta, int yDelta, Direction direction) {
    if (direction == Direction.EAST && xDelta > 0
        || direction == Direction.WEST && xDelta < 0
        || direction == Direction.NORTH && yDelta < 0
        || direction == Direction.SOUTH && yDelta > 0
        || direction == Direction.STILL && xDelta == 0 && yDelta == 0) {
      return true;
    } else {
      return false;
    }
  }

  private double localMoveScore(Ship ship, Position destination) {
    Direction d = haliteGrid.calculateDirection(ship.position, destination);

    int xStart = (d == Direction.EAST) ? 1 : -LOCAL_DISTANCE;
    int xEnd = (d == Direction.WEST) ? -1 : LOCAL_DISTANCE;

    int yStart = (d == Direction.SOUTH) ? 1 : -LOCAL_DISTANCE;
    int yEnd = (d == Direction.NORTH) ? -1 : LOCAL_DISTANCE;

    double bestHaliteRate = -999999;
    for (int y = yStart; y <= yEnd; y++) {
      for (int x = Math.max(xStart, -LOCAL_DISTANCE + Math.abs(y));
           x <= Math.min(xEnd, LOCAL_DISTANCE - Math.abs(y));
           x++) {

        int dx = ship.position.x + x;
        int dy = ship.position.y + y;

        int haliteUnderShip = haliteGrid.get(dx, dy);

        double haliteMined;
        int turnsFromDest;

        if (x == 0 && y == 0) {
          haliteMined = Math.min(Constants.MAX_HALITE - ship.halite, haliteUnderShip / 4);
          turnsFromDest = 1;
        } else {
          haliteMined = Math.min(Constants.MAX_HALITE - ship.halite, haliteUnderShip * 0.44);
          turnsFromDest = haliteGrid.distance(dx, dy, destination.x, destination.y) + 2 + 1;
        }

        double haliteReward = Math.min(
            Constants.MAX_HALITE - ship.halite,
            inspireMap.get(dx, dy) > 1 ? haliteMined * 3 : haliteMined);

//        double tollAfterMining = Math.min(
//            haliteGrid.get(dx, dy), (djikstraGrid.costCache.get(dx, dy) - haliteMined) * 0.10);

        int toll = (x != 0 || y != 0) ? haliteGrid.get(ship.position.x, ship.position.y) / 10 : 0;

        double haliteRate = (haliteReward - toll) / turnsFromDest;
        if (haliteRate > bestHaliteRate) {
          bestHaliteRate = haliteRate;
        }
      }
    }

    return bestHaliteRate;
  }

  private double explorePotentialScore(Ship ship, Position destination) {
    int xStart = ship.position.x - (haliteGrid.width / 2);
    int xEnd = ship.position.x + (haliteGrid.height / 2);

    int yStart = ship.position.y - (haliteGrid.height / 2);
    int yEnd = ship.position.y + (haliteGrid.height / 2);

    Direction d = haliteGrid.calculateDirection(ship.position, destination);
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
        double multiplier = inspireMap.get(x, y) > 1 ? 3.0 : 1.0;
        double adjustedHalite = haliteGrid.get(x, y) * multiplier;

        double haliteCollectedEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, haliteGrid.get(x, y) * 0.44);
        double haliteRewardEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, adjustedHalite * 0.44);

        int turnsFromDest = haliteGrid.distance(x, y, destination.x, destination.y) + 3;

        double moveToll = turnsFromDest <= 4 ? 0 : haliteGrid.get(destination.x, destination.y) / 10;

        double tollAfterMining = Math.min(
            haliteGrid.get(x, y),
            (djikstraGrid.costCache.get(x, y) - haliteCollectedEstimate) * 0.10 + moveToll);

        double haliteRate = (haliteRewardEstimate - tollAfterMining) / turnsFromDest;

        topNRates.add(haliteRate);
        int queueSize = (d == Direction.STILL) ? 24 : 6;
        if (topNRates.size() > queueSize) {
          topNRates.poll();
        }
      }
    }

    double haliteRateSum = topNRates.stream().mapToDouble(j -> j).sum();
    int haliteRateCount = Math.max(1, topNRates.size());

    return haliteRateSum / haliteRateCount;
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    int enemyMinHalite = enemyThreatMap.get(destination.x, destination.y);
    int diff = enemyMinHalite - ship.halite;
    if (enemyMinHalite > -1 && diff <= 0 && haliteGrid.distance(destination, home) > 2) {
      return diff;
    } else {
      return 0;
    }
  }

  private double killScore(Ship ship, Position destination) {
    int killPotentialGain = killMap.get(destination.x, destination.y);
    return killPotentialGain;
  }

  boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, home) + 5 + (shipCount / 5) >= turnsRemaining;
  }
}


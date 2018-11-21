package shipagent;

import com.google.common.collect.ImmutableSet;
import hlt.*;
import map.DjikstraGrid;
import map.Grid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MoveScorer {

  private final Grid<Integer> haliteGrid;
  private final Position home;
  private final int turnsRemaining;
  private final Set<Ship> myShips;

  final DjikstraGrid djikstraGrid;

  final Grid<Double> shipInfluenceMap;
  final Grid<Integer> enemyThreatMap;
  final Grid<Integer> enemyRetreatMap;
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
    this.enemyRetreatMap = InfluenceMaps.retreatMap(enemyShips, playerDropOffs, haliteGrid);
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

    // Score associated with Halite gain of turn
    double homeScore = goHomeScore(ship, destination, endTheGame);
    double mineScore = endTheGame ? 0 : goMineScore(ship, destination);
    double enemyInfluence = getEnemyInfluence(ship, destination);
    double killScore = killScore(ship, destination);

    return new DecisionVector(
        homeScore,
        mineScore,
        enemyInfluence,
        killScore);
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    double haliteCostToHome = endTheGame
        ? 0 : djikstraGrid.costCache.get(destination.x, destination.y) * 0.10;

    double moveHomeOpportunityCost = 1.0 * (ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE);
    return moveHomeOpportunityCost * (ship.halite - haliteCostToHome) / (haliteGrid.distance(destination, home) + 5);
  }

  private double goMineScore(Ship ship, Position destination) {
    double stayMineScore = 0;
    if (ship.position.equals(destination)) {
      stayMineScore = Math.min(Constants.MAX_HALITE - ship.halite, haliteGrid.get(ship.position.x, ship.position.y) / 3);
      if (inspireMap.get(destination.x, destination.y) > 1) {
        stayMineScore *= 3;
      }
    }

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

    double bestHaliteRate = -9999999;
    for (int y = yStart; y <= yEnd; y++) {
      for (int x = xStart; x <= xEnd; x++) {


//        if (haliteGrid.distance(x, y, ship.position.x, ship.position.y) > 4
//            && shipInfluenceMap.get(x, y) > 1.5) {
//          continue;
//        }

        double multiplier = Math.min(1.0, 1.0 / shipInfluenceMap.get(x, y));
        double adjustedHalite = haliteGrid.get(x, y) * multiplier;
        double haliteReward = inspireMap.get(x, y) > 1 ? adjustedHalite * 2.2 : adjustedHalite;

        double haliteCollectedEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, adjustedHalite * 0.58);
        double haliteRewardEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, haliteReward * 0.58);

        int turnsFromDest = haliteGrid.distance(x, y, destination.x, destination.y) + 3;

        double moveToll = destination.x == x && destination.y == y
            ? 0 : haliteGrid.get(destination.x, destination.y) / 10;

        double tollAfterMining = Math.min(
            haliteGrid.get(x, y),
            (djikstraGrid.costCache.get(x, y) - haliteCollectedEstimate) * 0.10 + moveToll);

        double haliteRate = (haliteRewardEstimate - tollAfterMining) / turnsFromDest;
        if (haliteRate > bestHaliteRate) {
          bestHaliteRate = haliteRate;
        }
      }
    }

    return Math.max(stayMineScore, bestHaliteRate);
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    int enemyMinHalite = enemyThreatMap.get(destination.x, destination.y);
    if (enemyMinHalite >= 0) {
      return enemyMinHalite - ship.halite;
    }

    return 0;
  }

  private double killScore(Ship ship, Position destination) {
    int killPotentialGain = enemyRetreatMap.get(destination.x, destination.y);
    if (killPotentialGain == -1) {
      return 0;
    } else {
      return killPotentialGain - ship.halite;
    }
  }

  boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, home) + 5 + (shipCount / 5) >= turnsRemaining;
  }
}


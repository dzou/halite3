package shipagent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Constants;
import hlt.Position;
import hlt.Ship;
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
  private final Map<Position, Ship> enemyShips;

  final DjikstraGrid djikstraGrid;
  final Grid<Double> shipInfluenceGrid;

  public MoveScorer(
      Grid<Integer> haliteGrid,
      Position home,
      int turnsRemaining,
      Collection<Ship> myShips,
      Collection<Ship> enemyShips) {

    this.haliteGrid = haliteGrid;
    this.home = home;
    this.turnsRemaining = turnsRemaining;
    this.myShips = ImmutableSet.copyOf(myShips);
    this.enemyShips = enemyShips.stream().collect(ImmutableMap.toImmutableMap(e -> e.position, e -> e));

    this.djikstraGrid = DjikstraGrid.create(haliteGrid, home);
    this.shipInfluenceGrid = buildShipInfluenceGrid(haliteGrid, myShips);
  }

  DecisionVector scorePosition(Ship ship, Position destination) {
    boolean endTheGame = isTimeToEndGame(ship, myShips.size());

    // Score associated with Halite gain of turn
    double homeScore = goHomeScore(ship, destination, endTheGame);
    double mineScore = endTheGame ? 0 : goMineScore(ship, destination);
    double enemyInfluence = getEnemyInfluence(ship, destination);

    return new DecisionVector(homeScore, mineScore, enemyInfluence);
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    int haliteCostToHome = endTheGame
        ? 0 : djikstraGrid.costCache.get(ship.position.x, ship.position.y) / 10;

    double moveHomeOpportunityCost = 1.0 * ship.halite / Constants.MAX_HALITE;
    return moveHomeOpportunityCost * (ship.halite - haliteCostToHome) / (haliteGrid.distance(destination, home) + 5);
  }

  private double goMineScore(Ship ship, Position destination) {
    double stayMineScore = 0;
    if (ship.position.equals(destination)) {
      stayMineScore = Math.min(Constants.MAX_HALITE - ship.halite, haliteGrid.get(ship.position.x, ship.position.y) / 3);
    }

    double bestHaliteRate = -9999999;
    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        double adjustedHalite =
            Math.min(1000, Math.max(0, haliteGrid.get(x, y) - shipInfluenceGrid.get(x, y)));

        double haliteCollectedEstimate = Math.min(
            Constants.MAX_HALITE - ship.halite, adjustedHalite * 0.58);
        int turnsFromDest = haliteGrid.distance(x, y, destination.x, destination.y) + 3;

        double tollAfterMining = 0.1 * (haliteGrid.get(x, y) - haliteCollectedEstimate);

        double haliteRate = (haliteCollectedEstimate - tollAfterMining) / turnsFromDest;
        if (haliteRate > bestHaliteRate) {
          bestHaliteRate = haliteRate;
        }
      }
    }

    return Math.max(stayMineScore, bestHaliteRate);
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    HashSet<Position> influencedPositions = haliteGrid.getNeighbors(destination);
    influencedPositions.add(destination);

    int minEnemyHalite = influencedPositions.stream()
        .map(pos -> enemyShips.get(pos))
        .filter(s -> s != null)
        .mapToInt(s -> s.halite)
        .min()
        .orElse(-1);

    if (minEnemyHalite == -1) {
      return 0;
    }

    int haliteDiff = minEnemyHalite - ship.halite;
    if (haliteDiff > 0) {
      return 0.1 * haliteDiff;
    } else {
      return haliteDiff;
    }
  }

  boolean isTimeToEndGame(Ship ship, int shipCount) {
    return haliteGrid.distance(ship.position, home) + 5 + (shipCount / 5) >= turnsRemaining;
  }

  static Grid<Double> buildShipInfluenceGrid(Grid<Integer> haliteGrid, Collection<Ship> myShips) {
    Grid<Double> shipInfluenceGrid = new Grid<Double>(haliteGrid.width, haliteGrid.height, 0.0);

    for (Ship ship : myShips) {
      for (int y = ship.position.y - 3; y <= ship.position.y + 3; y++) {
        for (int x = ship.position.x - 3; x <= ship.position.x + 3; x++) {
          double miningPotential = Math.min(
              haliteGrid.get(x, y) / 4,
              (Constants.MAX_HALITE - ship.halite) * 0.125 / (haliteGrid.distance(x, y, ship.position.x, ship.position.y) + 1)
          );

          double prev = shipInfluenceGrid.get(x, y);
          shipInfluenceGrid.set(x, y, prev + miningPotential);
        }
      }
    }
    return shipInfluenceGrid;
  }
}


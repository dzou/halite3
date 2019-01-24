package shipagent;

import bot.HaliteStatTracker;
import com.google.common.collect.ImmutableSet;
import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import tiles.GoalAssignment;
import tiles.SafetyScorer;
import tiles.TileScoreEntry;

import java.util.HashSet;
import java.util.Set;

public class MoveScorer {

  private final MapOracle mapOracle;

  final GoalAssignment goalAssignment;
  private final SafetyScorer safetyScorer;
  // private final FocusGrid focusGrid;

  public MoveScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.goalAssignment = new GoalAssignment(mapOracle);
    this.safetyScorer = new SafetyScorer(mapOracle);
    // this.focusGrid = FocusGrid.create(mapOracle);
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
    Direction moveDir = mapOracle.haliteGrid.calculateDirection(ship.position, destination);

    boolean endTheGame = mapOracle.isTimeToEndGame(ship, mapOracle.myShips.size());

    double homeScore = goHomeScore(ship, destination, endTheGame);
    double focusScore = 0;
    TileScoreEntry tileScoreEntry = goalAssignment.scoreLocalTile(ship, moveDir);
    double enemyInfluence = getEnemyInfluence(ship, destination);
    double killScore = killScore(ship, destination);

    if (!endTheGame) {
      return new DecisionVector(
          homeScore,
          tileScoreEntry,
          enemyInfluence,
          killScore,
          focusScore);
    } else {
      return new DecisionVector(
          1.0 * ship.halite / mapOracle.distance(destination, mapOracle.getNearestHome(destination)) + 5,
          new TileScoreEntry(Position.at(-1, -1), 0, 0),
          enemyInfluence,
          killScore,
          focusScore);
    }
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    double payload = ship.halite;
    if (destination.equals(ship.position)) {
      double multiplier = mapOracle.inspireMap.get(ship.position.x, ship.position.y) > 1 ? 3 : 1;
      payload += multiplier * 0.25 * mapOracle.haliteGrid.get(ship.position.x, ship.position.y);
    }
    payload = Math.min(Constants.MAX_HALITE, payload);

    double haliteCostToHome = endTheGame ? 0 : mapOracle.goHomeCost(destination);
    double moveHomeOpportunityCost = (1.0 * ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE);

    return moveHomeOpportunityCost * (payload - haliteCostToHome)
        / (mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) + 4);
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    return safetyScorer.safetyScore(ship, destination);
  }

  private double killScore(Ship ship, Position destination) {
//    if (mapOracle.influenceDifferenceAtPoint(destination.x, destination.y) < 0) {
//      return 0;
//    }
    if (mapOracle.distance(destination, mapOracle.nearestEnemyBase(destination)) <= 2) {
      return 0.0;
    }

    if (!mapOracle.enemyDropoffs.contains(destination)
        && mapOracle.enemyShipPositionsMap.containsKey(destination)) {

      Ship enemyShip = mapOracle.enemyShipPositionsMap.get(destination);
      Set<Position> killFromPoints = mapOracle.baseManager.findGoHomeDirections(enemyShip);

      if (2.0 * ship.halite < enemyShip.halite
          && enemyShip.halite > 300
          && killFromPoints.contains(ship.position)) {
          return enemyShip.halite * 0.08;
      }
    }

    Set<Ship> enemyNeighbors = mapOracle.haliteGrid.getNeighbors(destination).stream()
        .map(pos -> mapOracle.enemyShipPositionsMap.get(pos))
        .filter(s -> s != null)
        .filter(enemy -> 2.5 * ship.halite < enemy.halite && enemy.halite > 350)
        .collect(ImmutableSet.toImmutableSet());


    double result = 0.0;
    for (Ship enemy : enemyNeighbors) {
      Set<Position> killToPoints = mapOracle.baseManager.findGoHomeDirections(enemy);
      if (killToPoints.contains(destination)) {
        result = Math.max(result, enemy.halite * 0.04);
      }
    }

    return result;
  }
}


package shipagent;

import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.FocusGrid;
import tiles.GoalAssignment;
import tiles.SafetyScorer;
import tiles.TileScoreEntry;

import java.util.HashSet;

public class MoveScorer {

  private final MapOracle mapOracle;

  private final GoalAssignment goalAssignment;
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
    double focusScore = 0; // focusGrid.score(ship, moveDir);
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
          homeScore * 1000,
          new TileScoreEntry(Position.at(-1, -1), 0, 0),
          enemyInfluence,
          killScore,
          focusScore);
    }
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    double haliteCostToHome = endTheGame ? 0 : mapOracle.goHomeCost(destination);

    double payload = ship.halite;
    if (destination.equals(ship.position)) {
      payload += goalAssignment.mineScore(ship);
    }

    double moveHomeOpportunityCost = (1.0 * ship.halite * ship.halite) / (Constants.MAX_HALITE * Constants.MAX_HALITE);
    return moveHomeOpportunityCost * (payload - haliteCostToHome)
        / (mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) + 4);
  }

  private double getEnemyInfluence(Ship ship, Position destination) {
    return safetyScorer.safetyScore(ship, destination);
  }

  private double killScore(Ship ship, Position destination) {
//    int killPotentialGain = mapOracle.killMap.get(destination.x, destination.y);
//    return killPotentialGain;
    return 0;
  }
}


package shipagent;

import hlt.Constants;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;
import map.Zone;
import tiles.GoalAssignment;
import tiles.TileScoreEntry;
import tiles.ZoneScoreEntry;

import java.util.HashSet;

public class MoveScorer {

  private final MapOracle mapOracle;

  private final GoalAssignment goalAssignment;

  public MoveScorer(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.goalAssignment = new GoalAssignment(mapOracle);
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
    TileScoreEntry tileScoreEntry = goalAssignment.scoreLocalTile(ship, moveDir);
    ZoneScoreEntry zoneScoreEntry = goalAssignment.scoreZone(ship, moveDir);
    double enemyInfluence = getEnemyInfluence(ship, destination);
    double killScore = killScore(ship, destination);

    if (!endTheGame) {
      return new DecisionVector(
          homeScore,
          tileScoreEntry,
          zoneScoreEntry,
          enemyInfluence,
          killScore);
    } else {
      return new DecisionVector(
          homeScore,
          new TileScoreEntry(Position.at(-1, -1), 0, 0),
          new ZoneScoreEntry(Zone.EMPTY, 0),
          enemyInfluence,
          killScore);
    }
  }

  private double goHomeScore(Ship ship, Position destination, boolean endTheGame) {
    double haliteCostToHome = endTheGame ? 0 : mapOracle.goHomeCost(destination);

    double payload = ship.halite;
    if (destination.equals(ship.position)) {
      payload += goalAssignment.mineScore(ship);
    }

    double moveHomeOpportunityCost = 1.0 * (ship.halite) / (Constants.MAX_HALITE);
    return moveHomeOpportunityCost * (payload - haliteCostToHome)
        / (mapOracle.haliteGrid.distance(destination, mapOracle.getNearestHome(ship.position)) + 4);
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


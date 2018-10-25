package map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import hlt.*;

import java.util.*;

public class ExploreCommander {

  private ImmutableSet<Position> forbiddenPositions;

  public ExploreCommander(ImmutableSet<Position> forbiddenPositions) {
    this.forbiddenPositions = forbiddenPositions;
  }

  public List<Command> getExploreCommands(Map<EntityId, Ship> ships, GameMap gameMap) {
    ImmutableList.Builder<Command> result = ImmutableList.builder();
    HashSet<Position> occupiedPositions = new HashSet<>();
    CostGrid costGrid = gameMap.toCostgrid();

    for (Ship ship : ships.values()) {
      Position goal = GoalGenerator.generateRandomGoal(gameMap);
      Path p = Navigator.findShortestPath(ship.position, goal, costGrid);
      if (!p.path.isEmpty()
          && !occupiedPositions.contains(p.path.peek())
          && !forbiddenPositions.contains(p.path.peek())) {
        Position nextPosition = p.path.pop();
        Direction d = calculateDirection(ship.position, nextPosition);

        occupiedPositions.add(nextPosition);
        result.add(Command.move(ship.id, d));
      } else {
        occupiedPositions.add(ship.position);
        result.add(Command.move(ship.id, Direction.STILL));
      }
    }

    return result.build();
  }

  private static Direction calculateDirection(Position start, Position end) {
    if (start.x != end.x) {
      if (end.x > start.x) {
        return Direction.EAST;
      } else {
        return Direction.WEST;
      }
    } else if (start.y != end.y) {
      if (end.y > start.y) {
        return Direction.NORTH;
      } else {
        return Direction.SOUTH;
      }
    }

    return Direction.STILL;
  }
}

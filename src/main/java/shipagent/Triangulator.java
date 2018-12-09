package shipagent;

import com.google.common.collect.HashMultimap;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;

import java.util.*;

public class Triangulator {

  private final MapOracle mapOracle;

  public Triangulator(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
  }

  public Optional<Party> triangulateTarget(Position target, Collection<Ship> triangulators) {
    HashMultimap<Direction, Ship> axisCoveredMap = HashMultimap.create();

    for (Ship triangulatorShip : triangulators) {
      Position triangulator = triangulatorShip.position;

      int catchX = Math.abs(triangulator.x - target.x);
      int catchY = Math.abs(triangulator.y - target.y);

      if (triangulator.x - target.x >= catchY) {
        axisCoveredMap.put(Direction.WEST, triangulatorShip);
      } else if (target.x - triangulator.x >= catchY) {
        axisCoveredMap.put(Direction.EAST, triangulatorShip);
      }

      if (triangulator.y - target.y >= catchX) {
        axisCoveredMap.put(Direction.NORTH, triangulatorShip);
      } else if (target.y - triangulator.y >= catchX) {
        axisCoveredMap.put(Direction.SOUTH, triangulatorShip);
      }
    }

    HashMap<Ship, Direction> axisAssignments = new HashMap<>();
    HashMap<Ship, Direction> movementAssignments = new HashMap<>();

    while (!axisCoveredMap.isEmpty()) {
      Direction bestAxis = axisCoveredMap.keySet().stream()
          .min(Comparator.comparingInt(d -> axisCoveredMap.get(d).size()))
          .get();
      Set<Ship> candidates = axisCoveredMap.get(bestAxis);

      Optional<Ship> bestShipOptional = candidates.stream()
          .min(Comparator.comparingInt(s -> mapOracle.haliteGrid.distance(s.position, target)));

      if (!bestShipOptional.isPresent()) {
        break;
      }

      Ship best = bestShipOptional.get();

      axisCoveredMap.removeAll(bestAxis);
      for (Direction d : Direction.ALL_CARDINALS) {
        axisCoveredMap.remove(d, best);
      }

      axisAssignments.put(best, bestAxis);

      int dx = best.position.x - target.x;
      int dy = best.position.y - target.y;

      if (dx == 0 || dy == 0) {
        movementAssignments.put(best, bestAxis);
      } else {
        if (bestAxis == Direction.NORTH || bestAxis == Direction.SOUTH) {
          if (dx > 0) {
            movementAssignments.put(best, Direction.WEST);
          } else {
            movementAssignments.put(best, Direction.EAST);
          }
        } else {
          if (dy > 0) {
            movementAssignments.put(best, Direction.NORTH);
          } else {
            movementAssignments.put(best, Direction.SOUTH);
          }
        }
      }
    }


    return Optional.of(new Party(target, axisAssignments, movementAssignments));
  }

  public static class Party {
    public final Position target;
    public final Map<Ship, Direction> axisAssignment;
    public final Map<Ship, Direction> triangulationMoves;

    public Party(
        Position target, Map<Ship, Direction> axisAssignment, Map<Ship, Direction> triangulationMoves) {
      this.target = target;
      this.axisAssignment = axisAssignment;
      this.triangulationMoves = triangulationMoves;
    }

    @Override
    public String toString() {
      return "Party{" +
          "target=" + target +
          "\naxisAssignment=" + axisAssignment +
          "\ntriangulationMoves=" + triangulationMoves +
          '}';
    }
  }
}

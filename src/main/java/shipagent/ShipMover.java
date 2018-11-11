package shipagent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import grid.Grid;
import hlt.Command;
import hlt.Direction;
import hlt.Position;
import hlt.Ship;

import java.util.*;

public class ShipMover {

  private final Grid<Integer> haliteGrid;

  public final HashSet<Position> usedPositions;

  public ShipMover(Grid<Integer> haliteGrid) {
    this.haliteGrid = haliteGrid;
    this.usedPositions = new HashSet<>();
  }

  public List<Command> moveShips(Map<Ship, Decision> mappings) {
    ImmutableList.Builder<Command> commands = ImmutableList.builder();

    Set<Position> previousPositions = mappings.keySet()
        .stream()
        .map(ship -> ship.position)
        .collect(ImmutableSet.toImmutableSet());

    Map<Position, ArrayList<Ship>> blockedCommandMap = new HashMap<>();
    Map<Position, ArrayList<Ship>> freedPositions = new HashMap<>();

    for (Map.Entry<Ship, Decision> entry : mappings.entrySet()) {
      Ship ship = entry.getKey();
      Decision decision = entry.getValue();

      if (!previousPositions.contains(decision.destination)) {
        if (!freedPositions.containsKey(decision.destination)) {
          freedPositions.put(decision.destination, new ArrayList<>());
        }
        freedPositions.get(decision.destination).add(ship);
      } else {
        if (!blockedCommandMap.containsKey(decision.destination)) {
          blockedCommandMap.put(decision.destination, new ArrayList<>());
        }
        blockedCommandMap.get(decision.destination).add(ship);
      }
    }

    for (Map.Entry<Position, ArrayList<Ship>> freedEntry : freedPositions.entrySet()) {
      if (freedEntry.getValue().size() == 0) {
        continue;
      }

      Position to = freedEntry.getKey();
      ArrayList<Ship> blockedShips = freedEntry.getValue();

      while (blockedShips != null
          && !blockedShips.isEmpty()
          && !usedPositions.contains(to)) {

        Ship ship = blockedShips.remove(blockedShips.size() - 1);
        if (ship.halite < haliteGrid.get(ship.position.x, ship.position.y) / 10) {
          break;
        }

        commands.add(Command.move(ship.id, haliteGrid.calculateDirection(ship.position, to)));
        usedPositions.add(to);

        to = ship.position;
        blockedShips = blockedCommandMap.get(ship);
      }
    }

    for (ArrayList<Ship> ships : freedPositions.values()) {
      for (Ship ship : ships) {
        commands.add(Command.move(ship.id, Direction.STILL));
        usedPositions.add(ship.position);
      }
    }

    for (ArrayList<Ship> ships : blockedCommandMap.values()) {
      for (Ship ship : ships) {
        commands.add(Command.move(ship.id, Direction.STILL));
        usedPositions.add(ship.position);
      }
    }

    return commands.build();
  }
}

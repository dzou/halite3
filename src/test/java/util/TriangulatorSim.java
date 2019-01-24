package util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import shipagent.MapOracle;
import shipagent.Triangulator;

import java.util.*;
import java.util.stream.Collectors;

import static util.TestUtil.ship;

public class TriangulatorSim {

  private final MapOracle mapOracle;

  private final Triangulator triangulator;

  public TriangulatorSim(MapOracle mapOracle) {
    this.mapOracle = mapOracle;
    this.triangulator = new Triangulator(mapOracle);
  }

  public void simulate(Position target) {
    Scanner scanner = new Scanner(System.in);

    Collection<Ship> triangulators = mapOracle.enemyShips;

    while (true) {
      printGrid(target, triangulators);

      Optional<Triangulator.Party> partyOption = triangulator.triangulateTarget(target, triangulators);
      if (partyOption.isPresent()) {
        Triangulator.Party party = partyOption.get();

        if (party.triangulationMoves.size() < 4) {
          System.out.println("U WIN");
          System.out.println(party);
          break;
        }

        triangulators = party.triangulationMoves.entrySet().stream()
            .map(entry -> TestUtil.moveShip(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        final Position tmp = target;
        boolean killed = triangulators.stream().anyMatch(s -> s.position.equals(tmp));
        if (killed) {
          System.out.println("you lose.");
          break;
        }

      } else {
        System.out.println("you win.");
        break;
      }

      String input = scanner.nextLine();

      Direction move;
      if (input.equals("u")) {
        move = Direction.NORTH;
      } else if (input.equals("d")) {
        move = Direction.SOUTH;
      } else if (input.equals("r")) {
        move = Direction.EAST;
      } else if (input.equals("l")) {
        move = Direction.WEST;
      } else {
        move = Direction.STILL;
      }
      target = target.directionalOffset(move);

    }

    scanner.close();

  }

  public void printGrid(Position target, Collection<Ship> triangulators) {
    Set<Position> triangulatorPositions = triangulators.stream()
        .map(s -> s.position)
        .collect(Collectors.toSet());

    for (int y = 0; y < mapOracle.haliteGrid.height; y++) {
      for (int x = 0; x < mapOracle.haliteGrid.width; x++) {
        if (x == target.x && y == target.y) {
          System.out.print('x');
        } else if (triangulatorPositions.contains(Position.at(x, y))) {
          System.out.print('o');
        } else {
          System.out.print('.');
        }
      }
      System.out.println();
    }
  }

  public static void main(String[] args) {
    Grid<Integer> haliteGrid = new Grid<>(24, 24, 0);

    ImmutableList<Ship> myShips = ImmutableList.of(
        ship(10, 10, 200)
    );

    ImmutableList<Ship> enemyShips = ImmutableList.of(
        ship(10, 20),
        ship(10, 0, 0),
        ship(20, 10, 0),
        ship(0, 10, 0)
    );

    MapOracle mapOracle = new MapOracle(
        new PlayerId(0),
        haliteGrid,
        9999,
        myShips,
        enemyShips,
        ImmutableMap.of(new PlayerId(0), ImmutableSet.of(Position.at(0, 0))));

    TriangulatorSim sim = new TriangulatorSim(mapOracle);

    sim.simulate(Position.at(10, 10));
  }
}

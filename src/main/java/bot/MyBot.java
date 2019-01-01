package bot;

import hlt.*;
import shipagent.HaliteSpender;
import shipagent.MapOracle;
import shipagent.ShipRouter;

import java.util.*;
import java.util.stream.Collectors;

// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//   Otherwise the names of methods are consistent.
public class MyBot {
  public static void main(final String[] args) {
    final long rngSeed;
    if (args.length > 1) {
      rngSeed = Integer.parseInt(args[1]);
    } else {
      rngSeed = System.nanoTime();
    }
    final Random rng = new Random(rngSeed);

    Game game = new Game();
    // At this point "game" variable is populated with initial map data.
    // This is a good place to do computationally expensive start-up pre-processing.
    // As soon as you call "ready" function below, the 2 second per turn timer will start.
    game.ready("Challenger1");

    Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

    for (; ; ) {
      long startTime = System.currentTimeMillis();

      game.updateFrame();
      final Player me = game.me;
      final GameMap gameMap = game.gameMap;

      final ArrayList<Command> commandQueue = new ArrayList<>();

      MapOracle mapOracle = new MapOracle(
          me.id,
          gameMap.toHaliteGrid(),
          Constants.MAX_TURNS - game.turnNumber,
          game.me.ships.values(),
          game.getEnemyShips(),
          game.getPlayerBases());

      HaliteSpender spender = new HaliteSpender(mapOracle, game.me.halite);
      ShipRouter shipRouter = new ShipRouter(mapOracle);

      HashSet<Ship> shipsToTransform = new HashSet<>();

      // Dropoffs logic
      Optional<Position> dropOffProposal = spender.orderDropoff();
      if (dropOffProposal.isPresent()) {
        Log.log("Dropoff proposal: " + dropOffProposal.get());
        Optional<Ship> shipAtDropoff = mapOracle.orderDropOff(dropOffProposal.get());

        if (shipAtDropoff.isPresent()) {
          Ship ship = shipAtDropoff.get();
          commandQueue.add(Command.transformShipIntoDropoffSite(ship.id));
          shipsToTransform.add(ship);
        }
      } else {
        Log.log("No dropoff suggested.");
      }

      Map<Ship, Position> mappings = shipRouter.routeShips(shipsToTransform);

      boolean movedOnBase = false;

      for (Map.Entry<Ship, Position> mapping : mappings.entrySet()) {
        Ship ship = mapping.getKey();
        Position destination = mapping.getValue();
        if (destination.equals(me.shipyard.position)) {
          movedOnBase = true;
        }

        Direction direction = mapOracle.haliteGrid.calculateDirection(ship.position, destination);
        commandQueue.add(Command.move(ship.id, direction));
      }

      if (me.halite >= Constants.SHIP_COST
          && !movedOnBase
          && spender.shouldMakeShip()) {
        commandQueue.add(me.shipyard.spawn());
      }

      game.endTurn(commandQueue.stream().sorted(Comparator.comparing(c -> c.command)).collect(Collectors.toList()));
      long endTime = System.currentTimeMillis();
      Log.log("Time taken: " + (endTime - startTime));
    }
  }
}

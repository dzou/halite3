package bot;

import grid.Grid;
import hlt.*;
import shipagent.Decision;
import shipagent.ShipRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    game.ready("WorkingVersion_Curr");

    Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

    /**
     * 1. Generate and assign goals.
     * 2. Do Path finding
     * 3. Resolve moves for each path that you stored.
     */

    for (; ; ) {
      game.updateFrame();
      final Player me = game.me;
      final GameMap gameMap = game.gameMap;

      final ArrayList<Command> commandQueue = new ArrayList<>();

      Grid<Integer> haliteGrid = gameMap.toHaliteGrid();

      ShipRouter shipRouter = new ShipRouter(haliteGrid, game.me.shipyard.position);


      Map<Ship, Position> mappings = shipRouter.routeShips(me.ships.values());

      boolean movedOnBase = false;

      for (Map.Entry<Ship, Position> mapping : mappings.entrySet()) {
        Ship ship = mapping.getKey();
        Position destination = mapping.getValue();
        if (destination.equals(me.shipyard.position)) {
          movedOnBase = true;
        }

        Direction direction = haliteGrid.calculateDirection(ship.position, destination);
        commandQueue.add(Command.move(ship.id, direction));
      }

      if (game.turnNumber <= 200 && me.halite >= Constants.SHIP_COST && !movedOnBase) {
        commandQueue.add(me.shipyard.spawn());
      }

      game.endTurn(commandQueue);
    }
  }
}

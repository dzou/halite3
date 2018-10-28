package bot;

import com.google.common.collect.ImmutableList;
import hlt.*;

import java.util.ArrayList;
import java.util.List;
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

    List<String> someWords = ImmutableList.of("Hello", "World");
    Log.log(String.join(" ", someWords));
    Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

    /**
     * 1. Generate and assign goals.
     * 2. Do Path finding
     * 3. Resolve moves for each path that you stored.
     */

    for (;;) {
      game.updateFrame();
      final Player me = game.me;
      final GameMap gameMap = game.gameMap;

      final ArrayList<Command> commandQueue = new ArrayList<>();

//      for (final Ship ship : me.ships.values()) {
//        if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
//          final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
//          commandQueue.add(ship.move(randomDirection));
//        } else {
//          commandQueue.add(ship.stayStill());
//        }
//      }

      // commandQueue.addAll(commands);

      if (
        game.turnNumber <= 200 &&
        me.halite >= Constants.SHIP_COST &&
        !gameMap.at(me.shipyard).isOccupied())
      {
        commandQueue.add(me.shipyard.spawn());
      }

      game.endTurn(commandQueue);
    }
  }
}

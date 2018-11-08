package bot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import grid.DjikstraGrid;
import grid.Grid;
import hlt.*;
import map.Path;
import ml.*;
import shipagent.Decision;
import shipagent.ShipMover;
import shipagent.ShipRouter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MLTrainerBot {

  public static void main(final String[] args) throws IOException {
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
    game.ready("ML_TRAINER");

    Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

    File weightsFile = new File("weights.txt");
    ExploreModel model = new ExploreModel(weightsFile);
    ExploreTrainer trainer = new ExploreTrainer(model);


    for (; ; ) {
      game.updateFrame();
      final Player me = game.me;
      final GameMap gameMap = game.gameMap;

      final ArrayList<Command> commandQueue = new ArrayList<>();

      Grid<Integer> haliteGrid = gameMap.toHaliteGrid();

      ShipRouter shipRouter = new ShipRouter(haliteGrid, game.me.shipyard.position, model);
      Map<Ship, Decision> mappings = shipRouter.routeShips(me.ships.values());

      for (Map.Entry<Ship, Decision> entry : mappings.entrySet()) {
        Ship ship = entry.getKey();
        Decision decision = entry.getValue();

        Position next = ship.position;
        if (decision.path.path.size() >= 2) {
          next = Iterables.get(decision.path.path, 1);
        }

        trainer.addStateActionPair(decision.type, decision.state);
        if (next.equals(me.shipyard.position)) {
          if (ship.halite <= 0) {
            trainer.updateWeights(-10);
          } else {
            trainer.updateWeights(ship.halite);
          }
        }
//        else {
//          if (decision.type != ActionType.STAY) {
//        trainer.updateSingle(
//            -3.0,
//            decision.type,
//            decision.state);
//          }
//        }
      }

      ShipMover shipMover = new ShipMover(haliteGrid);
      List<Command> moveCommands = shipMover.moveShips(mappings);
      commandQueue.addAll(moveCommands);

      if (game.turnNumber <= 200
          && me.halite >= Constants.SHIP_COST
          && !shipMover.usedPositions.contains(me.shipyard.position)
          && me.ships.size() == 0) {
        commandQueue.add(me.shipyard.spawn());
      }


      game.endTurn(commandQueue);
      Log.log(model.toString());

      if (game.turnNumber == Constants.MAX_TURNS - 1) {
        trainer.writeWeights();
      }
    }
  }
}

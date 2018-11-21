package hlt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;

public class Game {
  public int turnNumber;
  public final PlayerId myId;
  public final ArrayList<Player> players = new ArrayList<>();
  public final Player me;
  public final GameMap gameMap;

  public Game() {
    Constants.populateConstants(Input.readLine());

    final Input input = Input.readInput();
    final int numPlayers = input.getInt();
    myId = new PlayerId(input.getInt());

    Log.open(myId.id);

    for (int i = 0; i < numPlayers; ++i) {
      players.add(Player._generate());
    }
    me = players.get(myId.id);
    gameMap = GameMap._generate();
  }

  public Map<PlayerId, Set<Position>> getPlayerBases() {
    ImmutableMap.Builder<PlayerId, Set<Position>> allBases = ImmutableMap.builder();
    for (Player player : players) {
      ImmutableSet.Builder<Position> playerBases = ImmutableSet.builder();
      playerBases.add(player.shipyard.position);
      for (Dropoff dropoff : player.dropoffs.values()) {
        playerBases.add(dropoff.position);
      }

      allBases.put(player.id, playerBases.build());
    }

    return allBases.build();
  }

  public HashSet<Ship> getEnemyShips() {
    HashSet<Ship> enemyShips = new HashSet<>();
    for (Player player : players) {
      if (player.id.id != me.id.id) {
        enemyShips.addAll(player.ships.values());
      }
    }
    return enemyShips;
  }

  public void ready(final String name) {
    System.out.println(name);
  }

  public void updateFrame() {
    turnNumber = Input.readInput().getInt();
    Log.log("=============== TURN " + turnNumber + " ================");

    for (int i = 0; i < players.size(); ++i) {
      final Input input = Input.readInput();

      final PlayerId currentPlayerId = new PlayerId(input.getInt());
      final int numShips = input.getInt();
      final int numDropoffs = input.getInt();
      final int halite = input.getInt();

      players.get(currentPlayerId.id)._update(numShips, numDropoffs, halite);
    }

    gameMap._update();

    for (final Player player : players) {
      for (final Ship ship : player.ships.values()) {
        gameMap.at(ship).markUnsafe(ship);
      }

      gameMap.at(player.shipyard).structure = player.shipyard;

      for (final Dropoff dropoff : player.dropoffs.values()) {
        gameMap.at(dropoff).structure = dropoff;
      }
    }
  }

  public void endTurn(final Collection<Command> commands) {
    for (final Command command : commands) {
      System.out.print(command.command);
      System.out.print(' ');
    }
    System.out.println();
  }
}

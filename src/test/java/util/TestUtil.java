package util;

import hlt.*;

public class TestUtil {

  public static Ship ship(int x, int y) {
    return ship(x, y, 0);
  }

  public static Ship ship(int x, int y, int halite) {
    return new Ship(new PlayerId(0), new EntityId(x * 100 + y), Position.at(x, y), halite);
  }

  public static Ship moveShip(Ship ship, Direction direction) {
    return new Ship(ship.owner, ship.id, ship.position.directionalOffset(direction), ship.halite);
  }

}

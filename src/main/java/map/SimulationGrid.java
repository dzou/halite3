package map;

import hlt.Direction;
import hlt.Position;

import java.util.HashMap;
import java.util.Map;

public class SimulationGrid {

  private final Grid<Integer> haliteGrid;
  private final Map<Position, Integer> modifiedHalite;

  private Position shipPosition;
  private int haliteGained;

  public SimulationGrid(Grid<Integer> haliteGrid, Position shipPosition) {
    this.haliteGrid = haliteGrid;
    this.modifiedHalite = new HashMap<>();

    this.shipPosition = shipPosition;
    this.haliteGained = 0;
  }

  public void moveShip(Direction dir) {
    moveShip(dir, false);
  }

  public void moveShip(Direction dir, boolean useInspire) {
    if (dir == Direction.STILL) {
      int haliteMined = getHalite(shipPosition) / 4;
      setHalite(shipPosition, getHalite(shipPosition) - haliteMined);

      if (useInspire) {
        haliteMined *= 3;
      }
      haliteGained += haliteMined;
    } else {
      haliteGained -= getHalite(shipPosition) / 10;
      shipPosition = haliteGrid.normalize(shipPosition.directionalOffset(dir));
    }
  }

  public int getHalite(Position pos) {
    return modifiedHalite.getOrDefault(pos, haliteGrid.get(pos.x, pos.y));
  }

  public Position getPosition() {
    return shipPosition;
  }

  public int getHaliteGained() {
    return haliteGained;
  }

  public int distance(Position x, Position y) {
    return haliteGrid.distance(x, y);
  }

  private void setHalite(Position pos, int haliteAmt) {
    modifiedHalite.put(pos, haliteAmt);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        stringBuilder.append(getHalite(Position.at(x, y)) + " ");
      }
      stringBuilder.append("\n");
    }

    return stringBuilder.toString();
  }
}

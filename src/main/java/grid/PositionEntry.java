package grid;

import hlt.Position;

public class PositionEntry {
  public final Position position;
  public final int costToPosition;

  public PositionEntry(Position position, int costToPosition) {
    this.position = position;
    this.costToPosition = costToPosition;
  }

  public static PositionEntry of(Position position, int costToPosition) {
    return new PositionEntry(position, costToPosition);
  }
}

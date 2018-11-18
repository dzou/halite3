package shipagent;

import hlt.Direction;
import hlt.Position;

public class Decision {

  public final Direction direction;

  public final Position destination;

  public final double score;

  public Decision(Direction direction, Position destination, double score) {
    this.direction = direction;
    this.destination = destination;
    this.score = score;
  }

  @Override
  public String toString() {
    return "Decision{" +
        "direction=" + direction +
        ", destination=" + destination +
        ", score=" + score +
        '}';
  }
}

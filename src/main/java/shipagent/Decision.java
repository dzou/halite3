package shipagent;

import hlt.Direction;
import hlt.Position;

public class Decision {

  public final Direction direction;

  public final Position destination;

  public final DecisionVector scoreVector;

  public Decision(Direction direction, Position destination, DecisionVector score) {
    this.direction = direction;
    this.destination = destination;
    this.scoreVector = score;
  }

  @Override
  public String toString() {
    return "Decision{" +
        "direction=" + direction +
        ", destination=" + destination +
        ", score=" + scoreVector +
        '}';
  }
}

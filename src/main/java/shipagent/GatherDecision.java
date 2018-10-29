package shipagent;

import hlt.Position;
import map.Path;

public class GatherDecision {

  public final Type type;

  public final Position destination;

  public final Path path;

  public final double decisionScore;

  public GatherDecision(Type type, Position destination, Path path, double decisionScore) {
    this.type = type;
    this.destination = destination;
    this.path = path;
    this.decisionScore = decisionScore;
  }

  @Override
  public String toString() {
    return "GatherDecision{" +
        "type=" + type +
        ", destination=" + destination +
        ", path=" + path +
        ", decisionScore=" + decisionScore +
        '}';
  }

  enum Type {
    EXPLORE,
    RETURN,
    STAY;
  }
}

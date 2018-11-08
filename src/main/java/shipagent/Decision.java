package shipagent;

import hlt.Position;
import map.Path;
import ml.ActionType;
import ml.ExploreVector;

public class Decision {

  public final ActionType type;

  public final ExploreVector state;

  public final Path path;

  public Decision(ActionType type, ExploreVector state, Path path) {
    this.type = type;
    this.state = state;
    this.path = path;
  }

  @Override
  public String toString() {
    return "Decision{" +
        "type=" + type +
        ", state=" + state +
        ", path=" + path +
        '}';
  }
}

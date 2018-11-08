package ml;

import hlt.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ExploreTrainer {

  private static final String WEIGHTS_FILENAME = "weights.txt";

  private ExploreModel model;

  private ArrayList<StateActionPair> stateActionPairs;

  public ExploreTrainer(ExploreModel model) {
    this.model = model;
    this.stateActionPairs = new ArrayList<>();
  }

  public void addStateActionPair(ActionType action, ExploreVector vector) {
    stateActionPairs.add(new StateActionPair(action, vector));
  }

  public void updateSingle(double rewardAmt, ActionType decisionType, ExploreVector state) {
    model.updateModel(rewardAmt, decisionType, state);
  }

  public void updateWeights(double rewardAmt) {
    for (int i = 0; i < stateActionPairs.size(); i++) {
      StateActionPair stateActionPair = stateActionPairs.get(i);

      double outcomeValue = Math.pow(0.55, stateActionPairs.size() - i - 1) * (rewardAmt - 5.0);

      model.updateModel(
          outcomeValue,
          stateActionPair.action,
          stateActionPair.vector);
    }
    stateActionPairs.clear();
  }

  public void writeWeights() throws IOException {
    Path path = Paths.get("weights.txt");
    Files.write(path, model.toString().getBytes());
  }

  private static class StateActionPair {
    public final ActionType action;
    public final ExploreVector vector;

    public StateActionPair(ActionType action, ExploreVector vector) {
      this.action = action;
      this.vector = vector;
    }
  }
}

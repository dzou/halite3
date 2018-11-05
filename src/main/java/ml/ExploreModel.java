package ml;

import hlt.Constants;
import hlt.Log;
import org.jblas.DoubleMatrix;
import org.jblas.Geometry;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class ExploreModel {

  private static final double SAMPLER_EPSILON = 0.075;

  private static final double STEP_SIZE = 0.00001;

  private final DoubleMatrix weights;

  public ExploreModel() {
    weights = DoubleMatrix.zeros(ActionType.values().length, ExploreVector.Feature.values().length);
  }

  public ExploreModel(File f) throws FileNotFoundException {
    weights = DoubleMatrix.zeros(ActionType.values().length, ExploreVector.Feature.values().length);

    int row = 0;

    Scanner scanner = new Scanner(f);
    while (scanner.hasNextLine()) {
      String weightsLine = scanner.nextLine();
      String[] tokens = weightsLine.split(" ");
      for (int i = 0; i < tokens.length; i++) {
        weights.put(row, i, Double.parseDouble(tokens[i]));
      }
      row += 1;
    }
    scanner.close();
  }

  public double getScore(ActionType actionType, ExploreVector vector) {
    int actionIdx = actionType.ordinal();
    DoubleMatrix actionWeights = weights.getRow(actionIdx);
    return dot(actionWeights, vector.toVector());
  }

  public void updateModel(
      double outcomeValue, ActionType actionType, ExploreVector vector) {

    double modelValue = getScore(actionType, vector);

    DoubleMatrix normalizedVector = Geometry.normalize(vector.toVector());
    DoubleMatrix updateVector = normalizedVector.mul(STEP_SIZE * (outcomeValue - modelValue));

    Log.log("Differential: " + (outcomeValue - modelValue));
    Log.log("UPDATE: " + actionType + "[" +  actionType.ordinal() + "]" + " -- " + updateVector);

    int actionIdx = actionType.ordinal();
    DoubleMatrix updateRow = weights.getRow(actionIdx).add(updateVector);
    weights.putRow(actionIdx, updateRow);
  }

  public ActionType sampleAction(ExploreVector vector) {
    double rand = Math.random();
    ActionType[] actionTypes = ActionType.values();

    Log.log("Sampling action for state: " + vector);

//    int haliteOnShip = (int) (Constants.MAX_HALITE - vector.vectorValues.get(ExploreVector.Feature.HALITE_CAPACITY_ON_SHIP));
//    int costToLeaveTile = (int) (vector.vectorValues.get(ExploreVector.Feature.HALITE_ON_TILE) / 10);
//    if (haliteOnShip < costToLeaveTile) {
//      Log.log("Forced Stay. ");
//      return ActionType.STAY;
//    }

    if (rand < SAMPLER_EPSILON) {
      int idx = (int) (Math.random() * 3);
      Log.log("Random decision triggered: " + actionTypes[idx]);
      return actionTypes[idx];
    }

    DoubleMatrix featureVector = vector.toVector();

    double maxActivation = -99999999.0;
    int bestIdx = 0;
    for (int i = 0; i < actionTypes.length; i++) {
      double activation = dot(weights.getRow(i), featureVector);
      Log.log(actionTypes[i] + ": " + Double.toString(activation));
      if (activation > maxActivation) {
        maxActivation = activation;
        bestIdx = i;
      }
    }

    Log.log("Chose: " + actionTypes[bestIdx]);
    return actionTypes[bestIdx];
  }

  DoubleMatrix getWeights() {
    return weights;
  }

  private double dot(DoubleMatrix a, DoubleMatrix b) {
    return a.dot(b);
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("00.00000");

    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < weights.rows; i++) {
      DoubleMatrix row = weights.getRow(i);
      for (int j = 0; j < row.length; j++) {
        stringBuilder.append(row.get(j) + " ");
      }
      stringBuilder.append("\n");
    }

    return stringBuilder.toString();
  }
}

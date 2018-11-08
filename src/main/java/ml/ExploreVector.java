package ml;

import com.google.common.collect.ImmutableList;
import org.jblas.DoubleMatrix;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExploreVector {

  public final HashMap<Feature, Double> vectorValues;

  public ExploreVector() {
    vectorValues = new HashMap<>();
  }

  public static ExploreVector of(double... values) {
    ExploreVector vector = new ExploreVector();

    Feature[] featureList = Feature.values();

    for (int i = 0; i < Feature.values().length; i++) {
      vector.setValue(featureList[i], values[i]);
    }

    return vector;
  }

  public void setValue(Feature feature, double value) {
    vectorValues.put(feature, value);
  }

  public DoubleMatrix toVector() {
    if (vectorValues.size() != Feature.values().length) {
      throw new RuntimeException("Vector contains different # of entries than features. " + vectorValues);
    }

    DoubleMatrix vector = DoubleMatrix.zeros(Feature.values().length);
    for (Map.Entry<Feature, Double> entry : vectorValues.entrySet()) {
      Feature feature = entry.getKey();
      vector.put(feature.ordinal(), entry.getValue());
    }

    return vector;
  }

  @Override
  public String toString() {
    ImmutableList<Feature> sortedFeatures = vectorValues
        .keySet()
        .stream()
        .sorted(Comparator.comparingInt(f -> f.ordinal()))
        .collect(ImmutableList.toImmutableList());

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("ExploreVector:\n");
    for (Feature f : sortedFeatures) {
      stringBuilder.append(f + " = " + vectorValues.get(f) + "\n");
    }
    return stringBuilder.toString();
  }

  public enum Feature {
    EXPLORE_DIST_TO_HOME,

    MAX_HALITE_COLLECT,

    HALITE_CAPACITY_ON_SHIP,

    PROJECTED_HALITE_TO_HOME,

    PROJECTED_HALITE_THRU_EXPLORE_TO_HOME;
  }
}

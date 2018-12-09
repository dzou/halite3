package shipagent;

import com.google.common.collect.ImmutableList;

import java.text.DecimalFormat;
import java.util.stream.DoubleStream;

public class DecisionVector {

  public final double goHomeScore;
  public final double localMoveScore;
  public final double explorePotentialScore;
  public final double enemyThreatScore;
  public final double killScore;

  public DecisionVector(double goHomeScore, double localMoveScore, double explorePotentialScore, double enemyThreatScore, double killScore) {
    this.goHomeScore = goHomeScore;
    this.localMoveScore = localMoveScore;
    this.explorePotentialScore = explorePotentialScore;
    this.enemyThreatScore = enemyThreatScore;
    this.killScore = killScore;
  }

  public double score() {
    double result = DoubleStream.of(goHomeScore, localMoveScore, explorePotentialScore).max().getAsDouble();
    return result
        + enemyThreatScore
        + 0.10 * killScore;
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("#.000");
    String vectorFormat = String.format(
        "Vector{home=%s, lcl=%s, exp=%s, ene=%s, kill=%s}[T=%s]",
        df.format(goHomeScore),
        df.format(localMoveScore),
        df.format(explorePotentialScore),
        df.format(enemyThreatScore),
        df.format(killScore),
        df.format(score()));
    return vectorFormat;
  }
}

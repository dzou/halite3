package shipagent;

import java.text.DecimalFormat;

public class DecisionVector {

  public final double goHomeScore;

  public final double exploreScore;

  public final double enemyThreatScore;

  public final double killScore;

  public DecisionVector(double goHomeScore, double exploreScore, double enemyThreatScore, double killScore) {
    this.goHomeScore = goHomeScore;
    this.exploreScore = exploreScore;
    this.enemyThreatScore = enemyThreatScore;
    this.killScore = killScore;
  }

  public double score() {
    if (enemyThreatScore < 0) {
      return enemyThreatScore;
    } else {
      return Math.max(goHomeScore, exploreScore) + 0.10 * killScore;
    }
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("#.000");
    String vectorFormat = String.format(
        "Vector{home=%s, exp=%s, ene=%s, kil=%s}[T=%s]",
        df.format(goHomeScore),
        df.format(exploreScore),
        df.format(enemyThreatScore),
        df.format(killScore),
        df.format(score()));
    return vectorFormat;
  }
}

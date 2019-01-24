package shipagent;

import tiles.TileScoreEntry;

import java.text.DecimalFormat;
import java.util.stream.DoubleStream;

public class DecisionVector {

  public final double goHomeScore;
  public final TileScoreEntry tileScoreEntry;
  public final double enemyThreatScore;
  public final double killScore;
  public final double focusScore;

  public DecisionVector(
      double goHomeScore,
      TileScoreEntry scoreEntry,
      double enemyThreatScore,
      double killScore,
      double focusScore) {

    this.goHomeScore = goHomeScore;
    this.tileScoreEntry = scoreEntry;
    this.enemyThreatScore = enemyThreatScore;
    this.killScore = killScore;
    this.focusScore = focusScore;
  }

  public double tileScore() {
    return tileScoreEntry.score;
  }

  public double score() {
    double result =
        DoubleStream.of(goHomeScore, tileScoreEntry.score, focusScore).max().getAsDouble();

    return result
        + enemyThreatScore
        + killScore;
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("#.000");
    String vectorFormat = String.format(
        "Vector{home=%s, lcl=%s, ene=%s, kill=%s, fcs=%s}[T=%s]",
        df.format(goHomeScore),
        df.format(tileScoreEntry.score) + tileScoreEntry.position,
        df.format(enemyThreatScore),
        df.format(killScore),
        df.format(focusScore),
        df.format(score()));
    return vectorFormat;
  }
}

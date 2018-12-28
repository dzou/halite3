package shipagent;

import tiles.TileScoreEntry;
import tiles.ZoneScoreEntry;

import java.text.DecimalFormat;
import java.util.stream.DoubleStream;

public class DecisionVector {

  public final double goHomeScore;
  public final TileScoreEntry tileScoreEntry;
  public final ZoneScoreEntry zoneScoreEntry;
  public final double enemyThreatScore;
  public final double killScore;

  public DecisionVector(
      double goHomeScore,
      TileScoreEntry scoreEntry,
      ZoneScoreEntry zoneScoreEntry,
      double enemyThreatScore,
      double killScore) {

    this.goHomeScore = goHomeScore;
    this.tileScoreEntry = scoreEntry;
    this.zoneScoreEntry = zoneScoreEntry;
    this.enemyThreatScore = enemyThreatScore;
    this.killScore = killScore;
  }

  public double tileScore() {
    return tileScoreEntry.score;
  }

  public double score() {
    double result =
        DoubleStream.of(goHomeScore, tileScoreEntry.score, zoneScoreEntry.score).max().getAsDouble();

    return result
        + enemyThreatScore
        + 0.10 * killScore;
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("#.000");
    String vectorFormat = String.format(
        "Vector{home=%s, lcl=%s, zone=%s, ene=%s, kill=%s}[T=%s]",
        df.format(goHomeScore),
        df.format(tileScoreEntry.score) + tileScoreEntry.position,
        df.format(zoneScoreEntry.score) + zoneScoreEntry.zone,
        df.format(enemyThreatScore),
        df.format(killScore),
        df.format(score()));
    return vectorFormat;
  }
}

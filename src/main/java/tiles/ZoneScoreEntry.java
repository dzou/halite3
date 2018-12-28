package tiles;

import map.Zone;

public class ZoneScoreEntry {

  public final Zone zone;

  public final double score;

  public ZoneScoreEntry(Zone zone, double score) {
    this.zone = zone;
    this.score = score;
  }

  @Override
  public String toString() {
    return "ZoneScoreEntry{" +
        "zone=" + zone +
        ", score=" + score +
        '}';
  }
}

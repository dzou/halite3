package tiles;

import hlt.Position;

public class TileScoreEntry {

  public final Position position;

  public final double score;

  public TileScoreEntry(Position position, double score) {
    this.position = position;
    this.score = score;
  }

  @Override
  public String toString() {
    return "TileScoreEntry{" +
        "position=" + position +
        ", score=" + score +
        '}';
  }
}

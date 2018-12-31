package tiles;

import hlt.Position;

public class TileScoreEntry {

  public final Position position;

  public final int haliteOnTile;

  public final double score;

  public TileScoreEntry(Position position, int haliteOnTile, double score) {
    this.position = position;
    this.haliteOnTile = haliteOnTile;
    this.score = score;
  }

  @Override
  public String toString() {
    return "TileScoreEntry{" +
        "position=" + position +
        ", haliteOnTile=" + haliteOnTile +
        ", score=" + score +
        '}';
  }
}

package tiles;

import hlt.Position;

public class TileWalk {
  public final int haliteGain;

  public final Position endpoint;

  public TileWalk(int haliteGain, Position endpoint) {
    this.haliteGain = haliteGain;
    this.endpoint = endpoint;
  }

  @Override
  public String toString() {
    return "TileWalk{" +
        "haliteGain=" + haliteGain +
        ", endpoint=" + endpoint +
        '}';
  }
}

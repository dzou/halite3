package tiles;

import hlt.Position;

public class TileWalk {

  public final int haliteGain;
  public final Position endpoint;
  public final int haliteDiscount;

  public TileWalk(int haliteGain, Position endpoint, int haliteDiscount) {
    this.haliteGain = haliteGain;
    this.endpoint = endpoint;
    this.haliteDiscount = haliteDiscount;
  }

  @Override
  public String toString() {
    return endpoint.toString() + haliteGain;
  }
}

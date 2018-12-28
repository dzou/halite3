package map;

import hlt.Position;

import java.util.Objects;

public class Tile {
  public final Position tilePosition;
  public final int haliteOnTile;

  public Tile(Position tilePosition, int haliteOnTile) {
    this.tilePosition = tilePosition;
    this.haliteOnTile = haliteOnTile;
  }

  @Override
  public String toString() {
    return "T" + tilePosition + "=" + haliteOnTile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Tile tile = (Tile) o;
    return haliteOnTile == tile.haliteOnTile &&
        Objects.equals(tilePosition, tile.tilePosition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tilePosition, haliteOnTile);
  }
}

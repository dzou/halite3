package map;

public class ZoneGrid {

  private static int ZONE_SIZE = 4;

  private final Grid<Integer> haliteGrid;

  public final Grid<Zone> zones;

  public ZoneGrid(Grid<Integer> haliteGrid) {
    this.haliteGrid = haliteGrid;
    this.zones = new Grid<>(
        (haliteGrid.width + ZONE_SIZE - 1) / ZONE_SIZE,
        (haliteGrid.height + ZONE_SIZE - 1) / ZONE_SIZE,
        null);

    for (int y = 0; y < zones.height; y++) {
      for (int x = 0; x < zones.width; x++) {
        int cornerX = x * ZONE_SIZE;
        int cornerY = y * ZONE_SIZE;

        Zone zone = Zone.create(cornerX, cornerY, haliteGrid, ZONE_SIZE);

        this.zones.set(x, y, zone);
      }
    }
  }


  @Override
  public String toString() {
    return zones.toString();
  }
}

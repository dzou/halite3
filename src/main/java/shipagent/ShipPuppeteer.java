package shipagent;

import grid.DjikstraGrid;
import grid.Grid;
import hlt.Position;

/** The master puppeteer. This guy tells ships what to do. */
public class ShipPuppeteer {

  private final Grid<Integer> haliteGrid;
  private final Position myBase;

  private final DjikstraGrid gridToHome;

  public ShipPuppeteer(Grid<Integer> haliteGrid, Position myBase) {
    this.haliteGrid = haliteGrid;
    this.myBase = myBase;

    this.gridToHome = DjikstraGrid.create(haliteGrid, myBase, null);
  }



}

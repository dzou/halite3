package map;

import grid.CostGrid;
import grid.Grid;
import hlt.GameMap;
import hlt.Position;

public class GoalGenerator {

  private final CostGrid costGrid;
  private final Position home;

  public GoalGenerator(GameMap gameMap, Position home) {
    this(CostGrid.create(gameMap.toHaliteGrid(), home), home);
  }

  public GoalGenerator(CostGrid costGrid, Position home) {
    this.costGrid = costGrid;
    this.home = home;
  }

}

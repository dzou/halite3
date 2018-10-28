package map;

import grid.CostGrid;
import grid.Grid;
import hlt.GameMap;
import hlt.Position;

public class GoalGenerator {

  private final Grid resourceMap;
  private final CostGrid costGrid;
  private final Position home;

  public GoalGenerator(Grid resourceMap, CostGrid costGrid, Position home) {
    this.resourceMap = resourceMap;
    this.costGrid = costGrid;
    this.home = home;
  }



}

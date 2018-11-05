package ml;

import grid.DjikstraGrid;
import hlt.Constants;
import hlt.Position;
import hlt.Ship;
import map.Path;

public class FeatureExtractor {

  public final DjikstraGrid gridToHome;
  private final Position myBase;

  public FeatureExtractor(DjikstraGrid gridToHome, Position myBase) {
    this.gridToHome = gridToHome;
    this.myBase = myBase;
  }

  public ExploreVector extractVector(Ship ship, Path explorePath) {
    ExploreVector vector = new ExploreVector();
    vector.setValue(ExploreVector.Feature.HALITE_CAPACITY_ON_SHIP, getHaliteCapacityOnShip(ship));
    vector.setValue(ExploreVector.Feature.MAX_HALITE_COLLECT, maxHaliteToCollect(ship));
    vector.setValue(ExploreVector.Feature.EXPLORE_DIST_TO_HOME, distFromExploreToHome(explorePath));
    vector.setValue(ExploreVector.Feature.PROJECTED_HALITE_TO_HOME, projectedHaliteToHome(ship));
    vector.setValue(ExploreVector.Feature.PROJECTED_HALITE_THRU_EXPLORE_TO_HOME, projectedHaliteThruExploreToHome(ship, explorePath));
    return vector;
  }

  private int maxHaliteToCollect(Ship ship) {
    return Math.min(
        Constants.MAX_HALITE - ship.halite,
        gridToHome.haliteGrid.get(ship.position.x, ship.position.y) / 4);
  }

  private int getDistanceFromHome(Ship ship) {
    return gridToHome.haliteGrid.distance(myBase, ship.position);
  }

  private double getCostFromHome(Ship ship) {
    return (gridToHome.costCache.get(ship.position.x, ship.position.y) + gridToHome.haliteGrid.get(ship.position.x, ship.position.y)) * 0.1;
  }

  private int getHaliteOnTile(Ship ship) {
    return gridToHome.haliteGrid.get(ship.position.x, ship.position.y);
  }

  private int getHaliteCapacityOnShip(Ship ship) {
    return Constants.MAX_HALITE - ship.halite;
  }

  private int getDistanceToExplore(Path explorePath) {
    return explorePath.path.size() - 1;
  }

  private double getCostToExplore(Path explorePath) {
    Position dest = explorePath.path.getLast();
    int cost = -gridToHome.haliteGrid.get(dest.x, dest.y);

    for (Position pos : explorePath.path) {
      cost += gridToHome.haliteGrid.get(pos.x, pos.y);
    }

    return cost * 0.1;
  }

  private int getHaliteOnExplore(Path explorePath) {
    Position dest = explorePath.path.getLast();
    return gridToHome.haliteGrid.get(dest.x, dest.y);
  }

  private int distFromExploreToHome(Path explorePath) {
    return gridToHome.haliteGrid.distance(explorePath.getDestination(), myBase);
  }

  private int projectedHaliteThruExploreToHome(Ship ship, Path explorePath) {
    Position dest = explorePath.path.getLast();
    int exploreBurn = -gridToHome.haliteGrid.get(dest.x, dest.y) / 10;
    for (Position p : explorePath.path) {
      exploreBurn += gridToHome.haliteGrid.get(p.x, p.y) / 10;
    }

    Path pathToHome = gridToHome.findPath(ship.position);
    int returnBurn = 0;
    for (Position p : pathToHome.path) {
      returnBurn += gridToHome.haliteGrid.get(p.x, p.y) / 10;
    }

    int haliteAtExploreTile = Math.min(
        Constants.MAX_HALITE,
        ship.halite - exploreBurn + gridToHome.haliteGrid.get(dest.x, dest.y));

    return haliteAtExploreTile - returnBurn;
  }

  private int projectedHaliteToHome(Ship ship) {
    Path path = gridToHome.findPath(ship.position);
    int burn = 0;
    for (Position p : path.path) {
      burn += gridToHome.haliteGrid.get(p.x, p.y) / 10;
    }

    return ship.halite - burn;
  }
}

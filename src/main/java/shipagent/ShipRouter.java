package shipagent;

import grid.DjikstraGrid;
import grid.Grid;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.GoalGenerator;
import map.Path;
import ml.ActionType;
import ml.ExploreModel;
import ml.ExploreVector;
import ml.FeatureExtractor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This guy tells ships what to do.
 */
public class ShipRouter {

  private final DjikstraGrid gridToHome;
  private final GoalGenerator goalGenerator;

  private final ExploreModel exploreModel;
  private final FeatureExtractor featureExtractor;

  public ShipRouter(Grid<Integer> haliteGrid, Position myBase, ExploreModel exploreModel) {
    this.gridToHome = DjikstraGrid.create(haliteGrid, myBase, null);
    this.goalGenerator = new GoalGenerator(this.gridToHome);
    this.exploreModel = exploreModel;
    this.featureExtractor = new FeatureExtractor(this.gridToHome, myBase);
  }

  // TODO: Fix how some ships might flip flop back and forth between 2 goals.
  public Map<Ship, Decision> routeShips(Collection<Ship> ships) {
    HashSet<Position> bestSquares = new HashSet<>(goalGenerator.getBestPositions(ships.size()));
    HashMap<Ship, Decision> decisionMap = new HashMap<>();

    for (Ship ship : ships) {
      Position exploreChoice =
          gridToHome.haliteGrid
              .findClosestPosition(ship.position, bestSquares)
              .orElse(ship.position);

      DjikstraGrid gridToShip = DjikstraGrid.create(this.gridToHome.haliteGrid, ship.position, exploreChoice);
      Path pathToExplore = gridToShip.findPath(exploreChoice);

      ExploreVector vector = featureExtractor.extractVector(ship, pathToExplore);

      ActionType actionType;
      if (gridToHome.haliteGrid.get(ship.position.x, ship.position.y) / 10 > ship.halite) {
        actionType = ActionType.STAY;
        Log.log("Forced stay. ");
      } else {
        actionType = exploreModel.sampleAction(vector);
      }

      if (actionType == ActionType.STAY) {
        Path path = new Path();
        path.push(ship.position);
        decisionMap.put(ship, new Decision(ActionType.STAY, vector, path));
        Log.log(ship.id + " - " + ship.position + " STAY");
      } else if (actionType == ActionType.EXPLORE) {
        decisionMap.put(ship, new Decision(ActionType.EXPLORE, vector, pathToExplore));
        bestSquares.remove(pathToExplore.getDestination());
        Log.log(ship.id + " - " + ship.position + " EXPLORE " + pathToExplore.getDestination());
      } else if (actionType == ActionType.RETURN) {
        decisionMap.put(ship, new Decision(ActionType.RETURN, vector, gridToHome.findPath(ship.position).reversed()));
        Log.log(ship.id + " - " + ship.position + " RETURN");
      }
    }

    return decisionMap;
  }

  private static void debugDecisions(Collection<Decision> decisions) {
    decisions.stream().forEach(c -> System.out.println(c));
  }
}

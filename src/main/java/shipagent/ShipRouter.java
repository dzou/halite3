package shipagent;

import com.google.common.collect.ImmutableList;
import grid.DjikstraGrid;
import grid.Grid;
import hlt.Constants;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.GoalGenerator;
import map.Path;

import java.util.*;

/** This guy tells ships what to do. */
public class ShipRouter {

  private static final int MAX_HALITE_CAPACITY = 1000;

  private static final int AVG_TURNS_ON_NODE = 3;

  private static final double PERCENT_MINED_FROM_NODE = 0.6;

  private final DjikstraGrid gridToHome;
  private final GoalGenerator goalGenerator;

  public ShipRouter(Grid<Integer> haliteGrid, Position myBase) {
    this.gridToHome = DjikstraGrid.create(haliteGrid, myBase, null);
    this.goalGenerator = new GoalGenerator(this.gridToHome);
  }

  public Map<Ship, GatherDecision> routeShips(Collection<Ship> ships) {
    HashSet<Position> bestSquares = new HashSet<>(goalGenerator.getBestPositions(ships.size() * 2));
    HashMap<Ship, GatherDecision> decisionMap = new HashMap<>();

    for (Ship ship : ships) {
      ImmutableList<GatherDecision> decisions = getDecisions(ship, bestSquares);

      for (GatherDecision decision : decisions) {
        Log.log(decision.toString());
      }

      GatherDecision decision = decisions.stream()
          .max(Comparator.comparingDouble(g -> g.decisionScore))
          .get();

      decisionMap.put(ship, decision);
      bestSquares.remove(decision.destination);
    }

    return decisionMap;
  }

  public ImmutableList<GatherDecision> getDecisions(Ship ship, HashSet<Position> goals) {
    ImmutableList.Builder<GatherDecision> result = ImmutableList.builder();
    result.add(stayScore(ship));
    result.add(goHomeScore(ship));


    Optional<Position> exploreOption = gridToHome.haliteGrid.findClosestPosition(ship.position, goals);
    if (exploreOption.isPresent()) {
      result.add(exploreScore(ship, exploreOption.get()));
    }

    return result.build();
  }

  private GatherDecision exploreScore(Ship ship, Position exploreDest) {
    DjikstraGrid exploreGrid = DjikstraGrid.create(this.gridToHome.haliteGrid, ship.position, exploreDest);

    Path pathToHome = gridToHome.findPath(exploreDest).reversed();
    Path pathToDest = exploreGrid.findPath(exploreDest);

    double burnShipToDest = 0.1 * exploreGrid.costCache.get(exploreDest.x, exploreDest.y);
    double burnDestToHome = 0.1 * gridToHome.costCache.get(exploreDest.x, exploreDest.y);

    double gain = Math.min(
        MAX_HALITE_CAPACITY - ship.halite,
        PERCENT_MINED_FROM_NODE * exploreGrid.haliteGrid.get(exploreDest.x, exploreDest.y) - burnShipToDest);

    int stepsTaken = pathToHome.path.size() + pathToDest.path.size() + AVG_TURNS_ON_NODE;

    double score = 0;
    if (!ship.position.equals(exploreDest)) {
      score = Math.max(0, 1.0 * (ship.halite + gain - burnDestToHome) / stepsTaken);
    }

    return new GatherDecision(GatherDecision.Type.EXPLORE, exploreDest, pathToDest, score);
  }

  private GatherDecision stayScore(Ship ship) {
    Path pathToHome = gridToHome.findPath(ship.position).reversed();
    pathToHome.push(ship.position);

    double gain = Math.min(
        MAX_HALITE_CAPACITY - ship.halite,
        PERCENT_MINED_FROM_NODE * gridToHome.haliteGrid.get(ship.position.x, ship.position.y));

    double estimatedBurn =
        0.1 * gridToHome.costCache.get(ship.position.x, ship.position.y)
        + 0.1 * (gridToHome.haliteGrid.get(ship.position.x, ship.position.y) - gain);

    double score = (pathToHome.path.size() == 0)
        ? 0
        : Math.max(0, (1.0 * ship.halite + gain - estimatedBurn) / (pathToHome.path.size() + AVG_TURNS_ON_NODE));

    return new GatherDecision(GatherDecision.Type.STAY, ship.position, pathToHome, score);
  }

  private GatherDecision goHomeScore(Ship ship) {
    Path pathToHome = gridToHome.findPath(ship.position).reversed();

    double estimatedBurn =
        0.1 * gridToHome.costCache.get(ship.position.x, ship.position.y)
        + 0.1 * gridToHome.haliteGrid.get(ship.position.x, ship.position.y);

    double score = 0;
    if (pathToHome.path.size() > 0) {
      score = Math.max(0, 1.0 * (ship.halite - estimatedBurn) / (1 + pathToHome.path.size()));
    }

    return new GatherDecision(GatherDecision.Type.RETURN, gridToHome.origin, pathToHome, score);
  }

  private static void debugDecisions(Collection<GatherDecision> decisions) {
    decisions.stream().forEach(c -> System.out.println(c));
  }
}

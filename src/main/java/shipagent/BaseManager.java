package shipagent;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;
import map.DjikstraGrid;
import map.Grid;

import java.util.*;

public class BaseManager {

  private final Map<PlayerId, Set<Position>> allBases;

  private final Grid<Integer> haliteGrid;

  public BaseManager(Map<PlayerId, Set<Position>> bases, Grid<Integer> haliteGrid) {
    this.allBases = bases;
    this.haliteGrid = haliteGrid;
  }

  public Set<Position> findGoHomeDirections(Ship ship) {
    HashSet<Position> goHomeDirections = new HashSet<>();

    Position closestBase = allBases.get(ship.owner)
        .stream()
        .min(Comparator.comparingInt(base -> haliteGrid.distance(base, ship.position)))
        .get();


    int dx = DjikstraGrid.getAxisDirection(ship.position.x, closestBase.x, haliteGrid.width);
    int dy = DjikstraGrid.getAxisDirection(ship.position.y, closestBase.y, haliteGrid.height);

    if (dx < 0) {
      goHomeDirections.add(ship.position.directionalOffset(Direction.WEST));
    } else if (dx > 0) {
      goHomeDirections.add(ship.position.directionalOffset(Direction.EAST));
    }

    if (dy < 0) {
      goHomeDirections.add(ship.position.directionalOffset(Direction.NORTH));
    } else if (dy > 0) {
      goHomeDirections.add(ship.position.directionalOffset(Direction.SOUTH));
    }

    return goHomeDirections;
  }

}

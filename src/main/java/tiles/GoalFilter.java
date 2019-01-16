package tiles;

import com.google.common.collect.ImmutableSet;
import hlt.Direction;
import hlt.Log;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import shipagent.MapOracle;

import java.util.*;

public class GoalFilter {

  private final Grid<Integer> haliteGrid;

  public GoalFilter(Grid<Integer> haliteGrid) {
    this.haliteGrid = haliteGrid;
  }

  Set<Position> getLocalMoves(Position origin, Direction d, int range) {
    ImmutableSet.Builder<Position> localPositions = ImmutableSet.builder();

    int xStart = (d == Direction.EAST) ? 1 : -range;
    int xEnd = (d == Direction.WEST) ? -1 : range;

    int yStart = (d == Direction.SOUTH) ? 1 : -range;
    int yEnd = (d == Direction.NORTH) ? -1 : range;

    for (int y = yStart; y <= yEnd; y++) {
      for (int x = Math.max(xStart, -range + Math.abs(y));
           x <= Math.min(xEnd, range - Math.abs(y));
           x++) {
        Position curr = Position.at(origin.x + x, origin.y + y);

        localPositions.add(haliteGrid.normalize(curr));
      }
    }

    return localPositions.build();
  }

}

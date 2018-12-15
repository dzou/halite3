package map;

import com.google.common.collect.ArrayListMultimap;
import hlt.Position;
import hlt.Ship;

import java.util.Collection;

/**
 * Provides information for how to trap ships on certain cells.
 */
public class TriangulationGrid {

  private final int coverRange;

  public final ArrayListMultimap<Position, Ship> northCovers;
  public final ArrayListMultimap<Position, Ship> southCovers;
  public final ArrayListMultimap<Position, Ship> eastCovers;
  public final ArrayListMultimap<Position, Ship> westCovers;

  public TriangulationGrid(Collection<Ship> shipCovers, int coverRange) {
    this.coverRange = coverRange;

    this.northCovers = ArrayListMultimap.create();
    this.southCovers = ArrayListMultimap.create();
    this.eastCovers = ArrayListMultimap.create();
    this.westCovers = ArrayListMultimap.create();

    for (Ship s : shipCovers) {
      Position origin = s.position;

      for (int dy = -coverRange; dy <= coverRange; dy++) {
        for (int dx = -coverRange; dx <= coverRange; dx++) {

          int posX = origin.x + dx;
          int posY = origin.y + dy;

          if (Math.abs(dy) + 1 >= Math.abs(dx)) {
            if (dy <= 0) {
              northCovers.put(Position.at(posX, posY), s);
            }
            if (dy >= 0) {
              southCovers.put(Position.at(posX, posY), s);
            }
          }

          if (Math.abs(dx) + 1 >= Math.abs(dy)) {
            if (dx >= 0) {
              eastCovers.put(Position.at(posX, posY), s);
            }
            if (dx <= 0) {
              westCovers.put(Position.at(posX, posY), s);
            }
          }
        }
      }
    }
  }

  public boolean isPositionCovered(Position pos) {
    return northCovers.get(pos).size() >= 1
        && southCovers.get(pos).size() >= 1
        && eastCovers.get(pos).size() >= 1
        && westCovers.get(pos).size() >= 1;
  }

  public String debugString(int size) {
    StringBuilder builder = new StringBuilder();
    builder.append("north:\n" + coverToString(northCovers, size) + "\n");
    builder.append("south:\n" + coverToString(southCovers, size) + "\n");
    builder.append("west:\n" + coverToString(westCovers, size) + "\n");
    builder.append("east:\n" + coverToString(eastCovers, size) + "\n");
    return builder.toString();
  }

  private static String coverToString(ArrayListMultimap<Position, Ship> multimap, int size) {
    Grid<Integer> covers = new Grid<>(size, size, 0);
    for (Position pos : multimap.keySet()) {
      covers.set(pos.x, pos.y, multimap.get(pos).size());
    }
    return covers.toString();
  }

}

package map;

import hlt.Direction;
import hlt.Position;

import java.util.ArrayDeque;

public class Path {
  public final ArrayDeque<Position> path = new ArrayDeque<>();

  public void push(Position position) {
    path.push(position);
  }

  @Override
  public String toString() {
    return path.toString();
  }
}

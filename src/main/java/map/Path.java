package map;

import hlt.Direction;
import hlt.Position;

import java.util.ArrayDeque;
import java.util.Iterator;

public class Path {
  public final ArrayDeque<Position> path = new ArrayDeque<>();

  public void push(Position position) {
    path.push(position);
  }

  public Position pop() {
    return path.pop();
  }

  public Path reversed() {
    Path result = new Path();
    for (Position pos : path) {
      result.push(pos);
    }
    return result;
  }

  @Override
  public String toString() {
    return path.toString();
  }
}

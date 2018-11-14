package matching;

import hlt.Position;
import java.util.HashSet;
import java.util.List;

public class BipartiteGraph {
  HashSet<Vertex> ships;
  HashSet<Vertex> destinations;

  public void addShip(Position shipPosition, List<Position> neighbors) {
    Vertex shipVertex = new Vertex(shipPosition,0);

    for (Position neighbor : neighbors) {
      Vertex destVertex = new Vertex(neighbor, 0);


      destinations.add(destVertex);
    }
  }

}

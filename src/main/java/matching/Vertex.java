package matching;

import hlt.Position;
import java.util.ArrayList;

public class Vertex {

  public final Position position;
  public double label;
  public ArrayList<Edge> edges;

  public Vertex(Position position, double label) {
    this.position = position;
    this.label = label;
    this.edges = new ArrayList<>();
  }

  public void addNeighbor(Vertex dest, double weight) {
    edges.add(new Edge(dest, weight));
  }
}

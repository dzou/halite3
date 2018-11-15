package matching;

import hlt.Position;

import java.text.DecimalFormat;
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

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("####0.#####");
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("VERTEX: " + position + " - " + df.format(label) + "\n");
    for (Edge edge : edges) {
      stringBuilder.append(edge + "\n");
    }
    return stringBuilder.toString();
  }
}

package matching;

import java.text.DecimalFormat;

public class Edge {
  public final Vertex destination;
  public final double weight;

  public Edge(Vertex destination, double weight) {
    this.destination = destination;
    this.weight = weight;
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0.0####");
    return "Edge to: " + destination.position + " - " + df.format(weight);
  }
}

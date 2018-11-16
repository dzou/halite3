package matching;

import java.text.DecimalFormat;

public class Edge {
  public final Vertex start;
  public final Vertex destination;
  public final double weight;

  public Edge(Vertex start, Vertex destination, double weight) {
    this.start = start;
    this.destination = destination;
    this.weight = weight;
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0.0####");
    return start.position + " to: " + destination.position + " - " + df.format(weight);
  }
}

package matching;

import hlt.Position;
import hlt.Ship;
import java.text.DecimalFormat;
import java.util.Objects;

public class Edge {
  public final Vertex start;
  public final Vertex destination;
  public final double weight;

  public Edge(Vertex start, Vertex destination, double weight) {
    this.start = start;
    this.destination = destination;
    this.weight = weight;
  }

  public Edge flipped() {
    return new Edge(destination, start, weight);
  }

  public static Edge manualEdge(Ship start, Position destination) {
    Vertex startV = new Vertex(start.position, 0);
    Vertex destV = new Vertex(destination, 0);
    return new Edge(startV, destV, 0);
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0.0####");
    return start.position + " to: " + destination.position + " - " + df.format(weight);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Edge edge = (Edge) o;

    return (Objects.equals(start, edge.start) && Objects.equals(destination, edge.destination))
        || (Objects.equals(start, edge.destination) && Objects.equals(destination, edge.start));
  }

  @Override
  public int hashCode() {
    return Objects.hash(start) ^ Objects.hash(destination);
  }
}

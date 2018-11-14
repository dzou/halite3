package matching;

public class Edge {
  public final Vertex destination;
  public final double weight;

  public Edge(Vertex destination, double weight) {
    this.destination = destination;
    this.weight = weight;
  }
}

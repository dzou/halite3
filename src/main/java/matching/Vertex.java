package matching;

import hlt.Position;
import hlt.Ship;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Vertex {

  public final Position position;

  public double label;

  public final Type type;

  public final ArrayList<Edge> edges;

  public final Optional<Ship> ship;

  public Vertex(Position position, double label, Type type, Ship ship) {
    this.position = position;
    this.label = label;
    this.edges = new ArrayList<>();
    this.type = type;
    this.ship = Optional.ofNullable(ship);
  }

  public void addNeighbor(Vertex dest, double weight) {
    edges.add(new Edge(this, dest, weight));
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("####0.##");
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("VERTEX: " + position + " (" + df.format(label) + ")\n");
    for (Edge edge : edges) {
      stringBuilder.append(edge + "\n");
    }
    return stringBuilder.toString();
  }

  enum Type {
    SHIP, DESTINATION;
  }
}

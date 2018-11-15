package matching;

import hlt.Position;
import shipagent.Decision;

import java.util.HashSet;
import java.util.List;

public class BipartiteGraph {
  private HashSet<Vertex> ships;
  private HashSet<Vertex> destinations;

  public BipartiteGraph() {
    this.ships = new HashSet<>();
    this.destinations = new HashSet<>();
  }

  public void addShip(Position shipPosition, List<Decision> neighbors) {
    Vertex shipVertex = new Vertex(shipPosition,0);

    for (Decision decision : neighbors) {
      shipVertex.label = Math.max(shipVertex.label, decision.score);

      Vertex destVertex = new Vertex(decision.destination, 0);
      shipVertex.addNeighbor(destVertex, decision.score);
      destVertex.addNeighbor(shipVertex, decision.score);

      destinations.add(destVertex);
    }

    ships.add(shipVertex);
  }


  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("SHIPS: \n");
    for (Vertex v : ships) {
      stringBuilder.append(v + "\n");
    }

    stringBuilder.append("DESTINATIONS: \n");
    for (Vertex v : destinations) {
      stringBuilder.append(v + "\n");
    }

    return stringBuilder.toString();
  }

}

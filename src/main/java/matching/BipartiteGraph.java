package matching;

import hlt.Position;
import hlt.Ship;
import java.util.Collection;
import java.util.HashMap;
import shipagent.Decision;

public class BipartiteGraph {
  HashMap<Position, Vertex> ships;
  HashMap<Position, Vertex> destinations;

  public BipartiteGraph() {
    this.ships = new HashMap<>();
    this.destinations = new HashMap<>();
  }

  public void addShip(Ship ship, Collection<Decision> neighbors) {
    Vertex shipVertex = new Vertex(ship.position,0);

    for (Decision decision : neighbors) {
      shipVertex.label = Math.max(shipVertex.label, decision.score);

      Vertex destVertex = destinations.get(decision.destination);
      if (destVertex == null) {
        destVertex = new Vertex(decision.destination, 0);
        destinations.put(decision.destination, destVertex);
      }

      shipVertex.addNeighbor(destVertex, decision.score);
      destVertex.addNeighbor(shipVertex, decision.score);
    }

    ships.put(ship.position, shipVertex);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("SHIPS: \n");
    for (Vertex v : ships.values()) {
      stringBuilder.append(v + "\n");
    }

    stringBuilder.append("DESTINATIONS: \n");
    for (Vertex v : destinations.values()) {
      stringBuilder.append(v + "\n");
    }

    return stringBuilder.toString();
  }

}

package matching;

import hlt.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class BipartiteGraph {

  HashSet<Vertex> sourceNodes;
  HashSet<Vertex> destNodes;

  private HashMap<Position, Vertex> sourcePositions;
  private HashMap<Position, Vertex> destPositions;

  public BipartiteGraph() {
    this.sourceNodes = new HashSet<>();
    this.destNodes = new HashSet<>();

    this.sourcePositions = new HashMap<>();
    this.destPositions = new HashMap<>();
  }

  public void addNode(Position pos, Map<Position, Double> neighbors) {
    Vertex source = new Vertex(pos, -999999);

    for (Map.Entry<Position, Double> neighborEntry : neighbors.entrySet()) {
      if (!destPositions.containsKey(neighborEntry.getKey())) {
        Vertex newDest = new Vertex(neighborEntry.getKey(), 0);
        destNodes.add(newDest);
        destPositions.put(neighborEntry.getKey(), newDest);
      }

      Vertex destNode = destPositions.get(neighborEntry.getKey());
      source.addNeighbor(destNode, neighborEntry.getValue());
      if (neighborEntry.getValue() > source.label) {
        source.label = neighborEntry.getValue();
      }
    }

    sourceNodes.add(source);
    sourcePositions.put(pos, source);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("SOURCES: \n");
    for (Vertex v : sourceNodes) {
      stringBuilder.append(v + "\n");
    }

    stringBuilder.append("DESTINATIONS: \n");
    for (Vertex v : destNodes) {
      stringBuilder.append(v + "\n");
    }

    return stringBuilder.toString();
  }
}

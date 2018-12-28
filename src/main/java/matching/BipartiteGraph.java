package matching;

import hlt.Position;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class BipartiteGraph {

  HashSet<Vertex> sourceNodes;
  HashSet<Vertex> destNodes;

  public HashMap<Vertex, Integer> destinationCapacityMap;

  private HashMap<Position, Vertex> sourcePositions;
  private HashMap<Position, Vertex> destPositions;

  public BipartiteGraph() {
    this.sourceNodes = new HashSet<>();
    this.destNodes = new HashSet<>();
    this.destinationCapacityMap = new HashMap<>();

    this.sourcePositions = new HashMap<>();
    this.destPositions = new HashMap<>();
  }

  public Collection<Position> getDestinations() {
    return destPositions.keySet();
  }

  public void setCapacity(Position destPos, int capacity) {
    Vertex destVertex = destPositions.get(destPos);
    destinationCapacityMap.put(destVertex, capacity);
  }

  public void addSingleCapacityNode(Position pos, Map<Position, Double> neighbors) {
    addSource(pos);

    for (Map.Entry<Position, Double> entry : neighbors.entrySet()) {
      Position neighborPosition = entry.getKey();
      if (!destPositions.containsKey(neighborPosition)) {
        addDestination(neighborPosition, 1);
      }
      addEdge(pos, neighborPosition, entry.getValue());
    }
  }

  void addSource(Position pos) {
    Vertex source = new Vertex(pos, -999999);
    sourceNodes.add(source);
    sourcePositions.put(pos, source);
  }

  void addDestination(Position pos, int destinationCapacity) {
    Vertex dest = new Vertex(pos, 0);
    destNodes.add(dest);
    destPositions.put(pos, dest);
    destinationCapacityMap.put(dest, destinationCapacity);
  }

  void addEdge(Position sourcePosition, Position destPosition, double weight) {
    Vertex sourceNode = sourcePositions.get(sourcePosition);
    Vertex destNode = destPositions.get(destPosition);

    sourceNode.addNeighbor(destNode, weight);
    sourceNode.label = Math.max(sourceNode.label, weight);
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

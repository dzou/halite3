package matching;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import hlt.Position;
import hlt.Ship;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import matching.Vertex.Type;
import shipagent.Decision;

/**
 * Implementation of the Hungarian matching algorithm
 *
 * reference:
 * http://www.maths.qmul.ac.uk/~bill/MAS210/ch5.pdf
 * http://www.maths.qmul.ac.uk/~bill/MAS210/ch6.pdf
 */
public class BipartiteGraph {
  HashMap<Position, Vertex> ships;
  HashMap<Position, Vertex> destinations;

  HashSet<Vertex> assignedVertices;
  HashSet<Vertex> assignedDestinations;

  HashSet<Edge> assignmentEdges;

  public BipartiteGraph() {
    this.ships = new HashMap<>();
    this.destinations = new HashMap<>();

    this.assignedVertices = new HashSet<>();
    this.assignedDestinations = new HashSet<>();
    this.assignmentEdges = new HashSet<>();
  }

  public void matchShipsToDestinations() {
    while (assignmentEdges.size() < ships.size()) {
      ImmutableList<Vertex> unassignedVertices = ships.values().stream()
          .filter(v -> !assignedVertices.contains(v))
          .collect(ImmutableList.toImmutableList());

      for (Vertex unassignedShip : unassignedVertices) {

      }


    }
  }

  public void relabelGraph() {
    ForestPartition forestPartition = findAlternatingForestScope();


  }

  public ArrayList<Edge> findAugmentingPath(Vertex ship) {
    ArrayDeque<Vertex> stack = new ArrayDeque<>();
    stack.push(ship);

    ArrayList<Edge> path = new ArrayList<>();

    while(!stack.isEmpty()) {
      Vertex curr = stack.pop();

      if (curr.type == Type.DESTINATION && !assignedDestinations.contains(curr)) {

      }

      for (Edge e : curr.edges) {
        if (curr.type == Type.SHIP && assignmentEdges.contains(e)
            || curr.type == Type.DESTINATION && !assignmentEdges.contains(e)) {
          continue;
        }

        stack.push(e.destination);
      }
    }

  }

  public ForestPartition findAlternatingForestScope() {
    ForestPartition forestPartition = new ForestPartition();

    for (Vertex shipPosition : ships.values()) {
      if (assignedVertices.contains(shipPosition)) {
        continue;
      }

      ArrayDeque<Vertex> stack = new ArrayDeque<>();
      stack.push(shipPosition);

      while (!stack.isEmpty()) {
        Vertex current = stack.pop();
        if (forestPartition.ships.contains(current) || forestPartition.dests.contains(current)) {
          continue;
        }

        if (current.type == Type.SHIP) {
          forestPartition.ships.add(current);
        } else {
          forestPartition.dests.add(current);
        }

        for (Edge e : current.edges) {
          if (current.type == Type.SHIP && assignmentEdges.contains(e)
              || current.type == Type.DESTINATION && !assignmentEdges.contains(e)) {
            continue;
          }
          stack.push(e.destination);
        }
      }
    }

    return forestPartition;
  }

  private static boolean isIncludedInEqualitySubgraph(Edge e) {
    return e.weight >= e.start.label + e.destination.label;
  }

  public void addShip(Ship ship, Collection<Decision> neighbors) {
    Vertex shipVertex = new Vertex(ship.position,0, Type.SHIP);

    for (Decision decision : neighbors) {
      shipVertex.label = Math.max(shipVertex.label, decision.score);

      Vertex destVertex = destinations.get(decision.destination);
      if (destVertex == null) {
        destVertex = new Vertex(decision.destination, 0, Type.DESTINATION);
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

  static class ForestPartition {
    private HashSet<Vertex> ships = new HashSet<>();
    private HashSet<Vertex> dests = new HashSet<>();

    @Override
    public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Forest Partition: \n");

      stringBuilder.append("Ships: ");
      for (Vertex v : ships) {
        stringBuilder.append(v.position);
      }
      stringBuilder.append("\n");

      stringBuilder.append("Dests: ");
      for (Vertex v : dests) {
        stringBuilder.append(v.position);
      }
      stringBuilder.append("\n");

      return stringBuilder.toString();
    }
  }
}

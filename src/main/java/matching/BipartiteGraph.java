package matching;

import com.google.common.collect.ImmutableList;
import hlt.Position;
import hlt.Ship;

import java.util.*;

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
  HashMap<Position, Vertex> sourceNodes;
  HashMap<Position, Vertex> destNodes;

  HashSet<Vertex> assignedSources;
  HashMap<Vertex, Edge> assignedDestinations;

  public BipartiteGraph() {
    this.sourceNodes = new HashMap<>();
    this.destNodes = new HashMap<>();

    this.assignedSources = new HashSet<>();
    this.assignedDestinations = new HashMap<>();
  }

  // public HashSet<Edge> matchShipsToDestinations() {
  //   while (assignmentEdges.size() < sourceNodes.size()) {
  //     List<Vertex> unassignedShips = sourceNodes.values()
  //         .stream()
  //         .filter(s -> !assignedVertices.contains(s))
  //         .collect(ImmutableList.toImmutableList());
  //
  //     for (Vertex s : unassignedShips) {
  //       ArrayList<Edge> path = findAugmentingPath(s);
  //       for (Edge e : path) {
  //         if (assignmentEdges.contains(e)) {
  //           assignmentEdges.remove(e);
  //         } else {
  //           assignedVertices.add(e.start);
  //           assignedDestinations.add(e.destination);
  //           assignmentEdges.add(e);
  //         }
  //       }
  //     }
  //
  //     relabelGraph();
  //   }
  //
  //   return assignmentEdges;
  // }
  //
  void relabelGraph() {
    //TODO: this is wrong by the way, u have to actually compute the forest rooted at the unassigned nodes.
  	Set<Vertex> unlabeledNodes = sourceNodes.values().stream()
        .filter(v -> !assignedSources.contains(v))
        .collect(Collectors.toSet());

  	// edge to relabel graph with
  	Edge minEdge = unlabeledNodes.stream()
        .flatMap(v -> v.edges.stream())
				.filter(e -> !isIncludedInEqualitySubgraph(e))
        .min(Comparator.comparingDouble(e -> e.start.label + e.destination.label - e.weight))
        .get();



    double globalBestAlpha = 99999999;

    for (Vertex shipVertex : forestPartition.ships) {
      for (Edge e : shipVertex.edges) {
        if (!isIncludedInEqualitySubgraph(e)) {
          double alpha = e.start.label + e.destination.label - e.weight;
          if (alpha < globalBestAlpha) {
            globalBestAlpha = alpha;
          }
        }
      }
    }

    for (Vertex v : forestPartition.ships) {
      v.label -= globalBestAlpha;
    }

    for (Vertex v : forestPartition.dests) {
      v.label += globalBestAlpha;
    }
  }

  ArrayList<Edge> findAugmentingPath(Vertex source) {
    HashMap<Vertex, Edge> prevMap = new HashMap<>();

    ArrayDeque<Vertex> stack = new ArrayDeque<>();
    stack.push(source);

    ArrayList<Edge> path = new ArrayList<>();

    while (!stack.isEmpty()) {
      Vertex curr = stack.pop();

      if (destNodes.containsKey(curr.position) && !assignedDestinations.containsKey(curr)) {
        Edge originEdge = prevMap.get(curr);
        while (originEdge != null) {
          path.add(originEdge);
          originEdge = prevMap.get(originEdge.start);
        }
        break;
      }

      if (sourceNodes.containsKey(curr)) {
        Set<Edge> edgesInEqualitySubgraph = curr.edges
            .stream()
            .filter(e ->
                !prevMap.containsKey(e.destination)
                && isIncludedInEqualitySubgraph(e)
                && !assignedDestinations.containsKey(e.destination))
            .collect(Collectors.toSet());

        for (Edge e : edgesInEqualitySubgraph) {
          stack.push(e.destination);
          prevMap.put(e.destination, e);
        }
      } else {
        Edge assignedEdge = assignedDestinations.get(curr);
        stack.push(assignedEdge.destination);
        prevMap.put(assignedEdge.destination, assignedEdge);
      }
    }

    return path;
  }

  private static boolean isIncludedInEqualitySubgraph(Edge e) {
    return e.weight + 0.00001 >= e.start.label + e.destination.label;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("SOURCES: \n");
    for (Vertex v : sourceNodes.values()) {
      stringBuilder.append(v + "\n");
    }

    stringBuilder.append("DESTINATIONS: \n");
    for (Vertex v : destNodes.values()) {
      stringBuilder.append(v + "\n");
    }

    return stringBuilder.toString();
  }

//  ForestPartition findAlternatingForestScope() {
//    ForestPartition forestPartition = new ForestPartition();
//
//    for (Vertex shipPosition : sourceNodes.values()) {
//      if (assignedVertices.contains(shipPosition)) {
//        continue;
//      }
//
//      ArrayDeque<Vertex> stack = new ArrayDeque<>();
//      stack.push(shipPosition);
//
//      while (!stack.isEmpty()) {
//        Vertex current = stack.pop();
//        if (forestPartition.ships.contains(current) || forestPartition.dests.contains(current)) {
//          continue;
//        }
//
//        if (current.type == Type.SHIP) {
//          forestPartition.ships.add(current);
//        } else {
//          forestPartition.dests.add(current);
//        }
//
//        for (Edge e : current.edges) {
//          if (current.type == Type.SHIP && assignmentEdges.contains(e)) {
//            continue;
//          }
//
//          if (current.type == Type.DESTINATION && !assignmentEdges.contains(e)) {
//            continue;
//          }
//
//          if (!isIncludedInEqualitySubgraph(e)) {
//            continue;
//          }
//
//          stack.push(e.destination);
//        }
//      }
//    }
//
//    return forestPartition;
//  }
//
//
//
//
//
//  static class ForestPartition {
//    private HashSet<Vertex> ships = new HashSet<>();
//    private HashSet<Vertex> dests = new HashSet<>();
//
//    @Override
//    public String toString() {
//      StringBuilder stringBuilder = new StringBuilder();
//      stringBuilder.append("Forest Partition: \n");
//
//      stringBuilder.append("Ships: ");
//      for (Vertex v : ships) {
//        stringBuilder.append(v.position);
//      }
//      stringBuilder.append("\n");
//
//      stringBuilder.append("Dests: ");
//      for (Vertex v : dests) {
//        stringBuilder.append(v.position);
//      }
//      stringBuilder.append("\n");
//
//      return stringBuilder.toString();
//    }
//  }
}

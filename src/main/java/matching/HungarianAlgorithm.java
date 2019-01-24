package matching;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Position;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Hungarian matching algorithm.
 * This is an extended version of the algorithm in which you may specify a max number of
 * matchings allowed for a given destination.
 *
 * <p>
 * references:
 * http://www.maths.qmul.ac.uk/~bill/MAS210/ch5.pdf
 * http://www.maths.qmul.ac.uk/~bill/MAS210/ch6.pdf
 */
public class HungarianAlgorithm {

  private static final double EPSILON = 0.10;

  BipartiteGraph graph;
  HashSet<Vertex> assignedSources;
  HashMultimap<Vertex, Edge> assignedDestinations;

  public HungarianAlgorithm(BipartiteGraph graph) {
    this.graph = graph;
    this.assignedSources = new HashSet<>();
    this.assignedDestinations = HashMultimap.create();
  }

  public Set<Position> getTappedDestinations() {
    return graph.destNodes.stream()
        .filter(dest -> assignedDestinations.get(dest).size() > 0)
        .map(v -> v.position)
        .collect(ImmutableSet.toImmutableSet());
  }

  public Map<Position, Position> processMatches() {
    while (assignedDestinations.size() < graph.sourceNodes.size()) {
      List<Vertex> unassignedShips = graph.sourceNodes
          .stream()
          .filter(s -> !assignedSources.contains(s))
          .collect(Collectors.toList());

      for (Vertex s : unassignedShips) {
        ArrayList<Edge> path = findAugmentingPath(s);
        for (Edge e : path) {
          if (assignedDestinations.containsKey(e.start)) {
            assignedDestinations.remove(e.start, e);
          } else {
            assignedSources.add(e.start);
            assignedDestinations.put(e.destination, e);
          }
        }
      }

      relabelGraph();
    }

    return assignedDestinations.values()
        .stream()
        .collect(ImmutableMap.toImmutableMap(
            e -> e.start.position,
            e -> e.destination.position));
  }

  void relabelGraph() {
    ForestPartition forestPartition = findAlternatingForestScope();
    if (forestPartition.sources.isEmpty()) {
      return;
    } else if (forestPartition.dests.isEmpty()) {
      throw new RuntimeException("Bug : created a bipartite graph for which no perfect matching exists . . . ");
    }

    double alpha = forestPartition.sources
        .stream()
        .flatMap(v -> v.edges.stream())
        .filter(e -> !isIncludedInEqualitySubgraph(e))
        .mapToDouble(e -> e.start.label + e.destination.label - e.weight)
        .min()
        .getAsDouble() + EPSILON;

    for (Vertex v : forestPartition.sources) {
      v.label -= alpha;
    }

    for (Vertex v : forestPartition.dests) {
      v.label += alpha;
    }
  }

  ArrayList<Edge> findAugmentingPath(Vertex source) {
    HashMap<Vertex, Edge> prevMap = new HashMap<>();

    ArrayDeque<Vertex> stack = new ArrayDeque<>();
    stack.push(source);

    ArrayList<Edge> path = new ArrayList<>();

    while (!stack.isEmpty()) {
      Vertex curr = stack.pop();

      if (graph.destNodes.contains(curr)
          && assignedDestinations.get(curr).size() < graph.destinationCapacityMap.get(curr)) {

        Edge originEdge = prevMap.get(curr);
        while (originEdge != null) {
          path.add(originEdge);
          originEdge = prevMap.get(originEdge.start);
        }
        break;
      }

      if (graph.sourceNodes.contains(curr)) {
        List<Edge> edgesInEqualitySubgraph = curr.edges
            .stream()
            .filter(e -> !prevMap.containsKey(e.destination) && isIncludedInEqualitySubgraph(e))
            .collect(Collectors.toList());

        for (Edge e : edgesInEqualitySubgraph) {
          stack.push(e.destination);
          prevMap.put(e.destination, e);
        }
      } else {
        for (Edge e : assignedDestinations.get(curr)) {
          stack.push(e.start);
          prevMap.put(e.start, e.flipped());
        }
      }
    }

    return path;
  }

  private static boolean isIncludedInEqualitySubgraph(Edge e) {
    return e.weight + EPSILON >= e.start.label + e.destination.label;
  }

  ForestPartition findAlternatingForestScope() {
    ForestPartition forestPartition = new ForestPartition();

    ArrayDeque<Vertex> stack = graph.sourceNodes
        .stream()
        .filter(vertex -> !assignedSources.contains(vertex))
        .collect(Collectors.toCollection(ArrayDeque::new));

    while (!stack.isEmpty()) {
      Vertex curr = stack.pop();

      if (forestPartition.sources.contains(curr) || forestPartition.dests.contains(curr)) {
        continue;
      }

      if (graph.sourceNodes.contains(curr)) {
        forestPartition.sources.add(curr);
        for (Edge e : curr.edges) {
          if (isIncludedInEqualitySubgraph(e)) {
            stack.push(e.destination);
          }
        }
      } else {
        forestPartition.dests.add(curr);
        for (Edge matchedEdge : assignedDestinations.get(curr)) {
          stack.push(matchedEdge.start);
        }
      }
    }

    return forestPartition;
  }

  static class ForestPartition {
    private HashSet<Vertex> sources = new HashSet<>();
    private HashSet<Vertex> dests = new HashSet<>();

    @Override
    public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Forest Partition: \n");

      stringBuilder.append("Sources: ");
      for (Vertex v : sources) {
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

package matching;

import com.google.common.collect.ImmutableMap;
import hlt.Position;
import org.junit.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class HungarianAlgorithmTest {

  @Test
  public void testSimpleMatch() {
    BipartiteGraph graph = new BipartiteGraph();

    graph.addSingleCapacityNode(
        Position.at(0, 0),
        ImmutableMap.of(
            Position.at(0, 1), 22.0,
            Position.at(-1, 0), 14.0,
            Position.at(1, 0), 13.4,
            Position.at(0, -1), 44.2));

    HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(graph);

    Map<Position, Position> assignments = hungarianAlgorithm.processMatches();

    assertThat(assignments).isEqualTo(
        ImmutableMap.of(Position.at(0, 0), Position.at(0, -1)));
  }

  @Test
  public void testComplexMatch() {
    BipartiteGraph graph = new BipartiteGraph();

    graph.addSingleCapacityNode(
        Position.at(0, 0),
        ImmutableMap.of(
            Position.at(0, 0), 0.0,
            Position.at(0, 1), 22.0,
            Position.at(-1, 0), 14.0,
            Position.at(1, 0), 13.4,
            Position.at(0, -1), 44.2));

    graph.addSingleCapacityNode(
        Position.at(0, -1),
        ImmutableMap.of(
            Position.at(0, -1), 44.2,
            Position.at(0, 0), 0.0,
            Position.at(0, -2), 14.0,
            Position.at(1, -1), 13.4,
            Position.at(-1, -1), 32.2));

    graph.addSingleCapacityNode(
        Position.at(1, 1),
        ImmutableMap.of(
            Position.at(0, -1), 44.2,
            Position.at(-1, -1), 36.0,
            Position.at(0, 1), 32.0,
            Position.at(2, 1), 0.4,
            Position.at(1, 1), 4.2));

    HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(graph);

    Map<Position, Position> assignments = hungarianAlgorithm.processMatches();
    System.out.println(assignments);

    assertThat(assignments).isEqualTo(
        ImmutableMap.of(
            Position.at(0, 0), Position.at(0, -1),
            Position.at(0, -1), Position.at(-1, -1),
            Position.at(1, 1), Position.at(0, 1)));
  }

  @Test
  public void testMultiMatch() {
    BipartiteGraph graph = new BipartiteGraph();

    graph.addSource(Position.at(0, 0));
    graph.addSource(Position.at(0, 1));
    graph.addSource(Position.at(0, 2));

    graph.addDestination(Position.at(1, 0), 3);
    graph.addDestination(Position.at(1, 1), 1);
    graph.addDestination(Position.at(1, 2), 1);

    graph.addEdge(Position.at(0, 0), Position.at(1, 0), 20.0);
    graph.addEdge(Position.at(0, 0), Position.at(1, 1), 10.0);
    graph.addEdge(Position.at(0, 0), Position.at(1, 2), 13.0);

    graph.addEdge(Position.at(0, 1), Position.at(1, 0), 20.0);
    graph.addEdge(Position.at(0, 1), Position.at(1, 1), 7.20);
    graph.addEdge(Position.at(0, 1), Position.at(1, 2), 4.0);

    graph.addEdge(Position.at(0, 2), Position.at(1, 0), 1.0);
    graph.addEdge(Position.at(0, 2), Position.at(1, 1), 14.20);
    graph.addEdge(Position.at(0, 2), Position.at(1, 2), 4.0);

    HungarianAlgorithm alg = new HungarianAlgorithm(graph);
    Map<Position, Position> matchings = alg.processMatches();

    System.out.println(matchings);

    assertThat(matchings).isEqualTo(
        ImmutableMap.of(
            Position.at(0, 0), Position.at(1, 0),
            Position.at(0, 1), Position.at(1, 0),
            Position.at(0, 2), Position.at(1, 1)));

    assertThat(alg.getPositionsWithCapacity()).containsExactly(Position.at(1, 0), Position.at(1, 2));
  }
}

package matching;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import hlt.Position;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;

public class HungarianAlgorithmTest {

  @Test
  public void testSimpleMatch() {
    BipartiteGraph graph = new BipartiteGraph();

    graph.addNode(
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

    graph.addNode(
        Position.at(0, 0),
        ImmutableMap.of(
            Position.at(0, 0), 0.0,
            Position.at(0, 1), 22.0,
            Position.at(-1, 0), 14.0,
            Position.at(1, 0), 13.4,
            Position.at(0, -1), 44.2));

    graph.addNode(
        Position.at(0, -1),
        ImmutableMap.of(
            Position.at(0, -1), 44.2,
            Position.at(0, 0), 0.0,
            Position.at(0, -2), 14.0,
            Position.at(1, -1), 13.4,
            Position.at(-1, -1), 32.2));

    graph.addNode(
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
            Position.at(0, -1), Position.at(-1, -1)));
  }



}

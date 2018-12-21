package matching;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import hlt.Position;
import org.junit.Test;

public class BipartiteGraphTest {

  @Test
  public void edgeTest() {
    Vertex v1 = new Vertex(new Position(0, 0), 3.029301);
    Vertex v2 = new Vertex(new Position(0, 1), 0);

    Edge e1 = new Edge(v1, v2, 3042.0);
    Edge e2 = new Edge(v2, v1, 3042.0);

    assertThat(e1).isEqualTo(e2);
  }

  @Test
  public void testGraphConstruction() {
    BipartiteGraph graph = new BipartiteGraph();

    graph.addNode(
        Position.at(0, 0),
        ImmutableMap.of(
            Position.at(0, 1), 22.0,
            Position.at(-1, 0), 14.0,
            Position.at(1, 0), 13.4,
            Position.at(0, -1), 44.2));

    Vertex v = Iterables.getOnlyElement(graph.sourceNodes);
    assertThat(v.label).isEqualTo(44.2);
    System.out.println(graph);
  }
}

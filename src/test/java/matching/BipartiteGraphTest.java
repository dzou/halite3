package matching;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hlt.Position;
import hlt.Ship;
import map.Grid;
import org.junit.Test;
import shipagent.MoveScorer;
import shipagent.ShipRouter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static matching.Vertex.Type.SHIP;
import static util.TestUtil.ship;

public class BipartiteGraphTest {

  @Test
  public void edgeTest() {
    Vertex v1 = new Vertex(new Position(0, 0), 3.029301, SHIP, null);
    Vertex v2 = new Vertex(new Position(0, 1), 0, SHIP, null);

    Edge e1 = new Edge(v1, v2, 3042.0);
    Edge e2 = new Edge(v2, v1, 3042.0);

    assertThat(e1).isEqualTo(e2);
  }

  @Test
  public void testGraphLoading() {
    Integer[][] simpleGrid = {
        {1000, 500, 1000, 1000},
        {500, 500, 500, 1000},
        {100, 100, 100, 100},
        {100, 100, 100, 100},
    };
    Grid<Integer> grid = new Grid(simpleGrid);

    Ship s1 = ship(0, 0, 200);
    Ship s2 = ship(2, 0, 200);
    ImmutableSet<Ship> myShips = ImmutableSet.of(s1, s2);

    MoveScorer scorer = new MoveScorer(grid, Position.at(0, 3), 9999, myShips, ImmutableSet.of(), ImmutableMap.of());

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    bipartiteGraph.addShip(s1, scorer.getDecisions(s1));
    bipartiteGraph.addShip(s2, scorer.getDecisions(s2));

    assertThat(bipartiteGraph.ships).hasSize(2);
    assertThat(bipartiteGraph.destinations).hasSize(8);


    Vertex v1 = bipartiteGraph.ships.get(Position.at(0, 0));
    Edge e1 = v1.edges.get(0);
    Vertex destination = e1.destination;

    bipartiteGraph.assignmentEdges.add(e1);
    assertThat(bipartiteGraph.assignmentEdges).containsExactly(destination.edges.get(0));

  }

  @Test
  public void testSimpleMatch() {
    Integer[][] simpleGrid = {
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000}
    };
    Grid<Integer> grid = new Grid(simpleGrid);

    HashSet<Ship> ships = new HashSet<>();
    for (int i = 1; i < simpleGrid.length; i++) {
      for (int j = 1; j < simpleGrid.length; j++) {
        Ship ship = ship(i, j, 1000);
        ships.add(ship);
      }
    }

    ShipRouter router = new ShipRouter(
        grid,
        Position.at(0, 0),
        9999,
        ships,
        ImmutableSet.of(),
        ImmutableMap.of());

    Map<Ship, Position> moves = router.routeShips();
    assertThat(moves).hasSize(16);
  }
}
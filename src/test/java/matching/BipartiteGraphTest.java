package matching;

import map.Grid;
import hlt.Position;
import hlt.Ship;
import org.junit.Test;
import shipagent.ShipRouter;

import java.util.HashSet;

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
    ShipRouter router = new ShipRouter(grid, Position.at(0, 3));

    Ship s1 = ship(0, 0, 200);
    Ship s2 = ship(2, 0, 200);

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    bipartiteGraph.addShip(s1, router.getDecisions(s1));
    bipartiteGraph.addShip(s2, router.getDecisions(s2));

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
    ShipRouter router = new ShipRouter(grid, Position.at(0, 0));

    BipartiteGraph bipartiteGraph = new BipartiteGraph();

    for (int i = 1; i < simpleGrid.length; i++) {
      for (int j = 1; j < simpleGrid.length; j++) {
        Ship ship = ship(i, j, 1000);
        bipartiteGraph.addShip(ship, router.getDecisions(ship));
      }
    }

    HashSet<Edge> edges = bipartiteGraph.matchShipsToDestinations();
    for (Edge e : edges) {
      System.out.println(e);
    }
  }


  @Test
  public void testShipMatchEmptyHalite() {
    Integer[][] simpleGrid = {
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {000, 000, 000, 000, 000},
        {10, 0, 10, 000, 500},
    };
    Grid<Integer> grid = new Grid(simpleGrid);
    ShipRouter router = new ShipRouter(grid, Position.at(0, 3));

    Ship s1 = ship(0, 3, 000);
    Ship s2 = ship(1, 3, 000);

    BipartiteGraph bipartiteGraph = new BipartiteGraph();
    bipartiteGraph.addShip(s1, router.getDecisions(s1));
    bipartiteGraph.addShip(s2, router.getDecisions(s2));

    HashSet<Edge> edges = bipartiteGraph.matchShipsToDestinations();
    System.out.println(edges);
  }
}
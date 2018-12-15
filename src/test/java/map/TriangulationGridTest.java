package map;

import com.google.common.collect.ImmutableList;
import hlt.Ship;
import org.junit.Test;

import java.util.List;

import static util.TestUtil.ship;

public class TriangulationGridTest {

  @Test
  public void testTriangulationGrid() {
    List<Ship> ships = ImmutableList.of(
        // ship(0, 0),
        ship(4, 4)
    );

    TriangulationGrid tGrid = new TriangulationGrid(ships, 2);

    System.out.println(tGrid.debugString(32));
  }
}

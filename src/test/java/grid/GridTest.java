package grid;

import hlt.Position;
import map.Navigator;
import map.Path;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class GridTest {

  @Test
  public void testCostGrid() {

    int[][] simpleGrid = {
      {3, 4, 3, 1},
      {2, 1, 1, 1},
      {4, 5, 8, 2},
      {4, 5, 3, 1}
    };

    Grid grid = new Grid(simpleGrid);
    Navigator navigator = new Navigator();

    Path path = navigator.findShortestPath(Position.at(1, 2), Position.at(3, 0), grid);
    assertThat(path.toString()).isEqualTo("[(1, 2), (0, 2), (3, 2), (3, 3), (3, 0)]");
  }
}

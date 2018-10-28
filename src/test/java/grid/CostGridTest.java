package grid;

import hlt.Position;
import map.Path;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class CostGridTest {

  @Test
  public void testCostGridCreate() {
    Integer[][] rawGrid = {
        {5, 2, 3, 4, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2},
        {0, 1, 1, 4, 2}
    };

    Grid<Integer> grid = new Grid(rawGrid);
    CostGrid costGrid = CostGrid.create(grid, Position.at(0, 4));
    assertThat(costGrid.get(2,4)).isEqualTo(1);

//    System.out.println(costGrid);
//    Path p = Navigator.findShortestPath(Position.at(0, 4), Position.at(3, 1), costGrid);
//    System.out.println(p);
  }

  @Test
  public void testCostGridNavigate() {
    Integer[][] rawGrid = {
        {5, 2, 3, 4, 2},
        {2, 1, 1, 4, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2},
        {0, 1, 1, 4, 2}
    };

    Grid grid = new Grid(rawGrid);
    CostGrid costGrid = CostGrid.create(grid, Position.at(0, 4));

    Path path = costGrid.findPath(Position.at(3, 1));
    assertThat(path.toString()).isEqualTo("[(0, 4), (0, 3), (0, 2), (1, 2), (2, 2), (2, 1), (3, 1)]");
    assertThat(costGrid.get(3, 1)).isEqualTo(5);
  }

//  @Test
//  public void testCostGridBig() {
//    for (int x = 0; x < 500; x++) {
//      int[][] rawGrid = new int[64][64];
//      for (int i = 0; i < rawGrid.length; i++) {
//        for (int j = 0; j < rawGrid[i].length; j++) {
//          rawGrid[i][j] = (int) (Math.random() * 100);
//        }
//      }
//
//      Grid grid = new Grid(rawGrid);
//      CostGrid costGrid = CostGrid.create(grid, Position.at(0, 0));
//
//      Path path = costGrid.findPath(Position.at(3, 1));
//    }
//  }
}

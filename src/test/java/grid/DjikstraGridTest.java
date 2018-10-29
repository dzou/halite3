package grid;

import hlt.Position;
import map.Path;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class DjikstraGridTest {

  @Test
  public void testCostGridCreate() {
    Integer[][] rawGrid = {
        {5, 2, 3, 4, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2},
        {0, 1, 1, 4, 2}
    };

    DjikstraGrid djikstraGrid = DjikstraGrid.create(rawGrid, Position.at(0, 4));
    assertThat(djikstraGrid.costCache.get(2,4)).isEqualTo(1);

//    System.out.println(djikstraGrid);
//    Path p = Navigator.findShortestPath(Position.at(0, 4), Position.at(3, 1), djikstraGrid);
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
    DjikstraGrid djikstraGrid = DjikstraGrid.create(rawGrid, Position.at(0, 4));

    Path path = djikstraGrid.findPath(Position.at(3, 1));
    assertThat(path.toString()).isEqualTo("[(0, 4), (0, 3), (0, 2), (1, 2), (2, 2), (2, 1), (3, 1)]");
    assertThat(djikstraGrid.costCache.get(3, 1)).isEqualTo(5);
  }

  @Test
  public void testNavigateWithDestination() {
    Integer[][] rawGrid = {
        {5, 2, 3, 4, 2},
        {2, 1, 1, 4, 2},
        {1, 1, 0, 10, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2}
    };
    DjikstraGrid djikstraGrid = DjikstraGrid.create(rawGrid, Position.at(2, 2), Position.at(4, 2));

    Path path = djikstraGrid.findPath(Position.at(4, 2));
    assertThat(path.toString()).isEqualTo("[(2, 2), (6, 2), (5, 2), (4, 2)]");
  }

  @Test
  public void testEarlyExitWithDestination() {
    Integer[][] rawGrid = {
        {5, 2, 3, 4, 2},
        {2, 1, 1, 4, 2},
        {1, 1, 0, 10, 2},
        {1, 1, 1, 4, 2},
        {1, 1, 1, 4, 2}
    };
    DjikstraGrid djikstraGrid = DjikstraGrid.create(rawGrid, Position.at(2, 2), Position.at(0, 2));

    Path path = djikstraGrid.findPath(Position.at(0, 2));
    assertThat(djikstraGrid.costCache.get(0, 4)).isEqualTo(Integer.MAX_VALUE);
 }

//  @Test
//  public void testCostGridBig() {
//    for (int x = 0; x < 500; x++) {
//      Integer[][] rawGrid = new Integer[64][64];
//      for (int i = 0; i < rawGrid.length; i++) {
//        for (int j = 0; j < rawGrid[i].length; j++) {
//          rawGrid[i][j] = (int) (Math.random() * 100);
//        }
//      }
//
//      DjikstraGrid costGrid = DjikstraGrid.create(rawGrid, Position.at(0, 0));
//
//      Path path = costGrid.findPath(Position.at(3, 1));
//    }
//  }
}

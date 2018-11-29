package map;

import hlt.Position;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class GridTest {

  @Test
  public void testSubGrid() {
    Integer[][] rawGrid = {
        {5, 2, 3, 4, 2, 1, 1, 8},
        {5, 2, 3, 4, 2, 1, 1, 8},
        {5, 2, 3, 4, 2, 1, 1, 8},
        {5, 2, 3, 4, 2, 1, 1, 8},
        {5, 2, 6, 0, 2, 1, 1, 8},
        {5, 2, 0, 4, 2, 1, 1, 8},
        {5, 2, 3, 4, 2, 1, 1, 8},
        {5, 2, 3, 4, 2, 1, 1, 8},
    };

    Grid<Integer> grid = new Grid<>(rawGrid);

    Grid<Integer> subGrid = grid.subGrid(Position.at(2, 5), 1);
    System.out.println(subGrid);

    Grid<Integer> expectedGrid = new Grid<>(new Integer[][]{
        {0, 4, 2},
        {3, 4, 2},
        {6, 0, 2}
    });
    assertThat(subGrid).isEqualTo(expectedGrid);

    DjikstraGrid djikstraGrid = DjikstraGrid.create(subGrid, Position.at(0, 0));
    System.out.println(djikstraGrid);
  }

}

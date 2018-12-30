package map;

import hlt.Direction;
import hlt.Position;
import org.junit.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class LocalCostGridTest {

  @Test
  public void testFindLowestCostPath() {
    Integer[][] rawGrid = {
        {2, 4, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 8, 2, 1, 1, 8},
        {1, 1, 3, 3, 4, 4, 4, 4},
        {1, 1, 3, 3, 4, 1, 1, 7},
        {1, 1, 3, 3, 4, 1, 1, 5},
        {1, 1, 3, 3, 4, 4, 4, 6},
    };
    Grid<Integer> haliteGrid = new Grid<>(rawGrid);

    LocalCostGrid localCostGrid = LocalCostGrid.create(haliteGrid, Position.at(6, 4), 3);

    assertThat(localCostGrid.getCostToDest(Position.at(1, 2), Direction.WEST)).isEqualTo(22);
    assertThat(localCostGrid.getCostToDest(Position.at(1, 2), Direction.EAST)).isEqualTo(14);
    assertThat(localCostGrid.getCostToDest(Position.at(1, 2), Direction.STILL)).isEqualTo(14);
  }

  @Test
  public void testCreatSubGrid() {
    Integer[][] rawGrid = {
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {2, 2, 2, 2, 2, 1, 1, 8},
        {1, 1, 3, 3, 4, 4, 4, 4},
        {1, 1, 3, 3, 4, 1, 1, 7},
        {1, 1, 3, 3, 4, 1, 1, 5},
        {1, 1, 3, 3, 4, 4, 4, 6},
    };

    Grid<Integer> grid = new Grid<>(rawGrid);

    int[][] result = LocalCostGrid.createSubgrid(grid, Position.at(7, 5), 2);

    int[][] expected = {
        {1, 1, 8, 2, 2},
        {4, 4, 4, 1, 1},
        {1, 1, 7, 1, 1},
        {1, 1, 5, 1, 1},
        {4, 4, 6, 1, 1}
    };

    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void testCostCacheConstruction() {
    int[][] rawGrid = {
        {4, 4, 4, 4},
        {4, 1, 3, 7},
        {4, 1, 1, 5},
        {4, 2, 4, 6},
    };

    int[][] costcache = LocalCostGrid.buildCostCache(rawGrid, Position.at(0, 0));

    for (int[] row : costcache) {
      System.out.println(Arrays.toString(row));
    }

    int[][] expected = {
        {4, 8, 12, 16},
        {8, 9, 12, 19},
        {12, 10, 11, 16},
        {16, 12, 15, 21}
    };

    assertThat(costcache).isEqualTo(expected);
  }
}

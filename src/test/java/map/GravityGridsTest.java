package map;

import grid.Grid;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class GravityGridsTest {

  @Test
  public void testTwoCellInfluence() {
    Integer[][] haliteField = {
        {000, 100, 000},
        {000, 100, 000},
        {000, 000, 000}
    };

    Grid<Integer> grid = new Grid<>(haliteField);
    Grid<Double> influenceMap = GravityGrids.createGravityGrid(grid);

    Grid<Double> expectedGrid = new Grid<>(new Double[][]{
        {75.0, 150.0, 75.0},
        {75.0, 150.0, 75.0},
        {50.0, 100.0, 50.0},
    });

    assertThat(influenceMap).isEqualTo(expectedGrid);
  }

  @Test
  public void testGeneralCaseSmall() {
    Integer[][] haliteField = {
        {000, 100, 000},
        {100, 100, 100},
        {000, 100, 000}
    };

    Grid<Integer> grid = new Grid<>(haliteField);
    Grid<Double> influenceMap = GravityGrids.createGravityGrid(grid);

    Grid<Double> expectedGrid = new Grid<>(new Double[][]{
        {75.0, 150.0, 75.0},
        {75.0, 150.0, 75.0},
        {50.0, 100.0, 50.0},
    });

    assertThat(influenceMap).isEqualTo(expectedGrid);
  }
}

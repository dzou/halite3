package map;

import org.junit.Test;

public class GravityGridsTest {

  @Test
  public void testTwoCellInfluence() {
    Integer[][] haliteField = {
        {000, 100, 000},
        {000, 50, 000},
        {000, 000, 000}
    };

    Grid<Integer> grid = new Grid<>(haliteField);
    Grid<Double> influenceMap = GravityGrids.createGravityGrid(grid);

    System.out.println(influenceMap);

//    Grid<Double> expectedGrid = new Grid<>(new Double[][]{
//        {31.25, 125.0, 31.25},
//        {31.25, 125.0, 31.25},
//        {12.50, 50.00, 12.50},
//    });
//
//    assertThat(influenceMap).isEqualTo(expectedGrid);
  }

  @Test
  public void testMaximumSizeCase() {
    Integer[][] haliteField = new Integer[10][10];
    for (int i = 0; i < haliteField.length; i++) {
      for (int j = 0; j < haliteField[i].length; j++) {
        haliteField[i][j] = 1000;
      }
    }
    haliteField[0][0] = 250;

    Grid<Integer> grid = new Grid<>(haliteField);
    Grid<Double> influenceMap = GravityGrids.createGravityGrid(grid);

    System.out.println(influenceMap);


//    for (int i = 0; i < influenceMap.height; i++) {
//      for (int j = 0; j < influenceMap.width; j++) {
//        assertThat(influenceMap.get(j, i)).isGreaterThan(2770.0);
//        assertThat(influenceMap.get(j, i)).isLessThan(2800.0);
//      }
//    }

  }
}

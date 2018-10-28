package map;

import com.google.common.collect.ImmutableList;
import grid.DjikstraGrid;
import hlt.Position;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class GoalGeneratorTest {

  @Test
  public void testGenerateGoals() {

    Integer[][] rawGrid = {
        {1, 1, 5, 1, 1},
        {1, 1, 1, 1, 1},
        {4, 1, 0, 1, 4},
        {1, 1, 1, 1, 1},
        {1, 1, 3, 1, 6},
    };
    DjikstraGrid djikstraGrid = DjikstraGrid.create(rawGrid, Position.at(2, 2));

    GoalGenerator goalGenerator = new GoalGenerator(djikstraGrid);

    GoalGenerator.PositionComparator comparator = new GoalGenerator.PositionComparator(djikstraGrid);
    ImmutableList<Position> positions = ImmutableList.sortedCopyOf(
        comparator, goalGenerator.getBestPositions(5));

    assertThat(positions.toString()).isEqualTo("[(2, 0), (4, 2), (0, 2), (4, 4), (2, 4)]");

//    System.out.println(positions);
//    for (Position position : positions) {
//      System.out.println(position + " -> " + comparator.getScore(position));
//    }
  }
}

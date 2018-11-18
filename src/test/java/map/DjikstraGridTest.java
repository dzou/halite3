package map;

import hlt.Position;
import map.DjikstraGrid;
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

    DjikstraGrid djikstraGrid = DjikstraGrid.create(new Grid<Integer>(rawGrid), Position.at(0, 4));
    assertThat(djikstraGrid.costCache.get(4, 2)).isEqualTo(4);
  }
}

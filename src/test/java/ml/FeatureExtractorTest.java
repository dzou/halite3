package ml;

import grid.DjikstraGrid;
import grid.Grid;
import hlt.Position;
import org.junit.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static util.TestUtil.ship;

public class FeatureExtractorTest {

  @Test
  public void testExtractFeaturesAccurate() {
    Integer[][] haliteGrid = {
        {50, 30, 20, 10, 10},
        {50, 30, 20, 10, 10},
        {50, 30, 00, 10, 10},
        {50, 30, 20, 10, 10},
        {50, 30, 20, 10, 10},
    };

    Grid<Integer> grid = new Grid(haliteGrid);
    DjikstraGrid gridToHome = DjikstraGrid.create(grid, Position.at(2, 2), null);
    DjikstraGrid gridToShip = DjikstraGrid.create(grid, Position.at(0, 0), Position.at(1, 3));

    FeatureExtractor featureExtractor = new FeatureExtractor(gridToHome, Position.at(2, 2));

    ExploreVector vector = featureExtractor.extractVector(ship(0, 0), gridToShip.findPath(Position.at(1, 3)));

    System.out.println(vector);

    Map<ExploreVector.Feature, Double> features = vector.vectorValues;
    assertThat(features.get(ExploreVector.Feature.EXPLORE_DIST_TO_HOME)).isEqualTo(2.0);
    assertThat(features.get(ExploreVector.Feature.MAX_HALITE_COLLECT)).isEqualTo(12.0);
    assertThat(features.get(ExploreVector.Feature.HALITE_CAPACITY_ON_SHIP)).isEqualTo(1000.0);
    assertThat(features.get(ExploreVector.Feature.PROJECTED_HALITE_TO_HOME)).isEqualTo(-9.0);
    assertThat(features.get(ExploreVector.Feature.PROJECTED_HALITE_THRU_EXPLORE_TO_HOME)).isEqualTo(10.0);
  }
}

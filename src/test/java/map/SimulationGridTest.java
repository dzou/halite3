package map;

import hlt.Direction;
import hlt.Position;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class SimulationGridTest {

  @Test
  public void testSimulationGrid() {
    Integer[][] rawGrid = new Integer[][] {
        {200, 200, 100},
        {500, 150, 200},
        {  0,   0,  20}
    };
    Grid<Integer> haliteGrid = new Grid<>(rawGrid);

    SimulationGrid simulationGrid = new SimulationGrid(haliteGrid, Position.at(0, 0));

    simulationGrid.moveShip(Direction.STILL);
    simulationGrid.moveShip(Direction.SOUTH);
    simulationGrid.moveShip(Direction.STILL);
    simulationGrid.moveShip(Direction.STILL);
    simulationGrid.moveShip(Direction.SOUTH);

    assertThat(simulationGrid.getHaliteGained()).isEqualTo((50 + 218) - (15 + 28));
    assertThat(simulationGrid.getHalite(Position.at(0, 1))).isEqualTo(282);
    assertThat(simulationGrid.getPosition()).isEqualTo(Position.at(0, 2));
  }

  @Test
  public void testSimulateWithInspire() {
    Integer[][] rawGrid = new Integer[][] {
        {200, 200, 100},
        {500, 150, 200},
        {  0,   0,  20}
    };
    Grid<Integer> haliteGrid = new Grid<>(rawGrid);

    SimulationGrid simulationGrid = new SimulationGrid(haliteGrid, Position.at(0, 0));

    simulationGrid.moveShip(Direction.STILL, true);
    simulationGrid.moveShip(Direction.SOUTH);
    simulationGrid.moveShip(Direction.STILL, true);
    simulationGrid.moveShip(Direction.SOUTH);

    assertThat(simulationGrid.getHaliteGained()).isEqualTo((150 + 375) - (15 + 37));
    assertThat(simulationGrid.getHalite(Position.at(0, 1))).isEqualTo(375);
    assertThat(simulationGrid.getPosition()).isEqualTo(Position.at(0, 2));
  }
}

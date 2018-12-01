package shipagent;

import hlt.Constants;
import hlt.Game;
import hlt.Log;
import map.Grid;

public class HaliteSpender {

  public static boolean shouldMakeShip(
      int currentTurnNumber,
      Grid<Integer> haliteGrid) {

    int turnsRemaining = Constants.MAX_TURNS - currentTurnNumber;

    double haliteSum = 0;
    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        haliteSum += haliteGrid.get(x, y);
      }
    }
    double avgHalitePotential = 0.25 *  haliteSum / (haliteGrid.width * haliteGrid.height);

    return turnsRemaining * avgHalitePotential > 2000;
  }
}

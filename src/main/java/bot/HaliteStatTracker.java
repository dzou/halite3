package bot;

import map.Grid;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class HaliteStatTracker {

  private static final int SNAPSHOT_COUNT = 30;

  private static final ArrayDeque<Integer> sumSnapshots = new ArrayDeque<>();

  private HaliteStatTracker () {}

  public static int getHaliteSum() {
    if (sumSnapshots.isEmpty()) {
      return 0;
    }

    return sumSnapshots.getLast();
  }

  public static double getHaliteConsumptionRate(int shipCount) {
    if (shipCount == 0) {
      return 0.0;
    }

    ArrayList<Integer> snapshots = new ArrayList<>(sumSnapshots);

    int differenceSum = 0;
    for (int i = 0; i < snapshots.size() - 1; i++) {
      differenceSum += snapshots.get(i) - snapshots.get(i + 1);
    }

    double avgDifference = 1.0 * differenceSum / Math.max(1, sumSnapshots.size());
    return avgDifference / shipCount;
  }

  public static void loadSnapshot(Grid<Integer> haliteGrid) {
    sumSnapshots.addLast(sumGrid(haliteGrid));
    if (sumSnapshots.size() > SNAPSHOT_COUNT) {
      sumSnapshots.removeFirst();
    }
  }

  private static int sumGrid(Grid<Integer> haliteGrid) {
    int result = 0;
    for (int y = 0; y < haliteGrid.height; y++) {
      for (int x = 0; x < haliteGrid.width; x++) {
        result += haliteGrid.get(x, y);
      }
    }

    return result;
  }
}

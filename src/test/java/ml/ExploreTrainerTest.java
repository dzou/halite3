package ml;

import org.jblas.DoubleMatrix;
import org.junit.Test;

public class ExploreTrainerTest {

  @Test
  public void testModelUpdates() {
    ExploreModel model = new ExploreModel();
    ExploreTrainer trainer = new ExploreTrainer(model);

    trainer.addStateActionPair(ActionType.EXPLORE, ExploreVector.of(1, 1, 1, 1, 1));
    trainer.addStateActionPair(ActionType.RETURN, ExploreVector.of(1, 1, 1, 1, 1));
    trainer.addStateActionPair(ActionType.STAY, ExploreVector.of(1, 1, 1, 1, 1));
    trainer.updateWeights(22360);

    System.out.println(model);

    ExploreVector vector = ExploreVector.of(1, 1, 1, 1, 1);
    System.out.println(model.getScore(ActionType.STAY, vector));
    System.out.println(model);

    model.sampleAction(vector);
    System.out.println(model);


  }

  @Test
  public void testVectorConversion() {
    DoubleMatrix vector = ExploreVector.of(2, 2, 2, 100, 2, 2, 2).toVector();

    System.out.println(vector);
  }
}

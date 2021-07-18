package gis2.scenario;

import gis2.GisScenario;

import java.util.Random;

/**
 * Function for randomizing a scenario.
 */
public class RandomiseFunction extends AbstractFunction {
  private Random random;

  /**
   * Construct a randomizer function.
   *
   * @param editor The editor instance.
   */
  public RandomiseFunction(ScenarioEditor editor) {
    super(editor);
    random = new Random();
  }

  @Override
  public String getName() {
    return "Randomise";
  }

  @Override
  public void execute() {
    RandomScenarioGenerator generator = new RandomScenarioGenerator();
    GisScenario s = generator.makeRandomScenario(editor.getMap(), random);
    try {
      editor.setScenario(editor.getMap(), s);
      editor.setChanged();
      editor.updateOverlays();
    } catch (CancelledByUserException e) {
      // Ignore
    }
  }
}
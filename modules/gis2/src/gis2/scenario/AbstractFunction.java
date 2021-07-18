package gis2.scenario;

/**
 * Abstract base class for scenario editing functions.
 */
public abstract class AbstractFunction implements Function {
  /** The editor instance. */
  protected ScenarioEditor editor;

  /**
   * Construct an AbstractFunction.
   *
   * @param editor The editor instance.
   */
  protected AbstractFunction(ScenarioEditor editor) {
    this.editor = editor;
  }
}
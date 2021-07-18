package gis2.scenario;

/**
 * Abstract base class for scenario editing tools.
 */
public abstract class AbstractTool implements Tool {
  /** The scenario editor instance. */
  protected ScenarioEditor editor;

  /**
   * Construct an AbstractTool.
   *
   * @param editor The scenario editor instance.
   */
  protected AbstractTool(ScenarioEditor editor) {
    this.editor = editor;
  }
}
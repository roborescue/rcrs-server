package gis2.scenario;

import gis2.GisScenario;

import java.util.HashSet;

/**
 * Function for removing all fires.
 */
public class ClearFiresFunction extends AbstractFunction {
  /**
   * Construct a clear fires function.
   *
   * @param editor The editor instance.
   */
  public ClearFiresFunction(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove fires";
  }

  @Override
  public void execute() {
    GisScenario s = editor.getScenario();
    s.setFires(new HashSet<Integer>());
    editor.setChanged();
    editor.updateOverlays();
  }
}
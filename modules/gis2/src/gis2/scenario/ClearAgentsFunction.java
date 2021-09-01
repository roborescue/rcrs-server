package gis2.scenario;

import gis2.GisScenario;

import java.util.HashSet;

/**
 * Function for removing all agents.
 */
public class ClearAgentsFunction extends AbstractFunction {
  /**
   * Construct a clear agents function.
   *
   * @param editor The editor instance.
   */
  public ClearAgentsFunction(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove agents";
  }

  @Override
  public void execute() {
    GisScenario s = editor.getScenario();
    s.setFireBrigades(new HashSet<Integer>());
    s.setFireStations(new HashSet<Integer>());
    s.setPoliceForces(new HashSet<Integer>());
    s.setPoliceOffices(new HashSet<Integer>());
    s.setAmbulanceTeams(new HashSet<Integer>());
    s.setAmbulanceCentres(new HashSet<Integer>());
    editor.setChanged();
    editor.updateOverlays();
  }
}
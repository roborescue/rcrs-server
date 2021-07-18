package gis2.scenario;

import gis2.GisScenario;

import java.util.HashSet;

/**
 * Function for removing all agents, fires, civilians and refuges.
 */
public class ClearAllFunction extends AbstractFunction {
  /**
   * Construct a clear all function.
   *
   * @param editor The editor instance.
   */
  public ClearAllFunction(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove all";
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
    s.setCivilians(new HashSet<Integer>());
    s.setFires(new HashSet<Integer>());
    s.setRefuges(new HashSet<Integer>());
    s.setGasStations(new HashSet<Integer>());
    s.setHydrants(new HashSet<Integer>());
    editor.setChanged();
    editor.updateOverlays();
  }
}
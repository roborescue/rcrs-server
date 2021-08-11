package gis2.scenario;

import gis2.GisScenario;
import java.util.Set;
import java.util.HashSet;

/**
   Function for removing all agents, fires, civilians and refuges.
*/
public class ClearAllFunction extends AbstractFunction {
    /**
       Construct a clear all function.
       @param editor The editor instance.
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
        Set<Integer> empty = new HashSet<Integer>();
        s.setFireBrigades(empty);
        s.setFireStations(empty);
        s.setPoliceForces(empty);
        s.setPoliceOffices(empty);
        s.setAmbulanceTeams(empty);
        s.setAmbulanceCentres(empty);
        s.setCivilians(empty);
        s.setFires(empty);
        s.setRefuges(empty);
        editor.setChanged();
        editor.updateOverlays();
    }
}
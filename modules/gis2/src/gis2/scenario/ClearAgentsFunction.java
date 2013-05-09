package gis2.scenario;

import gis2.GisScenario;
import java.util.Set;
import java.util.HashSet;

/**
   Function for removing all agents.
*/
public class ClearAgentsFunction extends AbstractFunction {
    /**
       Construct a clear agents function.
       @param editor The editor instance.
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
        Set<Integer> empty = new HashSet<Integer>();
        s.setFireBrigades(empty);
        s.setFireStations(empty);
        s.setPoliceForces(empty);
        s.setPoliceOffices(empty);
        s.setAmbulanceTeams(empty);
        s.setAmbulanceCentres(empty);
        editor.setChanged();
        editor.updateOverlays();
    }
}
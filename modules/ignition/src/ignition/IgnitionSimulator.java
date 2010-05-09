package ignition;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.log.Logger;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.Building;

import java.util.Set;

/**
   A simulator that determines when new building fires begin.
*/
public class IgnitionSimulator extends StandardSimulator {
    private IgnitionModel ignitionModel;

    @Override
    protected void postConnect() {
        super.postConnect();
        ignitionModel = new RandomIgnitionModel(model, config);
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        long start = System.currentTimeMillis();
        int time = c.getTime();
        Logger.info("Timestep " + time);
        // Find out which buildings have ignited.
        Set<Building> buildings = ignitionModel.findIgnitionPoints(model, c.getTime());
        for (Building next : buildings) {
            Logger.info("Igniting " + next);
            next.setIgnition(true);
            changes.addChange(next, next.getIgnitionProperty());
        }
        long end = System.currentTimeMillis();
        Logger.info("Timestep " + time + " took " + (end - start) + " ms");
    }

    @Override
    public String getName() {
        return "Ignition simulator";
    }
}

package ignition;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.control.KSCommands;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.Building;

import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A simulator that determines when new building fires begin.
*/
public class IgnitionSimulator extends StandardSimulator {
    private static final Log LOG = LogFactory.getLog(IgnitionSimulator.class);

    private IgnitionModel ignitionModel;

    @Override
    protected void postConnect() {
        super.postConnect();
        ignitionModel = new RandomIgnitionModel(model, config);
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        // Find out which buildings have ignited.
        Set<Building> buildings = ignitionModel.findIgnitionPoints(model, c.getTime());
        for (Building next : buildings) {
            LOG.info("Igniting " + next);
            next.setIgnition(true);
            changes.addChange(next, next.getIgnitionProperty());
        }
    }

    @Override
    public String getName() {
        return "Ignition simulator";
    }
}
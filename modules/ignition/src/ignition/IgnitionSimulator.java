package ignition;

import rescuecore2.components.AbstractSimulator;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.messages.control.Commands;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Building;

import java.util.Set;

/**
   A simulator that determines when new building fires begin.
*/
public class IgnitionSimulator extends AbstractSimulator<StandardEntity> {
    private IgnitionModel ignitionModel;

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        return new StandardWorldModel();
    }

    @Override
    protected void postConnect() {
        ignitionModel = new RandomIgnitionModel((StandardWorldModel)model, config);
    }

    @Override
    protected void handleCommands(Commands c) {
        // Find out which buildings have ignited.
        Set<Building> buildings = ignitionModel.findIgnitionPoints((StandardWorldModel)model, c.getTime());
        ChangeSet changes = new ChangeSet();
        for (Building next : buildings) {
            next.setIgnition(true);
            changes.addChange(next, next.getIgnitionProperty());
        }
        send(new SKUpdate(simulatorID, c.getTime(), changes));
    }

    @Override
    public String getName() {
        return "Ignition simulator";
    }
}
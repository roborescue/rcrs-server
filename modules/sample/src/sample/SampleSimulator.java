package sample;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.components.AbstractSimulator;

/**
   A sample simulator that doesn't do anything useful.
 */
public class SampleSimulator extends AbstractSimulator<DefaultWorldModel<Entity>> {
    @Override
    protected DefaultWorldModel<Entity> createWorldModel() {
        return new DefaultWorldModel<Entity>(Entity.class);
    }

    @Override
    protected void postConnect() {
        super.postConnect();
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
    }
}

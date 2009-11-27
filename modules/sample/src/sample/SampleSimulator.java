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
        System.out.println("SampleSimulator connected. World has " + model.getAllEntities().size() + " entities.");
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
        System.out.println("SampleSimulator received update: " + u);
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        System.out.println("SampleSimulator received commands: " + c);
    }
}
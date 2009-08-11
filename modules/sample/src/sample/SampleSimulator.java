package sample;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.components.AbstractSimulator;

/**
   A sample simulator that doesn't do anything useful.
 */
public class SampleSimulator extends AbstractSimulator<Entity> {
    @Override
    protected WorldModel<Entity> createWorldModel() {
        return new DefaultWorldModel<Entity>(Entity.class);
    }

    @Override
    protected void postConnect() {
        System.out.println("SampleSimulator connected. World has " + model.getAllEntities().size() + " entities.");
    }

    @Override
    protected void handleUpdate(Update u) {
        super.handleUpdate(u);
        System.out.println("SampleSimulator received update: " + u);
    }

    @Override
    protected void handleCommands(Commands c) {
        System.out.println("SampleSimulator received commands: " + c);
        super.handleCommands(c);
    }
}
package kernel;

import rescuecore2.worldmodel.Entity;

/**
   Interface for objects that are interested in kernel events.
   @param <T> The subclass of Entity that this listener understands.
 */
public interface KernelListener<T extends Entity> {
    /**
       Notification that a timestep has been completed.
       @param time The timestep that has just been completed.
    */
    void timestepCompleted(int time);

    /**
       Notification that an agent has been added.
       @param agent The agent that was added.
     */
    void agentAdded(Agent<T> agent);

    /**
       Notification that an agent has been removed.
       @param agent The agent that was removed.
     */
    void agentRemoved(Agent<T> agent);

    /**
       Notification that a simulator has been added.
       @param simulator The simulator that was added.
     */
    void simulatorAdded(Simulator<T> simulator);

    /**
       Notification that a simulator has been removed.
       @param simulator The simulator that was removed.
     */
    void simulatorRemoved(Simulator<T> simulator);

    /**
       Notification that a viewer has been added.
       @param viewer The viewer that was added.
     */
    void viewerAdded(Viewer<T> viewer);

    /**
       Notification that a viewer has been removed.
       @param viewer The viewer that was removed.
     */
    void viewerRemoved(Viewer<T> viewer);
}
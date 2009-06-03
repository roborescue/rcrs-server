package kernel;

import rescuecore2.worldmodel.Entity;

/**
   Interface for objects that are interested in kernel events.
 */
public interface KernelListener {
    /**
       Notification that a timestep has been completed.
       @param time The timestep that has just been completed.
    */
    void timestepCompleted(int time);

    /**
       Notification that an agent has been added.
       @param agent The agent that was added.
     */
    void agentAdded(Agent agent);

    /**
       Notification that an agent has been removed.
       @param agent The agent that was removed.
     */
    void agentRemoved(Agent agent);

    /**
       Notification that a simulator has been added.
       @param simulator The simulator that was added.
     */
    void simulatorAdded(Simulator simulator);

    /**
       Notification that a simulator has been removed.
       @param simulator The simulator that was removed.
     */
    void simulatorRemoved(Simulator simulator);

    /**
       Notification that a viewer has been added.
       @param viewer The viewer that was added.
     */
    void viewerAdded(Viewer viewer);

    /**
       Notification that a viewer has been removed.
       @param viewer The viewer that was removed.
     */
    void viewerRemoved(Viewer viewer);
}
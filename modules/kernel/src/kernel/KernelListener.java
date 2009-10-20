package kernel;

/**
   Interface for objects that are interested in kernel events.
 */
public interface KernelListener {
    /**
       Notification that a timestep has been completed.
       @param time The timestep that has just been completed.
    */
    void timestepCompleted(Timestep time);

    /**
       Notification that an agent has been added.
       @param agent The agent that was added.
     */
    void agentAdded(AgentProxy agent);

    /**
       Notification that an agent has been removed.
       @param agent The agent that was removed.
     */
    void agentRemoved(AgentProxy agent);

    /**
       Notification that a simulator has been added.
       @param simulator The simulator that was added.
     */
    void simulatorAdded(SimulatorProxy simulator);

    /**
       Notification that a simulator has been removed.
       @param simulator The simulator that was removed.
     */
    void simulatorRemoved(SimulatorProxy simulator);

    /**
       Notification that a viewer has been added.
       @param viewer The viewer that was added.
     */
    void viewerAdded(ViewerProxy viewer);

    /**
       Notification that a viewer has been removed.
       @param viewer The viewer that was removed.
     */
    void viewerRemoved(ViewerProxy viewer);
}
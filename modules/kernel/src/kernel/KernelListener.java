package kernel;

import rescuecore2.Timestep;

/**
   Interface for objects that are interested in kernel events.
 */
public interface KernelListener {
    /**
       Notification that the kernel has started the simulation.
       @param kernel The kernel.
    */
    void simulationStarted(Kernel kernel);

    /**
       Notification that the kernel has ended the simulation and shut down.
       @param kernel The kernel.
    */
    void simulationEnded(Kernel kernel);

    /**
       Notification that a timestep has been completed.
       @param kernel The kernel.
       @param time The timestep that has just been completed.
    */
    void timestepCompleted(Kernel kernel, Timestep time);

    /**
       Notification that an agent has been added.
       @param kernel The kernel.
       @param agent The agent that was added.
     */
    void agentAdded(Kernel kernel, AgentProxy agent);

    /**
       Notification that an agent has been removed.
       @param kernel The kernel.
       @param agent The agent that was removed.
     */
    void agentRemoved(Kernel kernel, AgentProxy agent);

    /**
       Notification that a simulator has been added.
       @param kernel The kernel.
       @param simulator The simulator that was added.
     */
    void simulatorAdded(Kernel kernel, SimulatorProxy simulator);

    /**
       Notification that a simulator has been removed.
       @param kernel The kernel.
       @param simulator The simulator that was removed.
     */
    void simulatorRemoved(Kernel kernel, SimulatorProxy simulator);

    /**
       Notification that a viewer has been added.
       @param kernel The kernel.
       @param viewer The viewer that was added.
     */
    void viewerAdded(Kernel kernel, ViewerProxy viewer);

    /**
       Notification that a viewer has been removed.
       @param kernel The kernel.
       @param viewer The viewer that was removed.
     */
    void viewerRemoved(Kernel kernel, ViewerProxy viewer);
}

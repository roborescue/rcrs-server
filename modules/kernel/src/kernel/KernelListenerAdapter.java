package kernel;

import rescuecore2.Timestep;

/**
   Abstract class for objects that want to implement a subset of the KernelListener interface. All default method implementations do nothing.
 */
public class KernelListenerAdapter implements KernelListener {
    @Override
    public void simulationStarted(Kernel kernel) {}

    @Override
    public void simulationEnded(Kernel kernel) {}

    @Override
    public void timestepCompleted(Kernel kernel, Timestep time) {}

    @Override
    public void agentAdded(Kernel kernel, AgentProxy agent) {}

    @Override
    public void agentRemoved(Kernel kernel, AgentProxy agent) {}

    @Override
    public void simulatorAdded(Kernel kernel, SimulatorProxy simulator) {}

    @Override
    public void simulatorRemoved(Kernel kernel, SimulatorProxy simulator) {}

    @Override
    public void viewerAdded(Kernel kernel, ViewerProxy viewer) {}

    @Override
    public void viewerRemoved(Kernel kernel, ViewerProxy viewer) {}
}

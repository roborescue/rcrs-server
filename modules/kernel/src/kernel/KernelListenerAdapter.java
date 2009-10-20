package kernel;

/**
   Abstract class for objects that want to implement a subset of the KernelListener interface. All default method implementations do nothing.
 */
public class KernelListenerAdapter implements KernelListener {
    @Override
    public void timestepCompleted(Timestep time) {}

    @Override
    public void agentAdded(AgentProxy agent) {}

    @Override
    public void agentRemoved(AgentProxy agent) {}

    @Override
    public void simulatorAdded(SimulatorProxy simulator) {}

    @Override
    public void simulatorRemoved(SimulatorProxy simulator) {}

    @Override
    public void viewerAdded(ViewerProxy viewer) {}

    @Override
    public void viewerRemoved(ViewerProxy viewer) {}
}
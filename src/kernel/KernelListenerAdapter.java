package kernel;

/**
   Abstract class for objects that want to implement a subset of the KernelListener interface. All default method implementations do nothing.
 */
public class KernelListenerAdapter implements KernelListener {
    @Override
    public void timestepCompleted(int time) {}

    @Override
    public void agentAdded(Agent agent) {}

    @Override
    public void agentRemoved(Agent agent) {}

    @Override
    public void simulatorAdded(Simulator simulator) {}

    @Override
    public void simulatorRemoved(Simulator simulator) {}

    @Override
    public void viewerAdded(Viewer viewer) {}

    @Override
    public void viewerRemoved(Viewer viewer) {}
}
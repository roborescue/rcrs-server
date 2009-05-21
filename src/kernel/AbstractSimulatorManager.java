package kernel;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Abstract base class for SimulatorManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractSimulatorManager<T extends Entity, S extends WorldModel<T>> implements SimulatorManager<T, S> {
    private Set<SimulatorManagerListener> listeners;

    /**
       Construct an AbstractSimulatorManager.
     */
    protected AbstractSimulatorManager() {
        listeners = new HashSet<SimulatorManagerListener>();
    }

    @Override
    public void addSimulatorManagerListener(SimulatorManagerListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeSimulatorManagerListener(SimulatorManagerListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Fire a 'simulator connected' event to all listeners.
       @param info The SimulatorInfo to send.
     */
    protected void fireSimulatorConnected(SimulatorInfo info) {
        for (SimulatorManagerListener next : getListeners()) {
            next.simulatorConnected(info);
        }
    }

    private Set<SimulatorManagerListener> getListeners() {
        Set<SimulatorManagerListener> result;
        synchronized (listeners) {
            result = new HashSet<SimulatorManagerListener>(listeners);
        }
        return result;
    }
}
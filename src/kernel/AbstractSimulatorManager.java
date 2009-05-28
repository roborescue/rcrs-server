package kernel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;

/**
   Abstract base class for SimulatorManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractSimulatorManager<T extends Entity, S extends WorldModel<T>> implements SimulatorManager<T, S> {
    private Set<SimulatorManagerListener> listeners;

    private S worldModel;

    private Set<Simulator<T, S>> allSims;

    /**
       Construct an AbstractSimulatorManager.
     */
    protected AbstractSimulatorManager() {
        listeners = new HashSet<SimulatorManagerListener>();
        allSims = new HashSet<Simulator<T, S>>();
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

    @Override
    public Collection<Simulator<T, S>> getAllSimulators() throws InterruptedException {
        synchronized (allSims) {
            return Collections.unmodifiableCollection(allSims);
        }
    }

    @Override
    public final void setWorldModel(S world) {
        worldModel = world;
    }

    @Override
    public void shutdown() {
    }

    /**
       Register a new simulator.
       @param s The new simulator.
     */
    protected void addSimulator(Simulator<T, S> s) {
        synchronized (allSims) {
            allSims.add(s);
        }
    }

    /**
       Get the world model.
       @return The world model.
     */
    protected S getWorldModel() {
        return worldModel;
    }

    /**
       Fire a 'simulator connected' event to all listeners.
       @param sim The Simulator that has connected.
     */
    protected void fireSimulatorConnected(Simulator sim) {
        SimulatorInfo info = new SimulatorInfo(sim.toString());
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
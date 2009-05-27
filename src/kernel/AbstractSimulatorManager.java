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

    private Set<Simulator> allSims;

    /**
       Construct an AbstractSimulatorManager.
     */
    protected AbstractSimulatorManager() {
        listeners = new HashSet<SimulatorManagerListener>();
        allSims = new HashSet<Simulator>();
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
       Register a new simulator.
       @param s The new simulator.
     */
    protected void addSimulator(Simulator s) {
        synchronized (allSims) {
            allSims.add(s);
        }
    }

    /**
       Get all connected simulators.
       @return All simulators.
     */
    protected Collection<Simulator> getAllSimulators() {
        synchronized (allSims) {
            return Collections.unmodifiableCollection(allSims);
        }
    }

    @Override
    public final void setWorldModel(S world) {
        worldModel = world;
    }

    /**
       Get the world model.
       @return The world model.
     */
    protected S getWorldModel() {
        return worldModel;
    }

    @Override
    public void sendToAll(Collection<? extends Message> messages) {
        Collection<Simulator> data = new HashSet<Simulator>();
        synchronized (allSims) {
            data.addAll(allSims);
        }
        for (Simulator next : data) {
            next.send(messages);
        }
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

    /**
       Internal representation of a simulator.
     */
    protected static class Simulator {
        private int id;
        private Connection connection;

        /**
           Construct a Simulator.
           @param id The simulator ID.
           @param c The connection for talking to the simulator.
         */
        public Simulator(int id, Connection c) {
            this.id = id;
            this.connection = c;
        }

        /**
           Get the ID of this simulator.
           @return The simulator ID.
         */
        public int getID() {
            return id;
        }

        /**
           Send some messages to the simulator.
           @param m The messages to send.
         */
        public void send(Collection<? extends Message> m) {
            if (!connection.isAlive()) {
                return;
            }
            try {
                connection.sendMessages(m);
            }
            catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

        /**
           Get the connection for talking to this simulator.
           @return The connection.
         */
        public Connection getConnection() {
            return connection;
        }

        @Override
        public String toString() {
            return connection.toString() + ": " + id;
        }
    }
}
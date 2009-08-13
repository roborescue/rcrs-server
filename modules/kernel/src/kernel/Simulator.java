package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
   This class is the kernel interface to a simulator.
 */
public class Simulator extends AbstractComponent {
    private Map<Integer, Collection<Entity>> updates;
    private int id;

    /**
       Construct a new simulator.
       @param c The connection this simulator is using.
       @param id The ID of the simulator.
     */
    public Simulator(Connection c, int id) {
        super(c);
        this.id = id;
        updates = new HashMap<Integer, Collection<Entity>>();
        c.addConnectionListener(new SimulatorConnectionListener());
    }

    /**
       Get updates from this simulator. This method may block until updates are available.
       @param time The timestep to get updates for.
       @return A collection of entities representing the updates from this simulator.
       @throws InterruptedException If this thread is interrupted while waiting for updates.
    */
    public Collection<Entity> getUpdates(int time) throws InterruptedException {
        Collection<Entity> result = null;
        synchronized (updates) {
            while (result == null) {
                result = updates.get(time);
                if (result == null) {
                    updates.wait(1000);
                }
            }
        }
        return result;
    }

    /**
       Send an update message to this simulator.
       @param time The simulation time.
       @param update The updated entities.
    */
    public void sendUpdate(int time, Collection<? extends Entity> update) {
        send(Collections.singleton(new Update(id, time, update)));
    }

    /**
       Send a set of agent commands to this simulator.
       @param time The current time.
       @param commands The agent commands to send.
     */
    public void sendAgentCommands(int time, Collection<? extends Command> commands) {
        send(Collections.singleton(new Commands(id, time, commands)));
    }

    @Override
    public String toString() {
        return "Simulator " + id + ": " + getConnection().toString();
    }

    /**
       Register an update from the simulator.
       @param time The timestep of the update.
       @param u The set of updated entities.
     */
    protected void updateReceived(int time, Collection<? extends Entity> u) {
        synchronized (updates) {
            Collection<Entity> c = updates.get(time);
            if (c == null) {
                c = new HashSet<Entity>();
                updates.put(time, c);
            }
            c.addAll(u);
            updates.notifyAll();
        }
    }

    private class SimulatorConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof SKUpdate) {
                SKUpdate update = (SKUpdate)msg;
                if (update.getSimulatorID() == id) {
                    System.out.println("Received simulator update: " + msg);
                    updateReceived(update.getTime(), update.getUpdatedEntities());
                }
            }
        }
    }
}
package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.EntityIDRequest;
import rescuecore2.messages.control.EntityIDResponse;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
   This class is the kernel interface to a simulator.
 */
public class SimulatorProxy extends AbstractKernelComponent {
    private Map<Integer, ChangeSet> updates;
    private int id;
    private EntityIDGenerator idGenerator;

    /**
       Construct a new simulator.
       @param name The name of the simulator.
       @param id The ID of the simulator.
       @param c The connection this simulator is using.
    */
    public SimulatorProxy(String name, int id, Connection c) {
        super(name, c);
        this.id = id;
        updates = new HashMap<Integer, ChangeSet>();
        c.addConnectionListener(new SimulatorConnectionListener());
    }

    /**
       Get updates from this simulator. This method may block until updates are available.
       @param time The timestep to get updates for.
       @return A ChangeSet representing the updates from this simulator.
       @throws InterruptedException If this thread is interrupted while waiting for updates.
    */
    public ChangeSet getUpdates(int time) throws InterruptedException {
        ChangeSet result = null;
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
    public void sendUpdate(int time, ChangeSet update) {
        send(new KSUpdate(id, time, update));
    }

    /**
       Send a set of agent commands to this simulator.
       @param time The current time.
       @param commands The agent commands to send.
    */
    public void sendAgentCommands(int time, Collection<? extends Command> commands) {
        send(new KSCommands(id, time, commands));
    }

    @Override
    public String toString() {
        return getName() + " (" + id + "): " + getConnection().toString();
    }

    /**
       Set the EntityIDGenerator.
       @param generator The new EntityIDGenerator.
    */
    public void setEntityIDGenerator(EntityIDGenerator generator) {
        idGenerator = generator;
    }

    /**
       Register an update from the simulator.
       @param time The timestep of the update.
       @param changes The set of changes.
    */
    protected void updateReceived(int time, ChangeSet changes) {
        synchronized (updates) {
            ChangeSet c = updates.get(time);
            if (c == null) {
                c = new ChangeSet();
                updates.put(time, c);
            }
            c.merge(changes);
            updates.notifyAll();
        }
    }

    private class SimulatorConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            Logger.pushLogContext(Kernel.KERNEL_LOG_CONTEXT);
            try {
                if (msg instanceof SKUpdate) {
                    SKUpdate update = (SKUpdate)msg;
                    if (update.getSimulatorID() == id) {
                        updateReceived(update.getTime(), update.getChangeSet());
                    }
                }
                if (msg instanceof EntityIDRequest) {
                    EntityIDRequest req = (EntityIDRequest)msg;
                    Logger.debug("Simulator proxy " + id + " received entity ID request: " + msg);
                    if (req.getSimulatorID() == id) {
                        int requestID = req.getRequestID();
                        int count = req.getCount();
                        List<EntityID> result = new ArrayList<EntityID>(count);
                        for (int i = 0; i < count; ++i) {
                            result.add(idGenerator.generateID());
                        }
                        Logger.debug("Simulator proxy " + id + " sending new IDs: " + result);
                        send(new EntityIDResponse(id, requestID, result));
                    }
                }
            }
            finally {
                Logger.popLogContext();
            }
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

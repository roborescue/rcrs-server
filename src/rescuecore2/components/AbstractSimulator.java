package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;
import java.util.HashSet;

/**
   Abstract base class for simulator implementations.
   @param <T> The subclass of Entity that this simulator understands.
 */
public abstract class AbstractSimulator<T extends Entity> extends AbstractComponent<T> implements Simulator {
    /**
       The ID of this simulator.
    */
    protected int simulatorID;

    private int lastUpdateTime;

    /**
       Create a new AbstractSimulator.
     */
    protected AbstractSimulator() {
    }

    /**
       Get this simulator's ID.
       @return The simulator ID.
     */
    public final int getSimulatorID() {
        return simulatorID;
    }

    @Override
    public void postConnect(Connection c, int id, Collection<Entity> entities) {
        super.postConnect(c, entities);
        this.simulatorID = id;
        c.addConnectionListener(new SimulatorListener());
        lastUpdateTime = 0;
        postConnect();
    }

    /**
       Perform any post-connection work required before acknowledgement of the connection is made. The default implementation does nothing.
     */
    protected void postConnect() {
    }

    /**
       Handle an Update object from the server. The default implementation just updates the world model.
       @param u The Update object.
     */
    protected void handleUpdate(Update u) {
        Collection<Entity> entities = u.getUpdatedEntities();
        int time = u.getTime();
        if (time != lastUpdateTime + 1) {
            System.out.println("WARNING: Recieved an unexpected update from the kernel. Last update: " + lastUpdateTime + ", this update: " + time);
        }
        lastUpdateTime = time;
        model.merge(entities);
    }

    /**
       Handle a Commands object from the server. The default implementation tells the kernel that nothing has changed.
       @param c The Commands object.
     */
    protected void handleCommands(Commands c) {
        send(new SKUpdate(simulatorID, c.getTime(), new HashSet<Entity>()));
    }

    private class SimulatorListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof Update) {
                handleUpdate((Update)msg);
            }
            if (msg instanceof Commands) {
                handleCommands((Commands)msg);
            }
        }
    }
}
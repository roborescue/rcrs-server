package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;

/**
   Abstract base class for component implementations.
   @param <T> The subclass of Entity that this agent understands.
 */
public abstract class AbstractComponent<T extends Entity> implements Component {
    private static final int TIMEOUT = 10000;

    /**
       The connection to the kernel.
    */
    protected Connection connection;

    /**
       The world model.
    */
    protected WorldModel<T> model;

    /**
       Create a new AbstractComponent.
    */
    protected AbstractComponent() {
    }

    /**
       Notification that connection to the kernel succeeded.
       @param c The kernel connection.
       @param entities The entities that the kernel sent on startup.
     */
    protected final void postConnect(Connection c, Collection<Entity> entities) {
        connection = c;
        model = createWorldModel();
        model.addEntities(entities);
    }

    /**
       Construct the world model.
       @return The world model.
    */
    protected abstract WorldModel<T> createWorldModel();

    /**
       Send a message to the kernel and silently ignore any errors.
       @param msg The message to send.
    */
    protected final void send(Message msg) {
        try {
            connection.sendMessage(msg);
        }
        catch (ConnectionException e) {
            // Ignore and log
            System.out.println(e);
        }
    }

    @Override
    public void initialise(Config config) {
    }
}
package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;
import java.util.Random;

/**
   Abstract base class for component implementations.
   @param <T> The subclass of WorldModel that this component understands.
 */
public abstract class AbstractComponent<T extends WorldModel<? extends Entity>> implements Component {
    private static final int TIMEOUT = 10000;

    /**
       The connection to the kernel.
    */
    protected Connection connection;

    /**
       The configuration. This will be automatically updated by the postConnect method to include config information from the kernel.
     */
    protected Config config;

    /**
       The world model.
    */
    protected T model;

    /**
       A random number generator.
    */
    protected Random random;

    /**
       Create a new AbstractComponent.
    */
    protected AbstractComponent() {
    }

    /**
       Notification that connection to the kernel succeeded.
       @param c The kernel connection.
       @param entities The entities that the kernel sent on startup.
       @param kernelConfig The config that the kernel sent on startup.
     */
    protected final void postConnect(Connection c, Collection<Entity> entities, Config kernelConfig) {
        connection = c;
        model = createWorldModel();
        model.addEntities(entities);
        config.merge(kernelConfig);
        random = config.getRandom();
        postConnect();
    }

    /**
       Perform any post-connection work required before acknowledgement of the connection is made. The default implementation does nothing.
     */
    protected void postConnect() {
    }

    /**
       Construct the world model.
       @return The world model.
    */
    protected abstract T createWorldModel();

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
    public void initialise() {
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
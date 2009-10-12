package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Constants;

import java.util.Collection;
import java.util.Random;

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
       The configuration. This will be automatically updated by the postConnect method to include config information from the kernel.
     */
    protected Config config;

    /**
       The world model.
    */
    protected WorldModel<T> model;

    /**
       A random number generator.
    */
    protected final Random random;

    /**
       Create a new AbstractComponent.
    */
    protected AbstractComponent() {
        random = new Random();
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
        long seed = config.getIntValue(Constants.RANDOM_SEED_KEY, 0);
        if (seed != 0) {
            random.setSeed(seed);
        }
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
    public void initialise(Config initialConfig) {
        config = initialConfig;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
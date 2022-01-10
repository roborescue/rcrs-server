package rescuecore2.components;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.log.Logger;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.Shutdown;
import rescuecore2.misc.WorkerThread;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
 * Abstract base class for component implementations.
 *
 * @param <T> The subclass of WorldModel that this component understands.
 */
public abstract class AbstractComponent<T extends WorldModel<? extends Entity>> implements Component {

  /**
   * The connection to the kernel.
   */
  protected Connection connection;

  /**
   * The configuration. This will be automatically updated by the postConnect
   * method to include config information from the kernel.
   */
  protected Config config;

  /**
   * The world model.
   */
  protected T model;

  /**
   * A random number generator.
   */
  protected Random random;

  /**
   * The thread that processes incoming messages.
   */
  private MessageProcessor processor;

  /**
   * Create a new AbstractComponent.
   */
  protected AbstractComponent() {
  }

  /**
   * Notification that connection to the kernel succeeded.
   *
   * @param c            The kernel connection.
   * @param entities     The entities that the kernel sent on startup.
   * @param kernelConfig The config that the kernel sent on startup.
   */
  protected final void postConnect(Connection c, Collection<Entity> entities, Config kernelConfig) {
    connection = c;
    model = createWorldModel();
    model.addEntities(entities);
    config.merge(kernelConfig);
    random = config.getRandom();
    String ndc = getPreferredNDC();
    if (ndc != null) {
      Logger.pushNDC(ndc);
    }
    try {
      Logger.info(this + " connected");
      postConnect();
      processor = new MessageProcessor();
      c.addConnectionListener(new MessageListener());
      processor.start();
    } finally {
      if (ndc != null) {
        Logger.popNDC();
      }
    }
  }

  /**
   * Perform any post-connection work required before acknowledgement of the
   * connection is made. The default implementation does nothing.
   */
  protected void postConnect() {
  }

  /**
   * Construct the world model.
   *
   * @return The world model.
   */
  protected abstract T createWorldModel();

  /**
   * Send a message to the kernel and silently ignore any errors.
   *
   * @param msg The message to send.
   */
  protected final void send(Message msg) {
    try {
      connection.sendMessage(msg);
    } catch (ConnectionException e) {
      // Ignore and log
      Logger.error("Error sending message", e);
    }
  }

  @Override
  public String getPreferredLogContext() {
    return getClass().getName();
  }

  /**
   * Get the preferred nested diagnostic context to use when processing messages
   * for this component. Default implementation returns null.
   *
   * @return The preferred NDC for this component, or null if no context is
   *         required.
   */
  protected String getPreferredNDC() {
    return null;
  }

  @Override
  public void initialise() {
  }

  @Override
  public void shutdown() {
    try {
      processor.kill();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public Registry getPreferredRegistry(Registry parent) {
    return parent;
  }

  /**
   * Process an incoming message.
   *
   * @param msg The incoming message.
   */
  protected void processMessage(Message msg) {
    Logger.info("Unrecognised message type: " + msg);
  }

  /**
   * Process an incoming message immediately. If the message can be processed
   * quickly then this method should do so and return true. If the message may
   * take some time to process (e.g. if it is a sense message (for agents) or a
   * command message (for simulators) then this method should return false and the
   * message will be processed in a different thread via the
   * {@link #processMessage(Message)} method.
   *
   * @param msg The incoming message.
   * @return true If the message was processed immediately, false if it requires
   *         slower processing.
   */
  protected boolean processImmediately(Message msg) {
    if (msg instanceof Shutdown) {
      shutdown();
      return true;
    } else {
      return false;
    }
  }

  private class MessageProcessor extends WorkerThread {
    private BlockingQueue<Message> queue;

    MessageProcessor() {
      queue = new LinkedBlockingQueue<Message>();
    }

    void push(Message m) {
      queue.add(m);
    }

    @Override
    public boolean work() throws InterruptedException {
      String ndc = getPreferredNDC();
      if (ndc != null) {
        Logger.pushNDC(ndc);
      }
      try {
        Logger.trace("MessageProcessor working: " + queue.size() + " messages in the queue");
        Message msg = queue.take();
        Logger.trace("Next message: " + msg);
        AbstractComponent.this.processMessage(msg);
        return true;
      } finally {
        if (ndc != null) {
          Logger.popNDC();
        }
      }
    }
  }

  private class MessageListener implements ConnectionListener {
    @Override
    public void messageReceived(Connection c, Message msg) {
      String ndc = getPreferredNDC();
      if (ndc != null) {
        Logger.pushNDC(ndc);
      }
      try {
        if (!processImmediately(msg)) {
          processor.push(msg);
        }
      } finally {
        if (ndc != null) {
          Logger.popNDC();
        }
      }
    }
  }
}
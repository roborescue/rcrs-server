package rescuecore2.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.log.Logger;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.EntityIDRequest;
import rescuecore2.messages.control.EntityIDResponse;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSConnectError;
import rescuecore2.messages.control.KSConnectOK;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.SKAcknowledge;
import rescuecore2.messages.control.SKConnect;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

/**
 * Abstract base class for simulator implementations.
 *
 * @param <T> The subclass of WorldModel that this simulator understands.
 */
public abstract class AbstractSimulator<T extends WorldModel<? extends Entity>> extends AbstractComponent<T>
    implements Simulator {
  /**
   * The ID of this simulator.
   */
  protected int simulatorID;

  private int lastUpdateTime;

  private Map<Integer, List<EntityID>> idRequests;
  private int nextIDRequest;

  /**
   * Create a new AbstractSimulator.
   */
  protected AbstractSimulator() {
  }

  /**
   * Get this simulator's ID.
   *
   * @return The simulator ID.
   */
  public final int getSimulatorID() {
    return simulatorID;
  }

  @Override
  public void postConnect(Connection c, int id, Collection<Entity> entities, Config kernelConfig) {
    this.simulatorID = id;
    lastUpdateTime = 0;
    nextIDRequest = 0;
    idRequests = new HashMap<Integer, List<EntityID>>();
    super.postConnect(c, entities, kernelConfig);
  }

  @Override
  public void connect(Connection connection, RequestIDGenerator generator, Config config)
      throws ConnectionException, ComponentConnectionException, InterruptedException {
    this.config = config;
    int requestID = generator.generateRequestID();
    SKConnect connect = new SKConnect(requestID, 1, getName());
    CountDownLatch latch = new CountDownLatch(1);
    SimulatorConnectionListener l = new SimulatorConnectionListener(requestID, latch);
    connection.addConnectionListener(l);
    connection.sendMessage(connect);
    // Wait for a reply
    latch.await();
    l.testSuccess();
  }

  @Override
  public void shutdown() {
    super.shutdown();
  }

  /**
   * Handle a KSUpdate object from the server. The default implementation just
   * updates the world model.
   *
   * @param u The Update object.
   */
  protected void handleUpdate(KSUpdate u) {
    ChangeSet changes = u.getChangeSet();
    int time = u.getTime();
    if (time != lastUpdateTime + 1) {
      Logger.warn(
          "Received an unexpected update from the kernel. Last update: " + lastUpdateTime + ", this update: " + time);
    }
    lastUpdateTime = time;
    model.merge(changes);
  }

  /**
   * Handle a KSCommands object from the server. The default implementation tells
   * the kernel that nothing has changed.
   *
   * @param c The Commands object.
   */
  protected void handleCommands(KSCommands c) {
    ChangeSet changes = new ChangeSet();
    processCommands(c, changes);
    send(new SKUpdate(simulatorID, c.getTime(), changes));
  }

  /**
   * Process the commands from the server and populate a ChangeSet.
   *
   * @param c       The commands to process.
   * @param changes The ChangeSet to populate.
   */
  protected void processCommands(KSCommands c, ChangeSet changes) {
  }

  /**
   * Request some new entity IDs from the kernel.
   *
   * @param count The number to request.
   * @return A list of new entity IDs.
   */
  protected List<EntityID> requestNewEntityIDs(int count) throws InterruptedException {
    synchronized (idRequests) {
      int id = nextIDRequest++;
      Logger.debug("Requesting " + count + " new IDs: request number " + id);
      send(new EntityIDRequest(simulatorID, id, count));
      // Wait for a reply
      Integer key = id;
      while (!idRequests.containsKey(key)) {
        Logger.debug("Waiting for response");
        idRequests.wait();
      }
      List<EntityID> result = idRequests.get(key);
      idRequests.remove(key);
      return result;
    }
  }

  @Override
  protected void processMessage(Message msg) {
    if (msg instanceof KSUpdate) {
      KSUpdate u = (KSUpdate) msg;
      if (u.getTargetID() == simulatorID) {
        handleUpdate(u);
      }
    } else if (msg instanceof KSCommands) {
      KSCommands commands = (KSCommands) msg;
      if (commands.getTargetID() == simulatorID) {
        handleCommands(commands);
      }
    } else {
      super.processMessage(msg);
    }
  }

  @Override
  protected boolean processImmediately(Message msg) {
    if (msg instanceof EntityIDResponse) {
      EntityIDResponse resp = (EntityIDResponse) msg;
      Logger.debug("Received " + msg);
      if (resp.getSimulatorID() == simulatorID) {
        synchronized (idRequests) {
          Logger.debug("ID response: " + resp.getRequestID() + ", " + resp.getEntityIDs());
          idRequests.put(resp.getRequestID(), resp.getEntityIDs());
          idRequests.notifyAll();
        }
      }
      return true;
    } else {
      return super.processImmediately(msg);
    }
  }

  private class SimulatorConnectionListener implements ConnectionListener {
    private int requestID;
    private CountDownLatch latch;
    private ComponentConnectionException failureReason;

    public SimulatorConnectionListener(int requestID, CountDownLatch latch) {
      this.requestID = requestID;
      this.latch = latch;
      failureReason = null;
    }

    @Override
    public void messageReceived(Connection c, Message msg) {
      if (msg instanceof KSConnectOK) {
        handleConnectOK(c, (KSConnectOK) msg);
      }
      if (msg instanceof KSConnectError) {
        handleConnectError(c, (KSConnectError) msg);
      }
    }

    private void handleConnectOK(Connection c, KSConnectOK ok) {
      if (ok.getRequestID() == requestID) {
        c.removeConnectionListener(this);
        postConnect(c, ok.getSimulatorID(), ok.getEntities(), ok.getConfig());
        try {
          c.sendMessage(new SKAcknowledge(requestID, ok.getSimulatorID()));
        } catch (ConnectionException e) {
          failureReason = new ComponentConnectionException(e);
        }
        latch.countDown();
      }
    }

    private void handleConnectError(Connection c, KSConnectError error) {
      if (error.getRequestID() == requestID) {
        c.removeConnectionListener(this);
        failureReason = new ComponentConnectionException(error.getReason());
        latch.countDown();
      }
    }

    /**
     * Check if the connection succeeded and throw an exception if is has not.
     */
    void testSuccess() throws ComponentConnectionException {
      if (failureReason != null) {
        throw failureReason;
      }
    }
  }
}
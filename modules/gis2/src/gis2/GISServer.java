package gis2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import rescuecore2.Constants;
import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.TCPConnection;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.GKConnectOK;
import rescuecore2.messages.control.KGConnect;
import rescuecore2.messages.control.Shutdown;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
 * This class is used for starting a remote GIS server.
 */
public final class GISServer {
  private static final long WAIT_TIME = 1000;

  private ServerSocket server;
  private WorldModel<? extends Entity> world;
  private volatile boolean running;

  private static final Logger LOG = Logger.getLogger(GISServer.class);

  private GISServer(int port, WorldModel<? extends Entity> world) throws IOException {
    server = new ServerSocket(port);
    this.world = world;
    running = true;
  }

  /**
   * Start the GIS server.
   *
   * @param args Command line arguments: <-c config file>
   */
  public static void main(String[] args) {
    Config config = new Config();
    try {
      CommandLineOptions.processArgs(args, config);
      int port = config.getIntValue(Constants.GIS_PORT_NUMBER_KEY, Constants.DEFAULT_GIS_PORT_NUMBER);
      processJarFiles(config);
      GMLWorldModelCreator creator = new GMLWorldModelCreator();
      new GISServer(port, creator.buildWorldModel(config)).run();
      LOG.info("GIS server listening on port " + port);
    } catch (Exception e) {
      LOG.fatal("Error starting GIS server", e);
    }
  }

  private static void processJarFiles(Config config) throws IOException {
    LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
    processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
    processor.process();
  }

  /**
   * Run the GIS server.
   */
  public void run() {
    while (running) {
      try {
        Socket socket = server.accept();
        new ServerThread(socket).start();
      } catch (IOException e) {
        LOG.error("Error accepting connection", e);
        running = false;
      }
    }
  }

  private class ServerThread extends Thread implements ConnectionListener {
    private Socket socket;
    private boolean dead;

    public ServerThread(Socket socket) {
      this.socket = socket;
      dead = false;
    }

    @Override
    public void run() {
      TCPConnection c = null;
      try {
        c = new TCPConnection(socket);
      } catch (IOException e) {
        LOG.error("Error starting TCPConnection", e);
        return;
      }
      c.startup();
      c.addConnectionListener(this);
      synchronized (this) {
        while (!dead) {
          try {
            this.wait(WAIT_TIME);
          } catch (InterruptedException e) {
            dead = true;
          }
        }
      }
      c.shutdown();
    }

    @Override
    public void messageReceived(Connection c, Message msg) {
      if (msg instanceof KGConnect) {
        // Send a GKConnectOK
        try {
          c.sendMessage(new GKConnectOK(world.getAllEntities()));
        } catch (ConnectionException e) {
          LOG.fatal("Error sending message", e);
          die();
        }
      }
      if (msg instanceof Shutdown) {
        die();
      }
    }

    private void die() {
      synchronized (this) {
        dead = true;
        notifyAll();
      }
      running = false;
    }
  }
}
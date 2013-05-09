package kernel;



import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.dom4j.DocumentException;

import rescuecore2.Constants;
import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.scenario.Scenario;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.GKConnectOK;
import rescuecore2.messages.control.GKConnectError;
import rescuecore2.messages.control.KGConnect;
import rescuecore2.messages.control.KGAcknowledge;
import rescuecore2.log.Logger;

/**
 * A WorldModelCreator that talks to a remote GIS.
 */
public class RemoteGISWorldModelCreator implements WorldModelCreator {
	private int nextID;

	@Override
	public WorldModel<? extends Entity> buildWorldModel(Config config)
			throws KernelException {
		Logger.info("Connecting to remote GIS...");
		DefaultWorldModel<Entity> world = DefaultWorldModel.create();
		CountDownLatch latch = new CountDownLatch(1);
		int gisPort = config.getIntValue(Constants.GIS_PORT_NUMBER_KEY,
				Constants.DEFAULT_GIS_PORT_NUMBER);
		Connection conn;
		try {
			conn = new TCPConnection(gisPort);
			conn.addConnectionListener(new GISConnectionListener(latch, world));
			conn.startup();
			conn.sendMessage(new KGConnect(1));
		} catch (IOException e) {
			throw new KernelException("Couldn't connect to GIS", e);
		} catch (ConnectionException e) {
			throw new KernelException("Couldn't connect to GIS", e);
		}
		// Wait for a reply
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new KernelException("Interrupted while connecting to GIS", e);
		}
		conn.shutdown();
		return world;
	}

	@Override
	public String toString() {
		return "Remote GIS";
	}

	@Override
	public EntityID generateID() {
		synchronized (this) {
			return new EntityID(nextID++);
		}
	}

	/**
	 * Listener for the GIS connection.
	 */
	private class GISConnectionListener implements ConnectionListener {
		private CountDownLatch latch;
		private DefaultWorldModel<Entity> model;

		public GISConnectionListener(CountDownLatch latch,
				DefaultWorldModel<Entity> model) {
			this.latch = latch;
			this.model = model;
		}

		public void messageReceived(Connection c, Message m) {
			if (m instanceof GKConnectOK) {
				try {
					// Update the internal world model
					model.removeAllEntities();
					model.addEntities(((GKConnectOK) m).getEntities());
					// Send an acknowledgement
					c.sendMessage(new KGAcknowledge());
					Logger.info("GIS connected OK");
					// Trigger the countdown latch
					latch.countDown();
					nextID = 0;
					for (Entity next : model) {
						nextID = Math.max(nextID, next.getID().getValue());
					}
					++nextID;
				} catch (ConnectionException e) {
					Logger.error("RemoteGISWorldModelCreator.messageReceived",
							e);
				}
			}
			if (m instanceof GKConnectError) {
				Logger.error("Error connecting to remote GIS: "
						+ ((GKConnectError) m).getReason());
				latch.countDown();
			}
		}
	}

	@Override
	public Scenario getScenario(Config config) throws DocumentException{
		return null;// TODO Salim implement
	}
}

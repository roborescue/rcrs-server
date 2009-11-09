package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionException;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.VKConnect;
import rescuecore2.messages.control.VKAcknowledge;
import rescuecore2.messages.control.KVConnectOK;
import rescuecore2.messages.control.KVConnectError;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.config.Config;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
   Abstract base class for viewer implementations.
   @param <T> The subclass of Entity that this viewer understands.
 */
public abstract class AbstractViewer<T extends Entity> extends AbstractComponent<T> implements Viewer {
    /**
       The ID of this viewer.
    */
    protected int viewerID;

    private int lastUpdateTime;

    /**
       Create a new AbstractViewer.
     */
    protected AbstractViewer() {
    }

    /**
       Get this viewer's ID.
       @return The viewer ID.
     */
    public final int getViewerID() {
        return viewerID;
    }

    @Override
    public void postConnect(Connection c, int id, Collection<Entity> entities, Config kernelConfig) {
        super.postConnect(c, entities, kernelConfig);
        this.viewerID = id;
        c.addConnectionListener(new ViewerListener());
        lastUpdateTime = 0;
        postConnect();
    }

    @Override
    public void connect(Connection connection, RequestIDGenerator generator) throws ConnectionException, ComponentConnectionException, InterruptedException {
        int requestID = generator.generateRequestID();
        VKConnect connect = new VKConnect(requestID, 1, getName());
        CountDownLatch latch = new CountDownLatch(1);
        ViewerConnectionListener l = new ViewerConnectionListener(requestID, latch);
        connection.addConnectionListener(l);
        connection.sendMessage(connect);
        // Wait for a reply
        latch.await();
        l.testSuccess();
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
        ChangeSet changes = u.getChangeSet();
        int time = u.getTime();
        if (time != lastUpdateTime + 1) {
            System.out.println("WARNING: Recieved an unexpected update from the kernel. Last update: " + lastUpdateTime + ", this update: " + time);
        }
        lastUpdateTime = time;
        model.merge(changes);
    }

    /**
       Handle a Commands object from the server. The default implementation does nothing.
       @param c The Commands object.
     */
    protected void handleCommands(Commands c) {
    }

    private class ViewerConnectionListener implements ConnectionListener {
        private int requestID;
        private CountDownLatch latch;
        private ComponentConnectionException failureReason;

        public ViewerConnectionListener(int requestID, CountDownLatch latch) {
            this.requestID = requestID;
            this.latch = latch;
            failureReason = null;
        }

        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KVConnectOK) {
                handleConnectOK(c, (KVConnectOK)msg);
            }
            if (msg instanceof KVConnectError) {
                handleConnectError(c, (KVConnectError)msg);
            }
        }

        private void handleConnectOK(Connection c, KVConnectOK ok) {
            if (ok.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                postConnect(c, ok.getViewerID(), ok.getEntities(), ok.getConfig());
                try {
                    c.sendMessage(new VKAcknowledge(requestID, ok.getViewerID()));
                }
                catch (ConnectionException e) {
                    failureReason = new ComponentConnectionException(e);
                }
                latch.countDown();
            }
        }

        private void handleConnectError(Connection c, KVConnectError error) {
            if (error.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                failureReason = new ComponentConnectionException(error.getReason());
                latch.countDown();
            }
        }

        /**
           Check if the connection succeeded and throw an exception if is has not.
        */
        void testSuccess() throws ComponentConnectionException {
            if (failureReason != null) {
                throw failureReason;
            }
        }
    }

    private class ViewerListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof Update) {
                Update u = (Update)msg;
                if (u.getTargetID() == viewerID) {
                    handleUpdate(u);
                }
            }
            if (msg instanceof Commands) {
                Commands commands = (Commands)msg;
                if (commands.getTargetID() == viewerID) {
                    handleCommands(commands);
                }
            }
        }
    }
}
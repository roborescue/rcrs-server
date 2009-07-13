package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.VKConnect;
import rescuecore2.messages.control.VKAcknowledge;
import rescuecore2.messages.control.KVConnectOK;
import rescuecore2.messages.control.KVConnectError;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.worldmodel.Entity;

/**
   Abstract base class for viewer implementations.
   @param <T> The subclass of Entity that this viewer understands.
 */
public abstract class AbstractViewer<T extends Entity> extends AbstractComponent<T> implements Viewer {
    private int viewerID;

    /**
       Create a new AbstractViewer.
     */
    protected AbstractViewer() {
    }

    @Override
    protected Message createConnectMessage(int uniqueID) {
        return new VKConnect(uniqueID, 0);
    }

    @Override
    protected ConnectionListener createConnectionListener() {
        return new ViewerConnectionListener();
    }

    /**
       Get this viewer's ID.
       @return The viewer ID.
     */
    protected final int getViewerID() {
        return viewerID;
    }

    private void handleConnectOK(KVConnectOK ok) {
        if (!checkRequestID(ok.getRequestID())) {
            return;
        }
        model.removeAllEntities();
        model.merge(ok.getEntities());
        viewerID = ok.getViewerID();
        System.out.println("Viewer " + viewerID + " connected OK");
        // Send an acknowledge
        try {
            connection.sendMessage(new VKAcknowledge(ok.getRequestID(), viewerID));
            connectionSucceeded();
        }
        catch (ConnectionException e) {
            e.printStackTrace();
            connectionFailed(e.toString());
        }
    }

    private void handleConnectError(KVConnectError error) {
        if (!checkRequestID(error.getRequestID())) {
            return;
        }
        connectionFailed(error.getReason());
    }

    /**
       Handle an Update object from the server. The default implementation does nothing.
       @param u The Update object.
     */
    protected void handleUpdate(Update u) {
    }

    /**
       Handle a Commands object from the server. The default implementation does nothing.
       @param c The Commands object.
     */
    protected void handleCommands(Commands c) {
    }

    private class ViewerConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof Update) {
                handleUpdate((Update)msg);
            }
            if (msg instanceof Commands) {
                handleCommands((Commands)msg);
            }
            if (msg instanceof KVConnectOK) {
                handleConnectOK((KVConnectOK)msg);
            }
            if (msg instanceof KVConnectError) {
                handleConnectError((KVConnectError)msg);
            }
        }
    }
}
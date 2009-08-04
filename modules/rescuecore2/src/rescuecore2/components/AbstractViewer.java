package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;

/**
   Abstract base class for viewer implementations.
   @param <T> The subclass of Entity that this viewer understands.
 */
public abstract class AbstractViewer<T extends Entity> extends AbstractComponent<T> implements Viewer {
    /**
       The ID of this viewer.
    */
    protected int viewerID;

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
    public void postConnect(Connection c, int id, Collection<Entity> entities) {
        super.postConnect(c, entities);
        this.viewerID = id;
        c.addConnectionListener(new ViewerListener());
        postConnect();
    }

    /**
       Perform any post-connection work required before acknowledgement of the connection is made. The default implementation does nothing.
     */
    protected void postConnect() {
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

    private class ViewerListener implements ConnectionListener {
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
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
    public void postConnect(Connection c, int id, Collection<Entity> entities) {
        super.postConnect(c, entities);
        this.viewerID = id;
        c.addConnectionListener(new ViewerListener());
        lastUpdateTime = 0;
        postConnect();
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
        Collection<Entity> entities = u.getUpdatedEntities();
        int time = u.getTime();
        if (time != lastUpdateTime + 1) {
            System.out.println("WARNING: Recieved an unexpected update from the kernel. Last update: " + lastUpdateTime + ", this update: " + time);
        }
        lastUpdateTime = time;
        model.merge(entities);
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
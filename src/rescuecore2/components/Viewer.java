package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.worldmodel.Entity;

import java.util.Collection;

/**
   Sub-interface for Viewer components.
 */
public interface Viewer extends Component {
    /**
       Notification that this viewer has been connected to the kernel.
       @param c The connection to the kernel.
       @param viewerID The ID of this viewer.
       @param entities The set of Entities the kernel sent to this viewer on connection.
     */
    void postConnect(Connection c, int viewerID, Collection<Entity> entities);
}
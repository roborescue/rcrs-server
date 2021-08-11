package rescuecore2.components;

import java.util.Collection;
import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.worldmodel.Entity;

/**
 * Sub-interface for Viewer components.
 */
public interface Viewer extends Component {

  /**
   * Notification that this viewer has been connected to the kernel.
   *
   * @param c
   *          The connection to the kernel.
   * @param viewerID
   *          The ID of this viewer.
   * @param entities
   *          The set of Entities the kernel sent to this viewer on connection.
   * @param config
   *          The Config the kernel send to this agent on connection.
   */
  void postConnect( Connection c, int viewerID, Collection<Entity> entities,
      Config config );
}
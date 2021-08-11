package rescuecore2.components;

import java.util.Collection;
import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.worldmodel.Entity;

/**
 * Sub-interface for Simulator components.
 */
public interface Simulator extends Component {

  /**
   * Notification that this simulator has been connected to the kernel.
   *
   * @param c
   *          The connection to the kernel.
   * @param simID
   *          The ID of this simulator.
   * @param entities
   *          The set of Entities the kernel sent to this simulator on
   *          connection.
   * @param config
   *          The Config the kernel send to this simulator on connection.
   */
  void postConnect( Connection c, int simID, Collection<Entity> entities,
      Config config );
}
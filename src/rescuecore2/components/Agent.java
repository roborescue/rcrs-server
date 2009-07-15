package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;

/**
   Sub-interface for Agent components.
 */
public interface Agent extends Component {
    /**
       Get the list of entity IDs that this agent is willing to control.
       @return An array of entity IDs.
    */
    int[] getRequestedEntityIDs();

    /**
       Notification that this agent has been connected to the kernel.
       @param c The connection to the kernel.
       @param agentID The ID of the entity controlled by this agent.
       @param entities The set of Entities the kernel sent to this agent on connection.
     */
    void postConnect(Connection c, EntityID agentID, Collection<Entity> entities);
}
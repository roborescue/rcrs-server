package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.config.Config;

import java.util.Collection;

/**
   Sub-interface for Agent components.
 */
public interface Agent extends Component {
    /**
       Get the list of entity URNs that this agent is willing to control.
       @return An array of entity URNs.
    */
    int[] getRequestedEntityURNs();

    /**
       Notification that this agent has been connected to the kernel.
       @param c The connection to the kernel.
       @param agentID The ID of the entity controlled by this agent.
       @param entities The set of Entities the kernel sent to this agent on connection.
       @param config The Config the kernel send to this agent on connection.
     */
    void postConnect(Connection c, EntityID agentID, Collection<Entity> entities, Config config);

    /**
       Get the ID of the entity this agent controls. If the agent has not yet been connected to the kernel then null will be returned.
       @return The entity ID or null if this agent is not yet connected.
     */
    EntityID getID();
}

package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.KASense;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
   Abstract base class for agent implementations.
   @param <T> The subclass of Entity that this agent understands.
 */
public abstract class AbstractAgent<T extends Entity> extends AbstractComponent<T> implements Agent {
    /**
       The ID of the entity controlled by this agent.
     */
    protected EntityID entityID;

    /**
       Create a new AbstractAgent.
     */
    protected AbstractAgent() {
    }

    @Override
    public final void postConnect(Connection c, EntityID agentID, Collection<Entity> entities) {
        super.postConnect(c, entities);
        this.entityID = agentID;
        c.addConnectionListener(new AgentListener());
        postConnect();
    }

    /**
       Notification that a timestep has started.
       @param time The timestep.
       @param changed A list of entities that changed this timestep.
     */
    protected abstract void think(int time, List<EntityID> changed);

    /**
       Perform any post-connection work required before acknowledgement of the connection is made. The default implementation does nothing.
     */
    protected void postConnect() {
    }

    /**
       Process an incoming sense message. The default implementation updates the world model and calls {@link #think}. Subclasses should generally not override this method but instead implement the {@link #think} method.
       @param sense The sense message.
     */
    protected void processSense(KASense sense) {
        model.merge(sense.getUpdates());
        List<Entity> updates = sense.getUpdates();
        List<EntityID> changed = new ArrayList<EntityID>(updates.size());
        for (Entity next : updates) {
            changed.add(next.getID());
        }
        think(sense.getTime(), changed);
    }

    /**
       Get the entity controlled by this agent.
       @return The entity controlled by this agent.
     */
    protected T me() {
        if (entityID == null) {
            return null;
        }
        return model.getEntity(entityID);
    }

    private class AgentListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KASense) {
                KASense sense = (KASense)msg;
                if (!entityID.equals(sense.getAgentID())) {
                    return;
                }
                processSense(sense);
            }
        }
    }
}
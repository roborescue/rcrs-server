package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.AKConnect;
import rescuecore2.messages.control.AKAcknowledge;
import rescuecore2.messages.control.KAConnectOK;
import rescuecore2.messages.control.KAConnectError;
import rescuecore2.messages.control.KASense;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.ArrayList;

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
    protected Message createConnectMessage(int uniqueID) {
        return new AKConnect(uniqueID, 0, getRequestedEntityIDs());
    }

    @Override
    protected ConnectionListener createConnectionListener() {
        return new AgentConnectionListener();
    }

    /**
       Notification that a timestep has started.
       @param time The timestep.
       @param changed A list of entities that changed this timestep.
     */
    protected abstract void think(int time, List<EntityID> changed);

    /**
       Get the list of entity IDs that this agent is willing to control.
       @return An array of entity IDs.
    */
    protected abstract int[] getRequestedEntityIDs();

    /**
       Process an incoming sense message. This will be called after the world model has been updated. The default implementation calls {@link #think}. Subclasses should generally not override this method but instead implement the {@link #think} method.
       @param sense The sense message.
     */
    protected void processSense(KASense sense) {
        List<Entity> updates = sense.getUpdates();
        //        System.out.println("Agent " + entityID + " received " + updates.size() + " updates");
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

    private void handleSense(KASense sense) {
        if (!entityID.equals(sense.getAgentID())) {
            return;
        }
        //        System.out.println("Agent " + me() + " received " + sense);
        model.merge(sense.getUpdates());
        processSense(sense);
    }

    private void handleConnectOK(KAConnectOK ok) {
        if (!checkRequestID(ok.getRequestID())) {
            return;
        }
        //        System.out.println("Agent connected OK: " + ok);
        model.removeAllEntities();
        model.merge(ok.getEntities());
        entityID = ok.getAgentID();
        // Send an acknowledge
        try {
            connection.sendMessage(new AKAcknowledge(ok.getRequestID(), entityID));
            connectionSucceeded();
        }
        catch (ConnectionException e) {
            e.printStackTrace();
            connectionFailed(e.toString());
        }
    }

    private void handleConnectError(KAConnectError error) {
        if (!checkRequestID(error.getRequestID())) {
            return;
        }
        //        System.out.println("Error connecting agent: " + error);
        connectionFailed(error.getReason());
    }

    private class AgentConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KASense) {
                handleSense((KASense)msg);
            }
            if (msg instanceof KAConnectOK) {
                handleConnectOK((KAConnectOK)msg);
            }
            if (msg instanceof KAConnectError) {
                handleConnectError((KAConnectError)msg);
            }
        }
    }
}
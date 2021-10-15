package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionException;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KASense;
import rescuecore2.messages.control.AKConnect;
import rescuecore2.messages.control.AKAcknowledge;
import rescuecore2.messages.control.KAConnectOK;
import rescuecore2.messages.control.KAConnectError;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.config.Config;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
   Abstract base class for agent implementations.
   @param <T> The subclass of WorldModel that this agent understands.
   @param <E> The subclass of Entity that this agent wants to control.
 */
public abstract class AbstractAgent<T extends WorldModel<? extends Entity>, E extends Entity> extends AbstractComponent<T> implements Agent {
    /**
       The ID of the entity controlled by this agent.
     */
    private EntityID entityID;

    /**
       Create a new AbstractAgent.
     */
    protected AbstractAgent() {
        config = new Config();
    }

    @Override
    public final void postConnect(Connection c, EntityID agentID, Collection<Entity> entities, Config kernelConfig) {
        this.entityID = agentID;
        super.postConnect(c, entities, kernelConfig);
    }

    @Override
    public EntityID getID() {
        return entityID;
    }

    @Override
    public void connect(Connection connection, RequestIDGenerator generator, Config config) throws ConnectionException, ComponentConnectionException, InterruptedException {
        this.config = config;
        int requestID = generator.generateRequestID();
        AKConnect connect = new AKConnect(requestID, 2, getName(), getRequestedEntityURNs());
        CountDownLatch latch = new CountDownLatch(1);
        AgentConnectionListener l = new AgentConnectionListener(requestID, latch);
        connection.addConnectionListener(l);
        connection.sendMessage(connect);
        // Wait for a reply
        latch.await();
        l.testSuccess();
    }

    @Override
    protected void postConnect() {
        super.postConnect();
    }

    @Override
    protected String getPreferredNDC() {
        if (me() != null) {
            return me().toString();
        }
        return null;
    }

    /**
       Notification that a timestep has started.
       @param time The timestep.
       @param changes The set of changes observed this timestep.
       @param heard The set of communication messages this agent heard.
     */
    protected abstract void think(int time, ChangeSet changes, Collection<Command> heard);

    /**
       Process an incoming sense message. The default implementation updates the world model and calls {@link #think}. Subclasses should generally not override this method but instead implement the {@link #think} method.
       @param sense The sense message.
     */
    protected void processSense(KASense sense) {
        model.merge(sense.getChangeSet());
        Collection<Command> heard = sense.getHearing();
        think(sense.getTime(), sense.getChangeSet(), heard);
    }

    /**
       Get the entity controlled by this agent.
       @return The entity controlled by this agent.
     */
    @SuppressWarnings("unchecked")
    protected E me() {
        if (entityID == null) {
            return null;
        }
        if (model == null) {
            return null;
        }
        return (E)model.getEntity(entityID);
    }

    @Override
    protected void processMessage(Message msg) {
        if (msg instanceof KASense) {
            KASense sense = (KASense)msg;
            if (entityID.equals(sense.getAgentID())) {
                processSense(sense);
            }
        }
        else {
            super.processMessage(msg);
        }
    }

    private class AgentConnectionListener implements ConnectionListener {
        private int requestID;
        private CountDownLatch latch;
        private ComponentConnectionException failureReason;

        public AgentConnectionListener(int requestID, CountDownLatch latch) {
            this.requestID = requestID;
            this.latch = latch;
            failureReason = null;
        }

        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KAConnectOK) {
                handleConnectOK(c, (KAConnectOK)msg);
            }
            if (msg instanceof KAConnectError) {
                handleConnectError(c, (KAConnectError)msg);
            }
        }

        private void handleConnectOK(Connection c, KAConnectOK ok) {
            if (ok.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                postConnect(c, ok.getAgentID(), ok.getEntities(), ok.getConfig());
                try {
                    c.sendMessage(new AKAcknowledge(requestID, ok.getAgentID()));
                }
                catch (ConnectionException e) {
                    failureReason = new ComponentConnectionException(e);
                }
                latch.countDown();
            }
        }

        private void handleConnectError(Connection c, KAConnectError error) {
            if (error.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                failureReason = new ComponentConnectionException(error.getReason());
                latch.countDown();
            }
        }

        /**
           Check if the connection succeeded and throw an exception if is has not.
        */
        void testSuccess() throws ComponentConnectionException {
            if (failureReason != null) {
                throw failureReason;
            }
        }
    }
}

package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.messages.Message;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionException;

import java.util.Collection;
import java.util.HashSet;

/**
   Abstract base class for Agent implementations.
 */
public abstract class AbstractAgent implements Agent {
    private Entity entity;
    private Connection connection;
    private Collection<Command> commands;

    /**
       Construct a new abstract agent.
       @param entity The entity controlled by this agent.
       @param c The connection this agent is using.
     */
    protected AbstractAgent(Entity entity, Connection c) {
        this.entity = entity;
        this.connection = c;
        commands = new HashSet<Command>();
        c.addConnectionListener(new AgentConnectionListener());
    }

    @Override
    public Entity getControlledEntity() {
        return entity;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void shutdown() {
        connection.shutdown();
    }

    @Override
    public void send(Collection<? extends Message> messages) {
        if (!connection.isAlive()) {
            return;
        }
        try {
            connection.sendMessages(messages);
        }
        catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Command> getAgentCommands(int timestep) {
        Collection<Command> result;
        synchronized (commands) {
            result = new HashSet<Command>(commands);
            commands.clear();
        }
        return result;
    }

    @Override
    public String toString() {
        return connection.toString() + ": " + entity.toString();
    }

    /**
       Register an agent command received.
       @param c The command that was received.
     */
    protected void commandReceived(Command c) {
        synchronized (commands) {
            commands.add(c);
            commands.notifyAll();
        }
    }

    private class AgentConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof Command) {
                EntityID id = ((Command)msg).getAgentID();
                if (id.equals(getControlledEntity().getID())) {
                    commandReceived((Command)msg);
                }
            }
        }
    }
}
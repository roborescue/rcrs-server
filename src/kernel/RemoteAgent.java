package kernel;

import java.util.Collection;
import java.util.HashSet;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.connection.ConnectionException;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

/**
   An agent that uses a connection to send and receive commands.
 */
public abstract class RemoteAgent<T extends Entity> extends AbstractAgent<T> {
    private Connection connection;
    private Collection<Command> commands;

    /**
       Construct a new remote agent.
       @param entity The entity controlled by this agent.
       @param c The connection this agent is using.
     */
    public RemoteAgent(T entity, Connection c) {
        super(entity);
        connection = c;
        commands = new HashSet<Command>();
        c.addConnectionListener(new RemoteAgentConnectionListener());
    }

    /**
       Get this agent's connection.
       @return The connection to the agent.
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void shutdown() {
        connection.shutdown();
    }

    @Override
    public void sendMessages(Collection<? extends Message> messages) {
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
        return connection.toString() + ": " + getControlledEntity().toString();
    }

    private void commandReceived(Command c) {
        synchronized (commands) {
            commands.add(c);
        }
    }

    private class RemoteAgentConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof Command) {
                EntityID id = ((Command)msg).getAgentID();
                if (id.equals(getControlledEntity().getID())) {
                    commandReceived((Command)msg);
                }
            }
        }
    }
}
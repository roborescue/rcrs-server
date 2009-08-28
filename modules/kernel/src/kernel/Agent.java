package kernel;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KASense;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   This class is the kernel interface to an agent.
 */
public class Agent extends AbstractComponent {
    private Entity entity;
    private Map<Integer, Collection<Command>> commands;

    /**
       Construct an agent.
       @param e The entity controlled by the agent.
       @param c The connection to the agent.
     */
    public Agent(Entity e, Connection c) {
        super(c);
        this.entity = e;
        commands = new HashMap<Integer, Collection<Command>>();
        c.addConnectionListener(new AgentConnectionListener());
    }

    @Override
    public String toString() {
        return entity.getType() + " " + entity.getID();
    }

    /**
       Get the entity controlled by this agent.
       @return The entity controlled by this agent.
     */
    public Entity getControlledEntity() {
        return entity;
    }

    /**
       Get all agent commands since the last call to this method.
       @param timestep The current timestep.
       @return A collection of messages representing the commands
     */
    public Collection<Command> getAgentCommands(int timestep) {
        Collection<Command> result;
        synchronized (commands) {
            result = commands.get(timestep);
            if (result == null) {
                result = new HashSet<Command>();
                commands.put(timestep, result);
            }
        }
        return result;
    }

    /**
       Notify the of a perception update.
       @param time The current timestep.
       @param visible The set of visible entitites.
       @param communication The set of communication messages that the agent perceived.
     */
    public void sendPerceptionUpdate(int time, Collection<? extends Entity> visible, Collection<? extends Message> communication) {
        KASense sense = new KASense(getControlledEntity().getID(), time, visible);
        Collection<Message> all = new ArrayList<Message>();
        all.add(sense);
        all.addAll(communication);
        send(all);
    }

    /**
       Register an agent command received.
       @param c The command that was received.
     */
    protected void commandReceived(Command c) {
        // Check that the command is for the right agent
        if (!c.getAgentID().equals(entity.getID())) {
            System.out.println("Ignoring bogus command: Agent " + entity.getID() + " tried to send a command for agent " + c.getAgentID());
            return;
        }
        int time = c.getTime();
        synchronized (commands) {
            Collection<Command> result = commands.get(time);
            if (result == null) {
                result = new HashSet<Command>();
                commands.put(time, result);
            }
            result.add(c);
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
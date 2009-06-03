package kernel;

import java.util.Collection;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;

/**
   This class is the kernel interface to an agent.
 */
public interface Agent {
    /**
       Send a set of messages to the agent.
       @param messages The messages to send.
     */
    void send(Collection<? extends Message> messages);

    /**
       Get the entity controlled by this agent.
       @return The entity controlled by this agent.
     */
    Entity getControlledEntity();

    /**
       Get all agent commands since the last call to this method.
       @param timestep The current timestep.
       @return A collection of messages representing the commands
     */
    Collection<Command> getAgentCommands(int timestep);

    /**
       Notify the of a perception update.
       @param time The current timestep.
       @param visible The set of visible entitites.
       @param communication The set of communication messages that the agent perceived.
     */
    void sendPerceptionUpdate(int time, Collection<? extends Entity> visible, Collection<? extends Message> communication);

    /**
       Shut this agent down.
     */
    void shutdown();

    /**
       Get this agent's connection.
       @return The connection to the agent.
     */
    Connection getConnection();
}
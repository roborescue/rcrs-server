package kernel;

import java.util.Set;
import java.util.Collection;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   This class is the kernel interface to an agent.
   @param <T> The subclass of Entity that this Agent understands.
 */
public interface Agent<T extends Entity> {
    /**
       Get the entity controlled by this agent.
       @return The entity controlled by this agent.
     */
    T getControlledEntity();

    /**
       Get all agent commands since the last call to this method.
       @param timestep The current timestep.
       @return A collection of messages representing the commands
     */
    Collection<Command> getAgentCommands(int timestep);

    /**
       Shut this agent down.
     */
    void shutdown();

    /**
       Notify the of a perception update.
       @param time The current timestep.
       @param visible The set of visible entitites.
     */
    void sendPerceptionUpdate(int time, Collection<? extends T> visible);

    /**
       Send a set of messages to the agent.
       @param messages The messages to send.
     */
    void sendMessages(Collection<? extends Message> messages);
}
package kernel;

import java.util.Collection;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

/**
   A model of communication. Implementers are responsible for determining what communications are received by each agent in the world.
 */
public interface CommunicationModel {
    /**
       Process a set of agent commands and work out what communications a particular agent can hear.
       @param agent The agent-controlled entity.
       @param agentCommands The set of all agent commands last timestep.
       @return A collection of communication update messages to be sent to the agent.
     */
    Collection<Message> process(Agent agent, Collection<Command> agentCommands);
}
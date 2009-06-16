package kernel;

import java.util.Collection;
import java.util.Map;

import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

/**
   A model of communication. Implementers are responsible for determining what communications are received by each agent in the world.
 */
public interface CommunicationModel {
    /**
       Process a set of agent commands and work out what communications each agent can hear.
       @param agents The set of agents in the system.
       @param agentCommands The set of all agent commands last timestep.
       @return A map from Agent to collection of communication update messages to be sent to the agent. If an agent can hear nothing then it need not be included in this map.
     */
    Map<Agent, Collection<Message>> process(Collection<Agent> agents, Collection<Command> agentCommands);

    /**
       Notify this perception object of the current time.
       @param timestep The current timestep.
    */
    void setTime(int timestep);
}
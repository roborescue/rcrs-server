package kernel;

import java.util.Collection;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   A model of communication. Implementers are responsible for determining what communications are received by each agent in the world.
 */
public interface CommunicationModel {
    /**
       Initialise this communication model.
       @param config The kernel configuration.
       @param world The world model.
    */
    void initialise(Config config, WorldModel<? extends Entity> world);

    /**
       Process a set of agent commands and work out what communications each agent can hear.
       @param time The current time.
       @param agentCommands The set of all agent commands this timestep.
     */
    void process(int time, Collection<? extends Command> agentCommands);

    /**
       Get the set of hear commands an agent can hear.
       @param agent The agent controlled entity.
       @return Set set of hear commands the agent can hear.
    */
    Collection<Command> getHearing(Entity agent);
}

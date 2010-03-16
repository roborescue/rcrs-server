package kernel;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;

import java.util.Collection;

/**
   The CommandCollector gathers commands from agents.
*/
public interface CommandCollector {
    /**
       Collect all commands from agents.
       @param agents The agents.
       @param timestep The timestep.
       @return All agent commands.
       @throws InterruptedException If the thread is interrupted.
    */
    Collection<Command> getAgentCommands(Collection<AgentProxy> agents, int timestep) throws InterruptedException;

    /**
       Initialise this command collector.
       @param config The kernel configuration.
    */
    void initialise(Config config);
}

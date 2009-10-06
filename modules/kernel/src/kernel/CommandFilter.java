package kernel;

import java.util.Collection;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;

/**
   An interface for allowing the kernel to filter out agent commands.
 */
public interface CommandFilter {
    /**
       Initialise this filter.
       @param config The kernel configuration.
       @param kernel The kernel.
    */
    void initialise(Config config, Kernel kernel);

    /**
       Filter a set of agent commands. Any illegal commands should be removed from the given collection.
       @param commands The commands to filter. This collection should be modified to remove any illegal commands.
       @param agent The agent that sent the commands.
     */
    void filter(Collection<Command> commands, AgentProxy agent);
}
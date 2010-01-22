package kernel;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;

import java.util.Collection;

/**
   A CommandFilter that ignores agent commands for some number of timesteps.
 */
public class FrozenAgentsCommandFilter implements CommandFilter {
    private static final String IGNORE_AGENT_COMMANDS_KEY = "kernel.agents.ignoreuntil";

    private int freezeTime;

    @Override
    public void initialise(Config config) {
        freezeTime = config.getIntValue(IGNORE_AGENT_COMMANDS_KEY, 0);
    }

    @Override
    public void filter(Collection<Command> commands, KernelState state) {
        int time = state.getTime();
        if (time < freezeTime) {
            commands.clear();
        }
    }
}
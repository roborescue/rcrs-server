package kernel;

import java.util.Collection;

import rescuecore2.commands.Command;
import rescuecore2.config.Config;
import rescuecore2.log.Logger;

/**
 * A CommandFilter that ignores agent commands for some number of timesteps.
 */
public class FrozenAgentsCommandFilter implements CommandFilter {
  private int freezeTime;

  @Override
  public void initialise(Config config) {
    freezeTime = config.getIntValue(KernelConstants.IGNORE_AGENT_COMMANDS_KEY, 0);
  }

  @Override
  public void filter(Collection<Command> commands, KernelState state) {
    int time = state.getTime();
    if (time < freezeTime) {
      Logger.info("Ignoring early commands: " + time + " < " + freezeTime);
      commands.clear();
    }
  }
}

package kernel;

import java.util.Collection;

import rescuecore2.commands.Command;
import rescuecore2.config.Config;

/**
 * The CommandCollector gathers commands from agents.
 */
public interface CommandCollector {
  /**
   * Collect all commands from agents.
   *
   * @param agents   The agents.
   * @param timestep The timestep.
   * @return All agent commands.
   * @throws InterruptedException If the thread is interrupted.
   */
  Collection<Command> getAgentCommands(Collection<AgentProxy> agents, int timestep) throws InterruptedException;

  /**
   * Initialise this command collector.
   *
   * @param config The kernel configuration.
   */
  void initialise(Config config);
}

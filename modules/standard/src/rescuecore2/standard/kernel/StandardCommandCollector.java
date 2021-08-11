package rescuecore2.standard.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import kernel.AgentProxy;
import kernel.CommandCollector;

import rescuecore2.commands.Command;
import rescuecore2.config.Config;
import rescuecore2.log.Logger;
import rescuecore2.standard.commands.AKClear;
import rescuecore2.standard.commands.AKClearArea;
import rescuecore2.standard.commands.AKExtinguish;
import rescuecore2.standard.commands.AKLoad;
import rescuecore2.standard.commands.AKMove;
import rescuecore2.standard.commands.AKRescue;
import rescuecore2.standard.commands.AKRest;
import rescuecore2.standard.commands.AKUnload;

/**
 * A CommandCollector that will wait until a non-communication command has been
 * received from each agent.
 */
public class StandardCommandCollector implements CommandCollector {

  private static final long WAIT_TIME = 100;

  @Override
  public void initialise(Config config) {
  }

  @Override
  public Collection<Command> getAgentCommands(Collection<AgentProxy> agents, int timestep) throws InterruptedException {
    Set<AgentProxy> waiting = new HashSet<AgentProxy>(agents);
    while (!waiting.isEmpty()) {
      for (AgentProxy next : agents) {
        Collection<Command> commands = next.getAgentCommands(timestep);
        for (Command c : commands) {
          if (isTriggerCommand(c)) {
            Logger.debug(next + " sent a trigger command");
            waiting.remove(next);
          }
        }
      }
      Logger.info(this + " waiting for commands from " + waiting.size() + " agents");
      Thread.sleep(WAIT_TIME);
    }
    Collection<Command> result = new ArrayList<Command>();
    for (AgentProxy next : agents) {
      result.addAll(next.getAgentCommands(timestep));
    }
    Logger.trace(this + " returning " + result.size() + " commands");
    return result;
  }

  @Override
  public String toString() {
    return "Standard command collector";
  }

  private boolean isTriggerCommand(Command c) {
    return ((c instanceof AKMove) || (c instanceof AKRest) || (c instanceof AKExtinguish) || (c instanceof AKClear)
        || (c instanceof AKClearArea) || (c instanceof AKRescue) || (c instanceof AKLoad) || (c instanceof AKUnload));
  }
}

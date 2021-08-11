package rescuecore2.standard.kernel.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kernel.CommandFilter;
import kernel.KernelState;

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
import rescuecore2.worldmodel.EntityID;

/**
 * A CommandFilter that ensures only one non-communication command is allowed
 * per agent.
 */
public class SingleCommandFilter implements CommandFilter {

  @Override
  public void initialise(Config config) {
  }

  @Override
  public void filter(Collection<Command> commands, KernelState state) {
    Set<EntityID> sent = new HashSet<EntityID>();
    Iterator<Command> it = commands.iterator();
    while (it.hasNext()) {
      Command command = it.next();
      if (filterable(command)) {
        EntityID sender = command.getAgentID();
        if (sent.contains(sender)) {
          it.remove();
          Logger.info("Ignoring command " + command + ": Agent " + sender + " already sent a command");
        } else {
          sent.add(sender);
          Logger.debug(sender + " sent command " + command);
        }
      }
    }
  }

  private boolean filterable(Command command) {
    return (command instanceof AKRest) || (command instanceof AKMove) || (command instanceof AKLoad)
        || (command instanceof AKUnload) || (command instanceof AKRescue) || (command instanceof AKClear)
        || (command instanceof AKClearArea) || (command instanceof AKExtinguish);
  }
}
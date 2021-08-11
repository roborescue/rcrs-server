package rescuecore2.standard.kernel.filter;

import kernel.AbstractCommandFilter;
import kernel.KernelState;

import rescuecore2.commands.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.commands.AKSay;
import rescuecore2.standard.commands.AKSpeak;
import rescuecore2.standard.commands.AKSubscribe;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * A CommandFilter that discards commands from buried agents.
 */
public class BuriedAgentsCommandFilter extends AbstractCommandFilter {

  @Override
  protected boolean allowed(Command command, KernelState state) {
    EntityID id = command.getAgentID();
    Entity e = state.getWorldModel().getEntity(id);
    if ((command instanceof AKSubscribe) || (command instanceof AKSpeak) || (command instanceof AKSay)) {
      return true;
    }
    if (e instanceof Human) {
      Human h = (Human) e;
      if (h.isBuriednessDefined() && h.getBuriedness() > 0) {
        Logger.info("Ignoring command " + command + ": Agent " + h + " is buried");
        return false;
      }
    }
    return true;
  }
}
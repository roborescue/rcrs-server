package rescuecore2.standard.kernel;

import kernel.AbstractCommandFilter;
import kernel.KernelState;

import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityConstants;

/**
   A CommandFilter that discards commands from dead agents.
 */
public class DeadAgentsCommandFilter extends AbstractCommandFilter {
    @Override
    protected boolean allowed(Command c, KernelState state) {
        EntityID id = c.getAgentID();
        Entity e = state.getWorldModel().getEntity(id);
        if (e instanceof Human) {
            Human h = (Human)e;
            if (h.isHPDefined() && h.getHP() <= 0) {
                Logger.info("Ignoring command " + c + ": Agent " + h + " is dead");
                return false;
            }
        }
        if (e instanceof Building) {
            Building b = (Building)e;
            if (b.isFierynessDefined() && b.getFierynessEnum() == StandardEntityConstants.Fieryness.BURNT_OUT) {
                Logger.info("Ignoring command " + c + ": Centre " + b + " is burnt out");
                return false;
            }
        }
        return true;
    }
}

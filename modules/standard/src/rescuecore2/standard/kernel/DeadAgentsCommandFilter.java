package rescuecore2.standard.kernel;

import kernel.AbstractCommandFilter;
import kernel.KernelState;

import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Human;

/**
   A CommandFilter that discards commands from dead agents.
 */
public class DeadAgentsCommandFilter extends AbstractCommandFilter {
    @Override
    protected boolean allowed(Command c, KernelState state) {
        EntityID id = c.getAgentID();
        Entity e = state.getWorldModel().getEntity(id);
        if ((e instanceof Human) && ((Human)e).isHPDefined() && ((Human)e).getHP() <= 0) {
            Logger.info("Ignoring command from dead agent " + e);
            return false;
        }
        return true;
    }
}
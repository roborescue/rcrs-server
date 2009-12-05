package rescuecore2.standard.kernel;

import kernel.AbstractCommandFilter;
import kernel.KernelState;

import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Human;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A CommandFilter that discards commands from dead agents.
 */
public class DeadAgentsCommandFilter extends AbstractCommandFilter {
    private static final Log LOG = LogFactory.getLog(DeadAgentsCommandFilter.class);

    @Override
    protected boolean allowed(Command c, KernelState state) {
        EntityID id = c.getAgentID();
        Entity e = state.getWorldModel().getEntity(id);
        if ((e instanceof Human) && ((Human)e).isHPDefined() && ((Human)e).getHP() <= 0) {
            LOG.info("Ignoring command from dead agent " + e);
            return false;
        }
        return true;
    }
}
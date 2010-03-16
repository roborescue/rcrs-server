package rescuecore2.standard.kernel;

import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;
import rescuecore2.log.Logger;

import kernel.KernelState;
import kernel.TerminationCondition;

/**
   A TerminationCondition that terminates the simulation once all Civilians are either rescued or dead.
*/
public class CiviliansDeadOrRescuedTerminationCondition implements TerminationCondition {
    @Override
    public void initialise(Config config) {
    }

    @Override
    public boolean shouldStop(KernelState k) {
        for (Entity next : k.getWorldModel()) {
            if (next instanceof Civilian) {
                Civilian c = (Civilian)next;
                if (!c.isHPDefined() || !c.isDamageDefined() || !c.isBuriednessDefined() || !c.isPositionDefined()) {
                    // Civilian with unknown state: we're not ready to stop.
                    return false;
                }
                if (c.getHP() <= 0) {
                    // Dead - ignore
                    continue;
                }
                if (c.getDamage() > 0 || c.getBuriedness() > 0) {
                    // Hurt or buried - keep running
                    return false;
                }
                Entity position = k.getWorldModel().getEntity(c.getPosition());
                if (!(position instanceof Refuge)) {
                    // Alive but not in a refuge - keep running
                    return false;
                }
            }
        }
        // Found no reason to keep going so stop.
        Logger.debug("CiviliansDeadOrRescuedTerminationCondition fired");
        return true;
    }

    @Override
    public String toString() {
        return "All civilians rescued or dead";
    }
}

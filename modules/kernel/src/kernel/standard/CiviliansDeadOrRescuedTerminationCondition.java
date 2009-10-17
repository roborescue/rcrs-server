package kernel.standard;

import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;

import kernel.Kernel;
import kernel.TerminationCondition;

/**
   A TerminationCondition that terminates the simulation once all Civilians are either rescued or dead.
*/
public class CiviliansDeadOrRescuedTerminationCondition implements TerminationCondition {
    @Override
    public void initialise(Config config) {
    }

    @Override
    public boolean shouldStop(Kernel k) {
        for (Entity next : k.getWorldModel()) {
            if (next instanceof Civilian) {
                Civilian c = (Civilian)next;
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
        return true;
    }

    @Override
    public String toString() {
        return "All civilians rescued or dead";
    }
}
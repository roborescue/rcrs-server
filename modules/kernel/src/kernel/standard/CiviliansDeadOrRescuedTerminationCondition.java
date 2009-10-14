package kernel.standard;

import rescuecore2.standard.entities.Civilian;
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
                if (c.getHP() > 0 && (c.getDamage() > 0 || c.getBuriedness() > 0)) {
                    // Alive but hurt or buried
                    return false;
                }
            }
        }
        return true;
    }
}
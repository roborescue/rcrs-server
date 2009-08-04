package kernel.standard;

import kernel.Kernel;
import kernel.Agent;
import kernel.CommandFilter;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.standard.entities.Human;

import java.util.Collection;

/**
   A CommandFilter that discards commands from dead agents.
 */
public class DeadAgentsCommandFilter implements CommandFilter {
    @Override
    public void initialise(Config config, Kernel kernel) {
    }

    @Override
    public void filter(Collection<Command> commands, Agent agent) {
        Entity e = agent.getControlledEntity();
        if ((e instanceof Human) && ((Human)e).getHP() <= 0) {
            System.out.println("Ignoring commands from dead agent " + e);
            commands.clear();
        }
    }
}
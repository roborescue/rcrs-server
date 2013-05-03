package rescuecore2.standard.kernel;

import kernel.CommandFilter;
import kernel.KernelState;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;

import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKExtinguish;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

/**
   A CommandFilter that ensures only one non-communication command is allowed per agent.
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
            Command c = it.next();
            if (filterable(c)) {
                EntityID sender = c.getAgentID();
                if (sent.contains(sender)) {
                    it.remove();
                    Logger.info("Ignoring command " + c + ": Agent " + sender + " already sent a command");
                }
                else {
                    sent.add(sender);
                    Logger.debug(sender + " sent command " + c);
                }
            }
        }
    }

    private boolean filterable(Command c) {
        return (c instanceof AKRest)
        || (c instanceof AKMove)
        || (c instanceof AKLoad)
        || (c instanceof AKUnload)
        || (c instanceof AKRescue)
        || (c instanceof AKClear)
        || (c instanceof AKClearArea)
        || (c instanceof AKExtinguish);
    }
}

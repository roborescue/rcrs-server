package kernel;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;

/**
   A CommandFilter that chains together a set of filters.
 */
public class ChainedCommandFilter implements CommandFilter {
    private List<CommandFilter> filters;

    /**
       Construct an empty ChainedCommandFilter.
     */
    public ChainedCommandFilter() {
        filters = new ArrayList<CommandFilter>();
    }

    /**
       Add a CommandFilter to the chain.
       @param filter The filter to add.
     */
    public void addFilter(CommandFilter filter) {
        filters.add(filter);
    }

    /**
       Remove a CommandFilter from the chain.
       @param filter The filter to remove.
     */
    public void removeFilter(CommandFilter filter) {
        filters.remove(filter);
    }

    @Override
    public void initialise(Config config) {
        for (CommandFilter next : filters) {
            next.initialise(config);
        }
    }

    @Override
    public void filter(Collection<Command> commands, KernelState state) {
        for (CommandFilter next : filters) {
            next.filter(commands, state);
        }
    }
}

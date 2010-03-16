package kernel;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

/**
   A CommandCollector that waits for any of a set of child CommandCollectors to return a result.
*/
public class CompositeCommandCollector implements CommandCollector {
    private Set<CommandCollector> children;

    /**
       Construct a CompositeCommandCollector with no children.
    */
    public CompositeCommandCollector() {
        children = new HashSet<CommandCollector>();
    }

    @Override
    public void initialise(Config config) {
        for (CommandCollector next : children) {
            next.initialise(config);
        }
    }

    @Override
    public Collection<Command> getAgentCommands(Collection<AgentProxy> agents, int timestep) throws InterruptedException {
        if (agents.size() == 0) {
            return new HashSet<Command>();
        }
        ExecutorCompletionService<Collection<Command>> service = new ExecutorCompletionService<Collection<Command>>(Executors.newFixedThreadPool(agents.size()));
        Set<Future<Collection<Command>>> futures = new HashSet<Future<Collection<Command>>>();
        for (CommandCollector next : children) {
            futures.add(service.submit(new ChildCommandsFetcher(next, agents, timestep)));
        }
        try {
            int size = children.size();
            for (int i = 0; i < size; ++i) {
                try {
                    return service.take().get();
                }
                catch (ExecutionException e) {
                    Logger.error("Error while getting agent commands", e);
                }
            }
        }
        finally {
            for (Future<Collection<Command>> next : futures) {
                next.cancel(true);
            }
        }
        return new HashSet<Command>();
    }

    /**
       Add a child command collector.
       @param child The child to add.
    */
    public void addCommandCollector(CommandCollector child) {
        children.add(child);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("CompositeCommandCollector [");
        for (Iterator<CommandCollector> it = children.iterator(); it.hasNext();) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }

    private static final class ChildCommandsFetcher implements Callable<Collection<Command>> {
        private CommandCollector child;
        private Collection<AgentProxy> agents;
        private int timestep;

        ChildCommandsFetcher(CommandCollector child, Collection<AgentProxy> agents, int timestep) {
            this.child = child;
            this.agents = agents;
            this.timestep = timestep;
        }

        @Override
        public Collection<Command> call() throws Exception {
            return child.getAgentCommands(agents, timestep);
        }
    }
}

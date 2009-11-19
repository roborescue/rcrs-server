package kernel;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;

import java.util.Collection;
import java.util.HashSet;

/**
   A CommandCollector that waits for a certain amount of time before returning agent commands.
*/
public class TimedCommandCollector implements CommandCollector {
    private static final int DEFAULT_TIME = 1000;
    private static final String TIME_KEY = "kernel.agents.think-time";

    private long time;

    @Override
    public void initialise(Config config) {
        time = config.getIntValue(TIME_KEY, DEFAULT_TIME);
    }

    @Override
    public Collection<Command> getAgentCommands(Collection<AgentProxy> agents, int timestep) throws InterruptedException {
        long now = System.currentTimeMillis();
        long end = now + time;
        while (now < end) {
            long diff = end - now;
            System.out.println(this + " waiting for " + diff + "ms");
            Thread.sleep(diff);
            now = System.currentTimeMillis();
        }
        Collection<Command> result = new HashSet<Command>();
        for (AgentProxy next : agents) {
            Collection<Command> commands = next.getAgentCommands(timestep);
            result.addAll(commands);
        }
        System.out.println(this + " returning " + result.size() + " commands");
        return result;
    }

    @Override
    public String toString() {
        return "Timed command collector";
    }
}
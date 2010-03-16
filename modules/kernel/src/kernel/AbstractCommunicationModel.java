package kernel;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;

import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.collections.LazyMap;

/**
   Abstract base class for communication models.
 */
public abstract class AbstractCommunicationModel implements CommunicationModel {
    private Map<Entity, Set<Command>> hearing;

    /**
       Construct an AbstractCommunicationModel.
    */
    public AbstractCommunicationModel() {
        hearing = new LazyMap<Entity, Set<Command>>() {
            @Override
            public Set<Command> createValue() {
                return new HashSet<Command>();
            }
        };
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        hearing.clear();
    }

    @Override
    public void process(int time, Collection<? extends Command> agentCommands) {
        hearing.clear();
    }

    @Override
    public Collection<Command> getHearing(Entity agent) {
        return hearing.get(agent);
    }

    /**
       Register a set of heard messages for an agent.
       @param agent The agent.
       @param c The messages heard.
    */
    protected void addHearing(Entity agent, Command... c) {
        addHearing(agent, Arrays.asList(c));
    }

    /**
       Register a set of heard messages for an agent.
       @param agent The agent.
       @param c The messages heard.
    */
    protected void addHearing(Entity agent, Collection<? extends Command> c) {
        hearing.get(agent).addAll(c);
    }
}

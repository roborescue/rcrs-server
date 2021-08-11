package kernel;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rescuecore2.commands.Command;
import rescuecore2.config.Config;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
 * Abstract base class for communication models.
 */
public abstract class AbstractCommunicationModel implements CommunicationModel {
  private Map<Entity, List<Command>> hearing;

  /**
   * Construct an AbstractCommunicationModel.
   */
  public AbstractCommunicationModel() {
    hearing = new LazyMap<Entity, List<Command>>() {
      @Override
      public List<Command> createValue() {
        return new LinkedList<Command>();
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
   * Register a set of heard messages for an agent.
   *
   * @param agent The agent.
   * @param c     The messages heard.
   */
  protected void addHearing(Entity agent, Command... c) {
    addHearing(agent, Arrays.asList(c));
  }

  /**
   * Register a set of heard messages for an agent.
   *
   * @param agent The agent.
   * @param c     The messages heard.
   */
  protected void addHearing(Entity agent, Collection<? extends Command> c) {
    hearing.get(agent).addAll(c);
  }
}

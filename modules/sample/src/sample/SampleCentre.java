package sample;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.log4j.Logger;

import rescuecore2.commands.Command;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;

/**
 * A sample centre agent.
 */
public class SampleCentre extends StandardAgent<Building> {

  private static final Logger LOG = Logger.getLogger(SampleCentre.class);

  @Override
  public String toString() {
    return "Sample centre";
  }

  @Override
  protected void think(int time, ChangeSet changed, Collection<Command> heard) {
    if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
      // Subscribe to channels 1 and 2
      sendSubscribe(time, 1, 2);
    }
    for (Command next : heard) {
      LOG.debug("Heard " + next);
    }
    sendRest(time);
  }

  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.FIRE_STATION, StandardEntityURN.AMBULANCE_CENTRE,
        StandardEntityURN.POLICE_OFFICE);
  }
}

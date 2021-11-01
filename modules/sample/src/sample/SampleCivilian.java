package sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import rescuecore2.messages.Command;
import rescuecore2.registry.FilterEntityFactory;
import rescuecore2.registry.FilterPropertyFactory;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * A sample civilian agent.
 */
public class SampleCivilian extends AbstractSampleAgent<Civilian> {

  private static final Logger LOG = Logger.getLogger(SampleCivilian.class);
  private static final double DEFAULT_HELP_PROBABILITY = 0.1;
  private static final double DEFAULT_OUCH_PROBABILITY = 0.1;
  private static final int DEFAULT_CONSCIOUS_THRESHOLD = 2500;

  private static final String HELP_PROBABILITY_KEY = "civilian.help.probability";
  private static final String OUCH_PROBABILITY_KEY = "civilian.ouch.probability";
  private static final String CONSCIOUS_THRESHOLD_KEY = "civilian.conscious.threshold";

  private static final String OUCH = "Ouch";
  private static final String HELP = "Help";

  private double helpProbability;
  private double ouchProbability;
  private int consciousThreshold;

  @Override
  public String toString() {
    return "Sample civilian";
  }

  @Override
  protected void postConnect() {
    super.postConnect();
    // model.indexClass(StandardEntityURN.REFUGE);
    helpProbability = config.getFloatValue(HELP_PROBABILITY_KEY, DEFAULT_HELP_PROBABILITY);
    ouchProbability = config.getFloatValue(OUCH_PROBABILITY_KEY, DEFAULT_OUCH_PROBABILITY);
    consciousThreshold = config.getIntValue(CONSCIOUS_THRESHOLD_KEY, DEFAULT_CONSCIOUS_THRESHOLD);
    LOG.info("Civilian " + getID() + " connected");
    Civilian me = me();
    // Remove all entities except me
    model.removeAllEntities();
    model.addEntity(me);
  }

  @Override
  public Registry getPreferredRegistry(Registry parent) {
    // Return a registry that filters out buildings and civilians
    Registry result = new Registry("SampleCivilian filter registry", super.getPreferredRegistry(parent));
    Set<Integer> entityURNs = new HashSet<>();
    entityURNs.add(StandardEntityURN.BUILDING.getURNId());
    entityURNs.add(StandardEntityURN.REFUGE.getURNId());
    entityURNs.add(StandardEntityURN.HYDRANT.getURNId());
    entityURNs.add(StandardEntityURN.GAS_STATION.getURNId());
    entityURNs.add(StandardEntityURN.ROAD.getURNId());
    entityURNs.add(StandardEntityURN.CIVILIAN.getURNId());
    Set<Integer> propertyURNs = new HashSet<>();
    propertyURNs.add(StandardPropertyURN.X.getURNId());
    propertyURNs.add(StandardPropertyURN.Y.getURNId());
    propertyURNs.add(StandardPropertyURN.EDGES.getURNId());
    propertyURNs.add(StandardPropertyURN.DAMAGE.getURNId());
    propertyURNs.add(StandardPropertyURN.BURIEDNESS.getURNId());
    propertyURNs.add(StandardPropertyURN.HP.getURNId());
    propertyURNs.add(StandardPropertyURN.POSITION.getURNId());
    result.registerFactory(new FilterEntityFactory(StandardEntityFactory.INSTANCE, entityURNs, true));
    result.registerFactory(new FilterPropertyFactory(StandardPropertyFactory.INSTANCE, propertyURNs, true));
    return result;
  }

  @Override
  protected void think(int time, ChangeSet changed, Collection<Command> heard) {
    // If we're not hurt or buried run for a refuge!

    Civilian me = me();
    // Remove all entities except me
    model.removeAllEntities();
    model.addEntity(me);
    int damage = me.isDamageDefined() ? me.getDamage() : 0;
    int hp = me.isHPDefined() ? me.getHP() : 0;
    int buriedness = me.isBuriednessDefined() ? me.getBuriedness() : 0;
    if (hp <= 0 || hp < consciousThreshold) {
      // Unconscious (or dead): do nothing
      LOG.info("Unconscious or dead");
      sendRest(time);
      return;
    }
    if (damage > 0 && random.nextDouble() < ouchProbability) {
      LOG.info("Shouting in pain");
      say(OUCH, time);
    }
    if (buriedness > 0 && random.nextDouble() < helpProbability) {
      LOG.info("Calling for help");
      say(HELP, time);
    }

    if (damage == 0 && buriedness == 0) {
      // Run for the refuge
      List<EntityID> path = search.breadthFirstSearchForCivilian(me().getPosition(), refugeIDs);
      if (path != null) {
        LOG.info("Heading for a refuge");
        sendMove(time, path);
        return;
      } else {
        LOG.info("Moving to road");
        if (model.getEntity(me().getPosition()) instanceof Road)
          sendRest(time);
        else
          sendMove(time, nearestRoad());
        return;
      }
    }
    LOG.info("Not moving: damage = " + damage + ", buriedness = " + buriedness);
    sendRest(time);
  }

  protected List<EntityID> nearestRoad() {
    int maxPathLength = 20;
    List<EntityID> result = new ArrayList<EntityID>(maxPathLength);
    Set<EntityID> seen = new HashSet<EntityID>();
    EntityID current = ((Human) me()).getPosition();

    for (int i = 0; i < maxPathLength; ++i) {
      result.add(current);
      seen.add(current);
      Area area = (Area) model.getEntity(current);
      if (area instanceof Road)
        break;
      if (area == null) {
        System.err.println("My position=" + current + " is null??? " + me());
        break;
      }
      List<EntityID> possible = new ArrayList<EntityID>(area.getNeighbours());
      Collections.shuffle(possible, random);
      boolean found = false;
      for (EntityID next : possible) {
        if (seen.contains(next)) {
          continue;
        }
        current = next;
        found = true;
        break;
      }
      if (!found) {
        // We reached a dead-end.
        break;
      }
    }
    return result;
  }

  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.CIVILIAN);
  }

  @Override
  protected boolean shouldIndex() {
    return false;
  }

  private void say(String message, int time) {
    try {
      if (useSpeak) {
        sendSpeak(time, 0, message.getBytes("UTF-8"));
      } else {
        sendSay(time, message.getBytes("UTF-8"));
      }
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException("This should not have happened!", e);
    }
  }
}
package rescuecore2.standard.components;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import rescuecore2.components.AbstractAgent;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.standard.messages.AKSay;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.messages.AKSubscribe;
import rescuecore2.standard.messages.AKTell;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.worldmodel.EntityID;

/**
 * Abstract base class for standard agents.
 *
 * @param <E> The subclass of StandardEntity that this agent wants to control.
 */
public abstract class StandardAgent<E extends StandardEntity> extends AbstractAgent<StandardWorldModel, E> {
  @Override
  public final int[] getRequestedEntityURNs() {
    EnumSet<StandardEntityURN> set = getRequestedEntityURNsEnum();
    int[] result = new int[set.size()];
    int i = 0;
    for (StandardEntityURN next : set) {
      result[i++] = next.getURNId();
    }
    return result;
  }

  /**
   * Get an EnumSet containing requested entity URNs.
   *
   * @return An EnumSet containing requested entity URNs.
   */
  protected abstract EnumSet<StandardEntityURN> getRequestedEntityURNsEnum();

  @Override
  protected StandardWorldModel createWorldModel() {
    return new StandardWorldModel();
  }

  @Override
  protected void postConnect() {
    super.postConnect();
    if (shouldIndex()) {
      model.index();
    }
  }

  /**
   * Send a rest command to the kernel.
   *
   * @param time The current time.
   */
  protected void sendRest(int time) {
    send(new AKRest(getID(), time));
  }

  /**
   * Send a move command to the kernel.
   *
   * @param time The current time.
   * @param path The path to send.
   */
  protected void sendMove(int time, List<EntityID> path) {
    send(new AKMove(getID(), time, path));
  }

  /**
   * Send a move command to the kernel.
   *
   * @param time  The current time.
   * @param path  The path to send.
   * @param destX The destination X coordinate.
   * @param destY The destination Y coordinate.
   */
  protected void sendMove(int time, List<EntityID> path, int destX, int destY) {
    send(new AKMove(getID(), time, path, destX, destY));
  }

  /**
   * Send an extinguish command to the kernel.
   *
   * @param time   The current time.
   * @param target The target building.
   * @param water  The amount of water to use.
   */
  protected void sendExtinguish(int time, EntityID target, int water) {
    send(new AKExtinguish(getID(), time, target, water));
  }

  /**
   * Send a clear command to the kernel.
   *
   * @param time   The current time.
   * @param target The target road.
   */
  protected void sendClear(int time, EntityID target) {
    send(new AKClear(getID(), time, target));
  }

  /**
   * Send a clear command to the kernel.
   *
   * @param time  The current time.
   * @param destX The destination X coordinate to clear.
   * @param destY The destination Y coordinate to clear.
   */
  protected void sendClear(int time, int destX, int destY) {
    send(new AKClearArea(getID(), time, destX, destY));
  }

  /**
   * Send a rescue command to the kernel.
   *
   * @param time   The current time.
   * @param target The target human.
   */
  protected void sendRescue(int time, EntityID target) {
    send(new AKRescue(getID(), time, target));
  }

  /**
   * Send a load command to the kernel.
   *
   * @param time   The current time.
   * @param target The target human.
   */
  protected void sendLoad(int time, EntityID target) {
    send(new AKLoad(getID(), time, target));
  }

  /**
   * Send an unload command to the kernel.
   *
   * @param time The current time.
   */
  protected void sendUnload(int time) {
    send(new AKUnload(getID(), time));
  }

  /**
   * Send a speak command to the kernel.
   *
   * @param time    The current time.
   * @param channel The channel to speak on.
   * @param data    The data to send.
   */
  protected void sendSpeak(int time, int channel, byte[] data) {
    send(new AKSpeak(getID(), time, channel, data));
  }

  /**
   * Send a subscribe command to the kernel.
   *
   * @param time     The current time.
   * @param channels The channels to subscribe to.
   */
  protected void sendSubscribe(int time, int... channels) {
    send(new AKSubscribe(getID(), time, channels));
  }

  /**
   * Send a say command to the kernel.
   *
   * @param time The current time.
   * @param data The data to send.
   */
  protected void sendSay(int time, byte[] data) {
    send(new AKSay(getID(), time, data));
  }

  /**
   * Send a tell command to the kernel.
   *
   * @param time The current time.
   * @param data The data to send.
   */
  protected void sendTell(int time, byte[] data) {
    send(new AKTell(getID(), time, data));
  }

  /**
   * Get a list of all refuges in the world.
   *
   * @return All refuges.
   */
  protected List<Refuge> getRefuges() {
    List<Refuge> result = new ArrayList<Refuge>();
    for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.REFUGE)) {
      if (next instanceof Refuge) {
        result.add((Refuge) next);
      }
    }
    return result;
  }

  /**
   * Get the location of the entity controlled by this agent.
   *
   * @return The location of the entity controlled by this agent.
   */
  protected StandardEntity location() {
    E me = me();
    if (me instanceof Human) {
      return ((Human) me).getPosition(model);
    }
    return me;
  }

  /**
   * Should the world model be automatically indexed?
   *
   * @return True if the world model should be automatically indexed, false
   *         otherwise. Default implementation returns true.
   */
  protected boolean shouldIndex() {
    return true;
  }
}

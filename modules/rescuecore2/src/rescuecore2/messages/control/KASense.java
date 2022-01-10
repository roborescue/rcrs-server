package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.Command;
import rescuecore2.messages.components.ChangeSetComponent;
import rescuecore2.messages.components.CommandListComponent;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * A message for signalling a perception update for an agent.
 */
public class KASense extends AbstractMessage {
  private EntityIDComponent agentID;
  private IntComponent time;
  private ChangeSetComponent updates;
  private CommandListComponent hear;

  /**
   * A KASense message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KASense(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A populated KASense message.
   *
   * @param agentID The ID of the Entity that is receiving the update.
   * @param time    The timestep of the simulation.
   * @param changes All changes that the agent can perceive.
   * @param hear    The messages that the agent can hear.
   */
  public KASense(EntityID agentID, int time, ChangeSet changes, Collection<? extends Command> hear) {
    this();
    this.agentID.setValue(agentID);
    this.time.setValue(time);
    this.updates.setChangeSet(changes);
    this.hear.setCommands(hear);
  }

  private KASense() {
    super(ControlMessageURN.KA_SENSE);
    agentID = new EntityIDComponent(ControlMessageComponentURN.AgentID);
    time = new IntComponent(ControlMessageComponentURN.Time);
    updates = new ChangeSetComponent(ControlMessageComponentURN.Updates);
    hear = new CommandListComponent(ControlMessageComponentURN.Hearing);
    addMessageComponent(agentID);
    addMessageComponent(time);
    addMessageComponent(updates);
    addMessageComponent(hear);
  }

  public KASense(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the ID of the agent.
   *
   * @return The agent ID.
   */
  public EntityID getAgentID() {
    return agentID.getValue();
  }

  /**
   * Get the time.
   *
   * @return The time.
   */
  public int getTime() {
    return time.getValue();
  }

  /**
   * Get the changed entities.
   *
   * @return The ChangeSet.
   */
  public ChangeSet getChangeSet() {
    return updates.getChangeSet();
  }

  /**
   * Get the messages the agent can hear.
   *
   * @return The agent messages.
   */
  public Collection<Command> getHearing() {
    return hear.getCommands();
  }
}

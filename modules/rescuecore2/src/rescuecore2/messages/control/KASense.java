package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import rescuecore2.commands.Command;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.ChangeSetProto;
import rescuecore2.messages.control.ControlMessageProto.CommandProto;
import rescuecore2.messages.control.ControlMessageProto.KASenseProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyMapProto;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A message for signalling a perception update for an agent.
 */
public class KASense extends AbstractMessage {

  private EntityID agentID;
  private int time;
  private ChangeSet changes;
  private List<Command> hear;

  /**
   * A KASense message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KASense(InputStream in) throws IOException {
    super(ControlMessageURN.KA_SENSE.toString());
    this.read(in);
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
    super(ControlMessageURN.KA_SENSE.toString());
    this.agentID = agentID;
    this.time = time;
    this.changes = changes;
    this.hear = new ArrayList<Command>();
    this.hear.addAll(hear);
  }

  /**
   * Get the ID of the agent.
   *
   * @return The agent ID.
   */
  public EntityID getAgentID() {
    return this.agentID;
  }

  /**
   * Get the time.
   *
   * @return The time.
   */
  public int getTime() {
    return this.time;
  }

  /**
   * Get the changed entities.
   *
   * @return The ChangeSet.
   */
  public ChangeSet getChanges() {
    return this.changes;
  }

  /**
   * Get the messages the agent can hear.
   *
   * @return The agent messages.
   */
  public Collection<Command> getHearing() {
    return this.hear;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KASenseProto.Builder kaSenseBuilder = KASenseProto.newBuilder().setAgentID(this.agentID.getValue())
        .setTime(this.time);

    kaSenseBuilder.setChanges(MsgProtoBuf.setChangeSetProto(this.changes));

    for (Command command : this.hear) {
      CommandProto commandProto = MsgProtoBuf.setCommandProto(command);
      kaSenseBuilder.addHears(commandProto);
    }

    KASenseProto kaSense = kaSenseBuilder.build();
    kaSense.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KASenseProto kaSense = KASenseProto.parseFrom(in);

    this.agentID = new EntityID(kaSense.getAgentID());
    this.time = kaSense.getTime();

    this.changes = new ChangeSet();
    ChangeSetProto changeSetProto = kaSense.getChanges();

    // Add changed entities and properties
    Map<Integer, PropertyMapProto> changesMap = changeSetProto.getChangesMap();
    Map<Integer, String> entitiesURN = changeSetProto.getEntitiesURNsMap();
    for (Integer entityIDProto : changesMap.keySet()) {
      EntityID entityID = new EntityID(entityIDProto);
      String urn = entitiesURN.get(entityIDProto);

      PropertyMapProto propertyMapProto = changesMap.get(entityIDProto);
      for (String propertyURN : propertyMapProto.getPropertyMap().keySet()) {
        Property<?> property = Registry.getCurrentRegistry().createProperty(propertyURN);

        if (property != null) {
          List<Object> fields = MsgProtoBuf.setPropertyFields(propertyMapProto.getPropertyMap().get(propertyURN));

          property.setFields(fields);

          this.changes.addChange(entityID, urn, property);
        }
      }
    }

    // Add deleted entities
    for (Integer entityID : changeSetProto.getDeletesList()) {
      this.changes.entityDeleted(new EntityID(entityID));
    }

    this.hear = new ArrayList<Command>();
    for (CommandProto commandProto : kaSense.getHearsList()) {
      Command command = Registry.getCurrentRegistry().createCommand(commandProto.getUrn());

      Map<String, Object> fields = MsgProtoBuf.setCommandFields(commandProto);

      command.setFields(fields);

      this.hear.add(command);
    }
  }
}
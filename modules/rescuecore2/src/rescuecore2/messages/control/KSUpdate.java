package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.ChangeSetProto;
import rescuecore2.messages.control.ControlMessageProto.KSUpdateProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyMapProto;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A broadcast update from the kernel.
 */
public class KSUpdate extends AbstractMessage {

  private int simID;
  private int time;
  private ChangeSet changes;

  /**
   * A KSUpdate message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KSUpdate(InputStream in) throws IOException {
    super(ControlMessageURN.KS_UPDATE.toString());
    this.read(in);
  }

  /**
   * A populated KSUpdate message.
   *
   * @param simID   The id of the simulator receiving the update.
   * @param time    The timestep of the simulation.
   * @param changes The changeset.
   */
  public KSUpdate(int simID, int time, ChangeSet changes) {
    super(ControlMessageURN.KS_UPDATE.toString());
    this.simID = simID;
    this.time = time;
    this.changes = changes;
  }

  /**
   * Get the id of the component that this message is addressed to.
   *
   * @return The ID of the target component.
   */
  public int getSimulatorID() {
    return this.simID;
  }

  /**
   * Get the time of the simulation.
   *
   * @return The simulation time.
   */
  public int getTime() {
    return this.time;
  }

  /**
   * Get the list of changes.
   *
   * @return The changes.
   */
  public ChangeSet getChanges() {
    return this.changes;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KSUpdateProto.Builder ksUpdateBuilder = KSUpdateProto.newBuilder().setSimID(this.simID).setTime(this.time);

    ksUpdateBuilder.setChanges(MsgProtoBuf.setChangeSetProto(this.changes));

    KSUpdateProto ksUpdate = ksUpdateBuilder.build();
    ksUpdate.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KSUpdateProto ksUpdate = KSUpdateProto.parseFrom(in);

    this.simID = ksUpdate.getSimID();
    this.time = ksUpdate.getTime();

    this.changes = new ChangeSet();
    ChangeSetProto changeSetProto = ksUpdate.getChanges();

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
  }
}
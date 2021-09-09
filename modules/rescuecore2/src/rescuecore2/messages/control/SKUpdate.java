package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.ChangeSetProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyMapProto;
import rescuecore2.messages.control.ControlMessageProto.SKUpdateProto;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A message for sending updates from a simulator to the kernel.
 */
public class SKUpdate extends AbstractMessage {

  private int simID;
  private int time;
  private ChangeSet changes;

  /**
   * An SKUpdate message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public SKUpdate(InputStream in) throws IOException {
    super(ControlMessageURN.SK_UPDATE.toString());
    this.read(in);
  }

  /**
   * SKUpdate message with a specific ID and data component.
   *
   * @param simID   The id of the simulator sending the update.
   * @param time    The timestep this update refers to.
   * @param changes The changeset.
   */
  public SKUpdate(int simID, int time, ChangeSet changes) {
    super(ControlMessageURN.SK_UPDATE.toString());
    this.simID = simID;
    this.time = time;
    this.changes = changes;
  }

  /**
   * Get the ID of the simulator that is acknowledging the connection.
   *
   * @return The simulator ID component.
   */
  public int getSimulatorID() {
    return this.simID;
  }

  /**
   * Get the list of changes.
   *
   * @return The ChangeSet.
   */
  public ChangeSet getChanges() {
    return this.changes;
  }

  /**
   * Get the timestep this update is for.
   *
   * @return The timestep.
   */
  public int getTime() {
    return this.time;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    SKUpdateProto.Builder skUpdateBuilder = SKUpdateProto.newBuilder().setSimID(this.simID).setTime(this.time);
    skUpdateBuilder.setChanges(MsgProtoBuf.setChangeSetProto(this.changes));
    SKUpdateProto skUpdate = skUpdateBuilder.build();
    skUpdate.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    SKUpdateProto skUpdate = SKUpdateProto.parseFrom(in);
    this.simID = skUpdate.getSimID();
    this.time = skUpdate.getTime();
    this.changes = MsgProtoBuf.setChangeSet(skUpdate.getChanges());
  }
}
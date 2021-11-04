package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.ChangeSetComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.ChangeSet;

/**
 * A broadcast update from the kernel.
 */
public class KSUpdate extends AbstractMessage {
  private IntComponent id;
  private IntComponent time;
  private ChangeSetComponent changes;

  /**
   * A KSUpdate message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KSUpdate(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A populated KSUpdate message.
   *
   * @param id      The id of the simulator receiving the update.
   * @param time    The timestep of the simulation.
   * @param changes The changeset.
   */
  public KSUpdate(int id, int time, ChangeSet changes) {
    this();
    this.id.setValue(id);
    this.time.setValue(time);
    this.changes.setChangeSet(changes);
  }

  private KSUpdate() {
    super(ControlMessageURN.KS_UPDATE);
    id = new IntComponent(ControlMessageComponentURN.ID);
    time = new IntComponent(ControlMessageComponentURN.Time);
    changes = new ChangeSetComponent(ControlMessageComponentURN.Changes);
    addMessageComponent(id);
    addMessageComponent(time);
    addMessageComponent(changes);
  }

  public KSUpdate(MessageProto proto) {
    this();
    fromMessageProto(proto);
  }

  /**
   * Get the id of the component that this message is addressed to.
   *
   * @return The ID of the target component.
   */
  public int getTargetID() {
    return id.getValue();
  }

  /**
   * Get the time of the simulation.
   *
   * @return The simulation time.
   */
  public int getTime() {
    return time.getValue();
  }

  /**
   * Get the list of changes.
   *
   * @return The changes.
   */
  public ChangeSet getChangeSet() {
    return changes.getChangeSet();
  }
}

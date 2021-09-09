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
import rescuecore2.messages.control.ControlMessageProto.KVTimestepProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyMapProto;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A message containing a timestep summary. This is sent from the kernel to all
 * viewers.
 */
public class KVTimestep extends AbstractMessage {

  private int viewerID;
  private int time;
  private List<Command> commands;
  private ChangeSet changes;

  /**
   * A KVTimestep message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KVTimestep(InputStream in) throws IOException {
    super(ControlMessageURN.KV_TIMESTEP.toString());
    read(in);
  }

  /**
   * A populated KVTimestep message.
   *
   * @param viewerID The id of the viewer receiving the update.
   * @param time     The timestep of the simulation.
   * @param commands All agent Commands.
   * @param changes  Summary of changes during the timestep.
   */
  public KVTimestep(int viewerID, int time, Collection<? extends Command> commands, ChangeSet changes) {
    super(ControlMessageURN.KV_TIMESTEP.toString());
    this.viewerID = viewerID;
    this.time = time;
    this.commands = new ArrayList<Command>();
    this.commands.addAll(commands);
    this.changes = changes;
  }

  /**
   * Get the id of the component that this message is addressed to.
   *
   * @return The ID of the target component.
   */
  public int getViewerID() {
    return this.viewerID;
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
   * Get the list of agent commands.
   *
   * @return The agent commands.
   */
  public List<Command> getCommands() {
    return this.commands;
  }

  /**
   * Get the list of changes.
   *
   * @return The changes.
   */
  public ChangeSet getChangeSet() {
    return this.changes;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KVTimestepProto.Builder kvTimestepBuilder = KVTimestepProto.newBuilder().setViewerID(this.viewerID)
        .setTime(this.time);

    for (Command command : this.commands) {
      kvTimestepBuilder.addCommands(MsgProtoBuf.setCommandProto(command));
    }

    kvTimestepBuilder.setChanges(MsgProtoBuf.setChangeSetProto(this.changes));

    KVTimestepProto kvTimestep = kvTimestepBuilder.build();
    kvTimestep.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KVTimestepProto kvTimestep = KVTimestepProto.parseFrom(in);

    this.viewerID = kvTimestep.getViewerID();
    this.time = kvTimestep.getTime();

    this.commands = new ArrayList<Command>();
    for (CommandProto commandProto : kvTimestep.getCommandsList()) {
      this.commands.add(MsgProtoBuf.setCommand(commandProto));
    }

    this.changes = MsgProtoBuf.setChangeSet(kvTimestep.getChanges());
  }
}
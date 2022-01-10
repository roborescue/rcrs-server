package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.Command;
import rescuecore2.messages.components.ChangeSetComponent;
import rescuecore2.messages.components.CommandListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.ChangeSet;

/**
 * A message containing a timestep summary. This is sent from the kernel to all
 * viewers.
 */
public class KVTimestep extends AbstractMessage {
  private IntComponent id;
  private IntComponent time;
  private CommandListComponent commands;
  private ChangeSetComponent changes;

  /**
   * A KVTimestep message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KVTimestep(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A populated KVTimestep message.
   *
   * @param id       The id of the viewer receiving the update.
   * @param time     The timestep of the simulation.
   * @param commands All agent Commands.
   * @param changes  Summary of changes during the timestep.
   */
  public KVTimestep(int id, int time, Collection<? extends Command> commands, ChangeSet changes) {
    this();
    this.id.setValue(id);
    this.time.setValue(time);
    this.commands.setCommands(commands);
    this.changes.setChangeSet(changes);
  }

  private KVTimestep() {
    super(ControlMessageURN.KV_TIMESTEP);
    id = new IntComponent(ControlMessageComponentURN.ID);
    time = new IntComponent(ControlMessageComponentURN.Time);
    commands = new CommandListComponent(ControlMessageComponentURN.Commands);
    changes = new ChangeSetComponent(ControlMessageComponentURN.Changes);
    addMessageComponent(id);
    addMessageComponent(time);
    addMessageComponent(commands);
    addMessageComponent(changes);
  }

  public KVTimestep(MessageProto proto) {
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
   * Get the list of agent commands.
   *
   * @return The agent commands.
   */
  public List<Command> getCommands() {
    return commands.getCommands();
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

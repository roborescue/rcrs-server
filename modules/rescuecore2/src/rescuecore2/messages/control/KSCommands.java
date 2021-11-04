package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.Command;
import rescuecore2.messages.components.CommandListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message containing a list of agent commands. This is sent from the kernel
 * to all simulators.
 */
public class KSCommands extends AbstractMessage {
  private IntComponent id;
  private IntComponent time;
  private CommandListComponent commands;

  /**
   * A KSCommands message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KSCommands(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * A populated KSCommands message.
   *
   * @param id       The id of the simulator receiving the update.
   * @param time     The timestep of the simulation.
   * @param commands All agent Commands.
   */
  public KSCommands(int id, int time, Collection<? extends Command> commands) {
    this();
    this.id.setValue(id);
    this.time.setValue(time);
    this.commands.setCommands(commands);
  }

  private KSCommands() {
    super(ControlMessageURN.KS_COMMANDS);
    id = new IntComponent(ControlMessageComponentURN.ID);
    time = new IntComponent(ControlMessageComponentURN.Time);
    commands = new CommandListComponent(ControlMessageComponentURN.Commands);
    addMessageComponent(id);
    addMessageComponent(time);
    addMessageComponent(commands);
  }

  public KSCommands(MessageProto proto) {
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
}

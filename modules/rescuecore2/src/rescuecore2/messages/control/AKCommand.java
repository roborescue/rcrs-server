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
import rescuecore2.messages.control.ControlMessageProto.AKCommandProto;
import rescuecore2.messages.control.ControlMessageProto.CommandProto;
import rescuecore2.registry.Registry;

/**
 * A message for sending commands from an agent to the kernel.
 */
public class AKCommand extends AbstractMessage {

  private List<Command> command;

  /**
   * An AKCommand message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public AKCommand(InputStream in) throws IOException {
    super(ControlMessageURN.AK_COMMAND.toString());
    this.read(in);
  }

  /**
   * A populated AKCommand message.
   *
   * @param command The commands that the agent generate.
   */
  public AKCommand(Command command) {
    super(ControlMessageURN.AK_COMMAND.toString());
    this.command = new ArrayList<Command>();
    this.command.add(command);
  }

  /**
   * A populated AKCommand message.
   *
   * @param command The commands that the agent generate.
   */
  public AKCommand(Collection<? extends Command> command) {
    super(ControlMessageURN.AK_COMMAND.toString());
    this.command = new ArrayList<Command>();
    this.command.addAll(command);
  }

  /**
   * Get the command.
   *
   * @return The command.
   */
  public Collection<Command> getCommand() {
    return this.command;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    AKCommandProto.Builder akCommandBuilder = AKCommandProto.newBuilder();

    for (Command command : this.command) {
      CommandProto commandProto = MsgProtoBuf.setCommandProto(command);
      akCommandBuilder.addCommands(commandProto);
    }

    AKCommandProto akCommand = akCommandBuilder.build();

    akCommand.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    AKCommandProto akCommand = AKCommandProto.parseFrom(in);

    this.command = new ArrayList<Command>();

    for (CommandProto commandProto : akCommand.getCommandsList()) {
      Command command = Registry.getCurrentRegistry().createCommand(commandProto.getUrn());

      Map<String, Object> fields = MsgProtoBuf.setCommandFields(commandProto);

      command.setFields(fields);

      this.command.add(command);
    }
  }
}
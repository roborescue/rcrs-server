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
import rescuecore2.messages.control.ControlMessageProto.CommandProto;
import rescuecore2.messages.control.ControlMessageProto.KSCommandsProto;
import rescuecore2.registry.Registry;

/**
 * A message containing a list of agent commands. This is sent from the kernel
 * to all simulators.
 */
public class KSCommands extends AbstractMessage {

  private int simID;
  private int time;
  private List<Command> commands;

  /**
   * A KSCommands message that populates its data from a stream.
   *
   * @param in The InputStream to read.
   * @throws IOException If there is a problem reading the stream.
   */
  public KSCommands(InputStream in) throws IOException {
    super(ControlMessageURN.KS_COMMANDS.toString());
    this.read(in);
  }

  /**
   * A populated KSCommands message.
   *
   * @param simID    The id of the simulator receiving the update.
   * @param time     The timestep of the simulation.
   * @param commands All agent Commands.
   */
  public KSCommands(int simID, int time, Collection<? extends Command> commands) {
    super(ControlMessageURN.KS_COMMANDS.toString());
    this.simID = simID;
    this.time = time;
    this.commands = new ArrayList<Command>();
    this.commands.addAll(commands);
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
   * Get the list of agent commands.
   *
   * @return The agent commands.
   */
  public List<Command> getCommands() {
    return this.commands;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KSCommandsProto.Builder ksCommandsBuilder = KSCommandsProto.newBuilder().setSimID(this.simID).setTime(this.time);

    for (Command command : this.commands) {
      CommandProto commandProto = MsgProtoBuf.setCommandProto(command);
      ksCommandsBuilder.addCommands(commandProto);
    }

    KSCommandsProto ksCommand = ksCommandsBuilder.build();
    ksCommand.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KSCommandsProto ksCommands = KSCommandsProto.parseFrom(in);

    this.simID = ksCommands.getSimID();
    this.time = ksCommands.getTime();

    this.commands = new ArrayList<Command>();
    for (CommandProto commandProto : ksCommands.getCommandsList()) {
      Command command = Registry.getCurrentRegistry().createCommand(commandProto.getUrn());

      Map<String, Object> fields = MsgProtoBuf.setCommandFields(commandProto);

      command.setFields(fields);

      this.commands.add(command);
    }
  }
}
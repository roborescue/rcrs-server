package rescuecore2.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.commands.Command;
import rescuecore2.messages.control.ControlMessageProto.CommandLogProto;
import rescuecore2.messages.control.ControlMessageProto.CommandProto;
import rescuecore2.messages.control.MsgProtoBuf;

/**
 * A commands record.
 */
public class CommandsRecord implements LogRecord {

  private int                 time;
  private Collection<Command> commands;


  /**
   * Construct a new CommandsRecord.
   *
   * @param time
   *          The timestep of this commands record.
   * @param commands
   *          The set of agent commands.
   */
  public CommandsRecord( int time, Collection<Command> commands ) {
    this.time = time;
    this.commands = commands;
  }


  /**
   * Construct a new CommandsRecord and read data from an InputStream.
   *
   * @param in
   *          The InputStream to read from.
   * @throws IOException
   *           If there is a problem reading the stream.
   * @throws LogException
   *           If there is a problem reading the log record.
   */
  public CommandsRecord( InputStream in ) throws IOException, LogException {
    read( in );
  }


  @Override
  public RecordType getRecordType() {
    return RecordType.COMMANDS;
  }


  @Override
  public void write( OutputStream out ) throws IOException {
	  CommandLogProto.Builder builder=CommandLogProto.newBuilder()
			  .setTime(time);
	  for ( Command next : commands ) {
	      builder.addCommands(MsgProtoBuf.setCommandProto(next));
	  }
  }


  @Override
  public void read( InputStream in ) throws IOException, LogException {
	  CommandLogProto proto=CommandLogProto.parseFrom(in);
	  time=proto.getTime();
	  commands = new ArrayList<Command>();
	  for (CommandProto commandProto : proto.getCommandsList()) {
	      commands.add(MsgProtoBuf.setCommand(commandProto));
	  }
  }


  /**
   * Get the timestamp for this record.
   *
   * @return The timestamp.
   */
  public int getTime() {
    return time;
  }


  /**
   * Get the commands.
   *
   * @return The commands.
   */
  public Collection<Command> getCommands() {
    return commands;
  }
}

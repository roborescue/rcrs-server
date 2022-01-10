package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeMessage;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readMessage;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.MsgProtoBuf;
import rescuecore2.messages.protobuf.RCRSLogProto.CommandLogProto;
import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;

/**
   A commands record.
*/
public class CommandsRecord implements LogRecord {
    private int time;
    private Collection<Command> commands;

    /**
       Construct a new CommandsRecord.
       @param time The timestep of this commands record.
       @param commands The set of agent commands.
     */
    public CommandsRecord(int time, Collection<Command> commands) {
        this.time = time;
        this.commands = commands;
    }

    /**
       Construct a new CommandsRecord and read data from an InputStream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
       @throws LogException If there is a problem reading the log record.
     */
    public CommandsRecord(InputStream in) throws IOException, LogException {
        read(in);
    }

    public CommandsRecord(LogProto log) {
    	fromLogProto(log);
    }

	@Override
    public RecordType getRecordType() {
        return RecordType.COMMANDS;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(time, out);
        writeInt32(commands.size(), out);
        for (Command next : commands) {
            writeMessage(next, out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException, LogException {
        time = readInt32(in);
        commands = new ArrayList<Command>();
        int count = readInt32(in);
        for (int i = 0; i < count; ++i) {
            Message m = readMessage(in);
            if (m == null) {
                throw new LogException("Could not read message from stream");
            }
            if (!(m instanceof Command)) {
                throw new LogException("Illegal message type in commands record: " + m);
            }
            commands.add((Command)m);
        }
    }

    /**
       Get the timestamp for this record.
       @return The timestamp.
    */
    public int getTime() {
        return time;
    }

    /**
       Get the commands.
       @return The commands.
     */
    public Collection<Command> getCommands() {
        return commands;
    }

	@Override
	public void fromLogProto(LogProto log) {
		CommandLogProto command = log.getCommand();
		time=command.getTime();
		ArrayList<Command> comms = new ArrayList<>();
		for (MessageProto m :command.getCommandsList()) {
			comms.add((Command)MsgProtoBuf.messageProto2Message(m));
		}
		commands=comms;
		
	}

	@Override
	public LogProto toLogProto() {
		CommandLogProto.Builder builder = CommandLogProto.newBuilder()
				.setTime(time);
		for (Command c : commands)
			builder.addCommands(c.toMessageProto());
		return LogProto.newBuilder().setCommand(builder).build();
	}
}

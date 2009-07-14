package kernel.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeEntity;
import static rescuecore2.misc.EncodingTools.writeMessage;

import java.io.OutputStream;
import java.io.IOException;
import java.util.Collection;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

/**
   A class for writing the kernel log to an output stream.
 */
public class StreamLogWriter implements LogWriter {
    private OutputStream out;

    /**
       Create a stream log writer.
       @param stream The stream to write to.
    */
    public StreamLogWriter(OutputStream stream) throws KernelLogException {
        this.out = stream;
        try {
            writeInt32(RecordType.START_OF_LOG.getID(), out);
            out.flush();
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public void logInitialConditions(WorldModel<? extends Entity> world) throws KernelLogException {
        try {
            writeInt32(RecordType.INITIAL_CONDITIONS.getID(), out);
            Collection<? extends Entity> all = world.getAllEntities();
            writeInt32(all.size(), out);
            for (Entity next : all) {
                writeEntity(next, out);
            }
            out.flush();
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public void logPerception(int time, EntityID agentID, Collection<? extends Entity> visible, Collection<? extends Message> comms) throws KernelLogException {
        try {
            writeInt32(RecordType.PERCEPTION.getID(), out);
            writeInt32(agentID.getValue(), out);
            writeInt32(time, out);
            writeInt32(visible.size(), out);
            for (Entity next : visible) {
                writeEntity(next, out);
            }
            writeInt32(comms.size(), out);
            for (Message next : comms) {
                writeMessage(next, out);
            }
            out.flush();
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public void logCommands(int time, Collection<? extends Command> commands) throws KernelLogException {
        try {
            writeInt32(RecordType.COMMANDS.getID(), out);
            writeInt32(time, out);
            writeInt32(commands.size(), out);
            for (Command next : commands) {
                writeMessage(next, out);
            }
            out.flush();
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public void logUpdates(int time, Collection<? extends Entity> updates) throws KernelLogException {
        try {
            writeInt32(RecordType.UPDATES.getID(), out);
            writeInt32(time, out);
            writeInt32(updates.size(), out);
            for (Entity next : updates) {
                writeEntity(next, out);
            }
            out.flush();
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public void close() {
        writeInt32(RecordType.END_OF_LOG.getID(), out);
        try {
            out.flush();
        }
        catch (IOException e) {
            // FIXME: Log and ignore
            e.printStackTrace();
        }
        try {
            out.close();
        }
        catch (IOException e) {
            // FIXME: Log and ignore
            e.printStackTrace();
        }
    }
}
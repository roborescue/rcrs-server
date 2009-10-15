package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeEntity;
import static rescuecore2.misc.EncodingTools.writeMessage;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readEntity;
import static rescuecore2.misc.EncodingTools.readMessage;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.ArrayList;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;

/**
   A perception record.
*/
public class PerceptionRecord implements LogRecord {
    private int time;
    private EntityID entityID;
    private Collection<Entity> visible;
    private Collection<Message> communications;

    /**
       Construct a new PerceptionRecord.
       @param time The timestep of this perception record.
       @param id The ID of the entity.
       @param visible The set of visible entities.
       @param communications The set of communication messages.
     */
    public PerceptionRecord(int time, EntityID id, Collection<Entity> visible, Collection<Message> communications) {
        this.time = time;
        this.entityID = id;
        this.visible = visible;
        this.communications = communications;
    }

    /**
       Construct a new PerceptionRecord and read data from an InputStream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
       @throws LogException If there is a problem reading the log record.
     */
    public PerceptionRecord(InputStream in) throws IOException, LogException {
        read(in);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.PERCEPTION;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(entityID.getValue(), out);
        writeInt32(time, out);
        writeInt32(visible.size(), out);
        for (Entity e : visible) {
            writeEntity(e, out);
        }
        writeInt32(communications.size(), out);
        for (Message next : communications) {
            writeMessage(next, out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException, LogException {
        entityID = new EntityID(readInt32(in));
        time = readInt32(in);
        visible = new ArrayList<Entity>();
        communications = new ArrayList<Message>();
        int count = readInt32(in);
        for (int i = 0; i < count; ++i) {
            Entity e = readEntity(in);
            if (e == null) {
                throw new LogException("Could not read entity from stream");
            }
            visible.add(e);
        }
        count = readInt32(in);
        for (int i = 0; i < count; ++i) {
            Message m = readMessage(in);
            if (m == null) {
                throw new LogException("Could not read message from stream");
            }
            communications.add(m);
        }
    }
}
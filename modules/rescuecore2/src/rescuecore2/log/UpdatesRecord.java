package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeEntity;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readEntity;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.ArrayList;

import rescuecore2.worldmodel.Entity;

/**
   An updates record.
*/
public class UpdatesRecord implements LogRecord {
    private int time;
    private Collection<Entity> entities;

    /**
       Construct a new EntitiesRecord.
       @param time The timestep of this entities record.
       @param entities The set of agent entities.
     */
    public UpdatesRecord(int time, Collection<Entity> entities) {
        this.time = time;
        this.entities = entities;
    }

    /**
       Construct a new UpdatesRecord and read data from an InputStream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
       @throws LogException If there is a problem reading the log record.
     */
    public UpdatesRecord(InputStream in) throws IOException, LogException {
        read(in);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.UPDATES;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(time, out);
        writeInt32(entities.size(), out);
        for (Entity next : entities) {
            writeEntity(next, out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException, LogException {
        time = readInt32(in);
        entities = new ArrayList<Entity>();
        int count = readInt32(in);
        for (int i = 0; i < count; ++i) {
            Entity e = readEntity(in);
            if (e == null) {
                throw new LogException("Could not read entity from stream");
            }
            entities.add(e);
        }
    }
}
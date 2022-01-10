package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeEntity;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readEntity;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Collection;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.protobuf.MsgProtoBuf;
import rescuecore2.messages.protobuf.RCRSLogProto.InitialConditionsLogProto;
import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.messages.protobuf.RCRSProto.EntityProto;
import rescuecore2.worldmodel.DefaultWorldModel;

/**
   An initial conditions record.
*/
public class InitialConditionsRecord implements LogRecord {
    private WorldModel<Entity> model;

    /**
       Construct a new InitialConditionsRecord.
       @param model The world model to record.
     */
    public InitialConditionsRecord(WorldModel<? extends Entity> model) {
        this.model = DefaultWorldModel.create();
        this.model.merge(model.getAllEntities());
    }

    /**
       Construct a new InitialConditionsRecord and read data from an InputStream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
       @throws LogException If there is a problem reading the log record.
     */
    public InitialConditionsRecord(InputStream in) throws IOException, LogException {
        read(in);
    }
    public InitialConditionsRecord(LogProto in) throws LogException {
        fromLogProto(in);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.INITIAL_CONDITIONS;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        Collection<? extends Entity> all = model.getAllEntities();
        writeInt32(all.size(), out);
        for (Entity e : all) {
            writeEntity(e, out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException, LogException {
        model = DefaultWorldModel.create();
        int size = readInt32(in);
        for (int i = 0; i < size; ++i) {
            Entity e = readEntity(in);
            if (e == null) {
                throw new LogException("Could not read entity from stream");
            }
            model.addEntity(e);
        }
    }

    /**
       Get the world model.
       @return The world model.
     */
    public WorldModel<Entity> getWorldModel() {
        return model;
    }

	@Override
	public void fromLogProto(LogProto log) throws LogException {
		model = DefaultWorldModel.create();
		for(EntityProto e:log.getInitialCondition().getEntitiesList()) {
			Entity en = MsgProtoBuf.entityProto2Entity(e);
			if(en==null)
				throw new LogException("Could not read entity from stream");
			model.addEntity(en);
		}
	}

	@Override
	public LogProto toLogProto() {
		InitialConditionsLogProto.Builder builder=InitialConditionsLogProto.newBuilder();
		Collection<? extends Entity> all = model.getAllEntities();
        for (Entity e : all) {
            builder.addEntities(e.toEntityProto());
        }
		return LogProto.newBuilder().setInitialCondition(builder).build();
	}
}

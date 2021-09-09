package rescuecore2.log;


import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.List;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.control.ControlMessageProto.EntityProto;
import rescuecore2.messages.control.ControlMessageProto.InitialConditionsLogProto;
import rescuecore2.messages.control.MsgProtoBuf;
import rescuecore2.worldmodel.DefaultWorldModel;

/**
 * An initial conditions record.
 */
public class InitialConditionsRecord implements LogRecord {

  private WorldModel<Entity> model;


  /**
   * Construct a new InitialConditionsRecord.
   *
   * @param model
   *          The world model to record.
   */
  public InitialConditionsRecord( WorldModel<? extends Entity> model ) {
    this.model = DefaultWorldModel.create();
    this.model.merge( model.getAllEntities() );
  }


  /**
   * Construct a new InitialConditionsRecord and read data from an InputStream.
   *
   * @param in
   *          The InputStream to read from.
   * @throws IOException
   *           If there is a problem reading the stream.
   * @throws LogException
   *           If there is a problem reading the log record.
   */
  public InitialConditionsRecord( InputStream in ) throws IOException, LogException {
    read( in );
  }


  @Override
  public RecordType getRecordType() {
    return RecordType.INITIAL_CONDITIONS;
  }


  @Override
  public void write( OutputStream out ) throws IOException {
	  InitialConditionsLogProto.Builder builder=InitialConditionsLogProto.newBuilder();
	  for ( Entity e : model.getAllEntities()) {
		  builder.addEntities(MsgProtoBuf.setEntityProto(e));
	  }
	  builder.build().writeTo(out);
//    Collection<? extends Entity> all = model.getAllEntities();
//    writeInt32( all.size(), out );
//    for ( Entity e : all ) {
//      // writeEntity(e, out);
//    }
  }


  @Override
  public void read( InputStream in ) throws IOException, LogException {
	InitialConditionsLogProto initialConditionsLogProto=InitialConditionsLogProto.parseFrom(in);
    this.model = DefaultWorldModel.create();
    List<EntityProto> entityList = initialConditionsLogProto.getEntitiesList();
    for (EntityProto entityProto : entityList) {
      Entity entity = MsgProtoBuf.setEntity(entityProto);
      if (entity != null) {
        this.model.addEntity(entity);
      }
    }
//    int size = readInt32( in );
//    for ( int i = 0; i < size; ++i ) {
//      // Entity e = readEntity(in);
//      // if (e == null) {
//      // throw new LogException("Could not read entity from stream");
//      // }
//      // model.addEntity(e);
//    }
  }


  /**
   * Get the world model.
   *
   * @return The world model.
   */
  public WorldModel<Entity> getWorldModel() {
    return model;
  }
}
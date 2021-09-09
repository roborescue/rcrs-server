package rescuecore2.log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.commands.Command;
import rescuecore2.messages.control.MsgProtoBuf;
import rescuecore2.messages.control.ControlMessageProto.CommandProto;
import rescuecore2.messages.control.ControlMessageProto.PerceptionLogProto;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * A perception record.
 */
public class PerceptionRecord implements LogRecord {

  private int time;
  private EntityID entityID;
  private ChangeSet visible;
  private Collection<Command> communications;

  /**
   * Construct a new PerceptionRecord.
   *
   * @param time           The timestep of this perception record.
   * @param id             The ID of the entity.
   * @param visible        The set of visible changes to entities.
   * @param communications The set of communication messages.
   */
  public PerceptionRecord(int time, EntityID id, ChangeSet visible, Collection<Command> communications) {
    this.time = time;
    this.entityID = id;
    this.visible = visible;
    this.communications = communications;
  }

  /**
   * Construct a new PerceptionRecord and read data from an InputStream.
   *
   * @param in The InputStream to read from.
   * @throws IOException  If there is a problem reading the stream.
   * @throws LogException If there is a problem reading the log record.
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
	PerceptionLogProto.Builder builder=PerceptionLogProto.newBuilder()
			.setEntityID(entityID.getValue())
			.setTime(time)
			.setVisible(MsgProtoBuf.setChangeSetProto(visible));
	for (Command next : communications) {
	    builder.addCommunications(MsgProtoBuf.setCommandProto(next));
	}
	builder.build().writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException, LogException {
	  PerceptionLogProto perceptionLogProto=PerceptionLogProto.parseFrom(in);
	  entityID = new EntityID(perceptionLogProto.getEntityID());
	  time = perceptionLogProto.getTime();
	  visible = MsgProtoBuf.setChangeSet(perceptionLogProto.getVisible());
	  communications = new ArrayList<Command>();
	  for (CommandProto commandProto:perceptionLogProto.getCommunicationsList()) {
	      communications.add(MsgProtoBuf.setCommand(commandProto));
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
   * Get the EntityID for this record.
   *
   * @return The EntityID.
   */
  public EntityID getEntityID() {
    return entityID;
  }

  /**
   * Get the set of visible entity updates.
   *
   * @return The visible entity updates.
   */
  public ChangeSet getChangeSet() {
    return visible;
  }

  /**
   * Get the set of communication messages heard.
   *
   * @return The communication messages heard.
   */
  public Collection<Command> getHearing() {
    return communications;
  }
}
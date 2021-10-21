package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readEntity;
import static rescuecore2.misc.EncodingTools.writeEntity;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;
import rescuecore2.messages.protobuf.MsgProtoBuf;
import rescuecore2.worldmodel.Entity;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   An Entity component to a message.
 */
public class EntityComponent extends AbstractMessageComponent {
    private Entity entity;

    /**
       Construct an EntityComponent with no content.
       @param name The name of the component.
     */
    public EntityComponent(URN name) {
        super(name);
        entity = null;
    }

    /**
       Construct an EntityComponent with a specific entity value.
       @param name The name of the component.
       @param entity The value of this component.
     */
    public EntityComponent(URN name, Entity entity) {
        super(name);
        this.entity = entity;
    }

    /**
       Get the entity.
       @return The entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
       Set the entity.
       @param e The new entity.
     */
    public void setEntity(Entity e) {
        entity = e;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeEntity(entity, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        entity = readEntity(in);
    }

    @Override
    public String toString() {
        return getName() + " = " + entity.toString();
    }

	@Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
		entity=MsgProtoBuf.entityProto2Entity(proto.getEntity());
		
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		return MessageComponentProto.newBuilder().setEntity(entity.toEntityProto()).build();
	}
}

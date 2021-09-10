package rescuecore2.messages.protobuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore.commands.Command;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.ControlMessageProto.EntityProto;
import rescuecore2.messages.protobuf.ControlMessageProto.MessageProto;
import rescuecore2.messages.protobuf.ControlMessageProto.PropertyProto;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

public class MsgProtoBuf {

	public static Property propertyProto2Property(PropertyProto propertyProto) {
		Property property = Registry.getCurrentRegistry().createProperty(propertyProto.getUrn());
		if(property!=null)
			property.fromPropertyProto(propertyProto);
		return property;
	}
	public static Entity entityProto2Entity(EntityProto entityProto) {
		String urn = entityProto.getUrn();
		Entity entity = Registry.getCurrentRegistry().createEntity(urn,
				new EntityID(entityProto.getEntityID()));
		if(entity!=null)
			entity.fromEntityProto(entityProto);
		return entity;
	}
	public static Message messageProto2Message(MessageProto messageProto) {
		String urn = messageProto.getUrn();
		Message msg = Registry.getCurrentRegistry().createMessage(urn,
				messageProto);
		return msg;
	}
}

package rescuecore2.messages.protobuf;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.ControlMessageProto.EntityProto;
import rescuecore2.messages.protobuf.ControlMessageProto.EntityURN;
import rescuecore2.messages.protobuf.ControlMessageProto.MessageProto;
import rescuecore2.messages.protobuf.ControlMessageProto.MsgURN;
import rescuecore2.messages.protobuf.ControlMessageProto.PropertyProto;
import rescuecore2.messages.protobuf.ControlMessageProto.PropertyURN;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

public class MsgProtoBuf {

	public static Property propertyProto2Property(PropertyProto propertyProto) {
		PropertyURN urn = propertyProto.getUrn();
		Property property = Registry.getCurrentRegistry().createProperty(urn.toString());
		if(property!=null)
			property.fromPropertyProto(propertyProto);
		return property;
	}
	public static Entity entityProto2Entity(EntityProto entityProto) {
		EntityURN urn = entityProto.getUrn();
		Entity entity = Registry.getCurrentRegistry().createEntity(urn.toString(),
				new EntityID(entityProto.getEntityID()));
		if(entity!=null)
			entity.fromEntityProto(entityProto);
		return entity;
	}
	public static Message messageProto2Message(MessageProto messageProto) {
		MsgURN urn = messageProto.getUrn();
		Message msg = Registry.getCurrentRegistry().createMessage(urn.toString(),
				messageProto);
		return msg;
	}
}

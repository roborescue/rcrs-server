package rescuecore2.messages.protobuf;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.EntityProto;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

public class MsgProtoBuf {

  public static Property propertyProto2Property(PropertyProto propertyProto) {
    int urn = propertyProto.getUrn();
    Property property = Registry.getCurrentRegistry().createProperty(urn);
    if (property != null)
      property.fromPropertyProto(propertyProto);
    return property;
  }

  public static Entity entityProto2Entity(EntityProto entityProto) {
    int urn = entityProto.getUrn();
    Entity entity = Registry.getCurrentRegistry().createEntity(urn, new EntityID(entityProto.getEntityID()));
    if (entity != null)
      entity.fromEntityProto(entityProto);
    return entity;
  }

  public static Message messageProto2Message(MessageProto messageProto) {
    int urn = messageProto.getUrn();
    Message msg = Registry.getCurrentRegistry().createMessage(urn, messageProto);
    return msg;
  }
}
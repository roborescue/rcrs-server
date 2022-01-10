package rescuecore2.worldmodel;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readProperty;
import static rescuecore2.misc.EncodingTools.readString;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeProperty;
import static rescuecore2.misc.EncodingTools.writeString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rescuecore2.log.Logger;
import rescuecore2.messages.protobuf.MsgProtoBuf;
import rescuecore2.messages.protobuf.RCRSProto.ChangeSetProto;
import rescuecore2.messages.protobuf.RCRSProto.ChangeSetProto.EntityChangeProto;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.registry.Registry;
//import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.properties.EntityRefListProperty;

/**
 * This class is used for accumulating changes to entities.
 */
public class ChangeSet {

  private Map<EntityID, Map<Integer, Property>> changes;
  private Set<EntityID> deleted;
  private Map<EntityID, Integer> entityURNs;

  /**
   * Create an empty ChangeSet.
   */
  public ChangeSet() {
    changes = new LazyMap<EntityID, Map<Integer, Property>>() {

      @Override
      public Map<Integer, Property> createValue() {
        return new HashMap<Integer, Property>();
      }
    };
    entityURNs = new HashMap<EntityID, Integer>();
    deleted = new HashSet<EntityID>();
  }

  /**
   * Copy constructor.
   *
   * @param other The ChangeSet to copy.
   */
  public ChangeSet(ChangeSet other) {
    this();
    merge(other);
  }

  /**
   * Add a change.
   *
   * @param e The entity that has changed.
   * @param p The property that has changed.
   */
  public void addChange(Entity e, Property p) {
    addChange(e.getID(), e.getURN(), p);
  }

  /**
   * Add a change.
   *
   * @param e   The ID of the entity that has changed.
   * @param urn The URN of the entity that has changed.
   * @param p   The property that has changed.
   */
  public void addChange(EntityID e, int urn, Property p) {
    if (deleted.contains(e)) {
      return;
    }
    Property prop = p.copy();
    changes.get(e).put(prop.getURN(), prop);
    entityURNs.put(e, urn);
  }

  /**
   * Register a deleted entity.
   *
   * @param e The ID of the entity that has been deleted.
   */
  public void entityDeleted(EntityID e) {
    deleted.add(e);
    changes.remove(e);
  }

  /**
   * Get the properties that have changed for an entity.
   *
   * @param e The entity ID to look up.
   * @return The set of changed properties. This may be empty but will never be
   *         null.
   */
  public Set<Property> getChangedProperties(EntityID e) {
    return new HashSet<Property>(changes.get(e).values());
  }

  /**
   * Look up a property change for an entity by property URN.
   *
   * @param e   The entity ID to look up.
   * @param urn The property URN to look up.
   * @return The changed property with the right URN, or null if the property is
   *         not found or has not changed.
   */
  public Property getChangedProperty(EntityID e, int urn) {
    Map<Integer, Property> props = changes.get(e);
    if (props != null) {
      return props.get(urn);
    }
    return null;
  }

  /**
   * Get the IDs of all changed entities.
   *
   * @return A set of IDs of changed entities.
   */
  public Set<EntityID> getChangedEntities() {
    return new HashSet<EntityID>(changes.keySet());
  }

  /**
   * Get the IDs of all deleted entities.
   *
   * @return A set of IDs of deleted entities.
   */
  public Set<EntityID> getDeletedEntities() {
    return new HashSet<EntityID>(deleted);
  }

  /**
   * Get the URN of a changed entity.
   *
   * @param id The ID of the entity.
   * @return The URN of the changed entity.
   */
  public int getEntityURN(EntityID id) {
    return entityURNs.get(id);
  }

  /**
   * Merge another ChangeSet into this one.
   *
   * @param other The other ChangeSet.
   */
  // private static final String BLOCKADES_URN = StandardPropertyURN.BLOCKADES
  // .toString();

  public void merge(ChangeSet other) {
    for (Map.Entry<EntityID, Map<Integer, Property>> next : other.changes.entrySet()) {
      EntityID e = next.getKey();
      int urn = other.getEntityURN(e);
      for (Property p : next.getValue().values()) {

        // if ( p.getURN().equals( BLOCKADES_URN )
        // && changes.get( e ).containsKey( BLOCKADES_URN ) ) {

        if ((p instanceof EntityRefListProperty)
            && (changes.get(e).containsKey(urn) && (changes.get(e).get(urn) instanceof EntityRefListProperty))) {

          EntityRefListProperty bp1 = (EntityRefListProperty) p.copy();
          // EntityRefListProperty bp2 = (EntityRefListProperty)
          // changes.get( e )
          // .get( BLOCKADES_URN );
          EntityRefListProperty bp2 = (EntityRefListProperty) changes.get(e).get(urn);

          if (bp2.isDefined()) {
            for (EntityID id : bp2.getValue())
              bp1.addValue(id);
          }

          for (EntityID id : deleted) {
            bp1.removeValue(id);
          }

          for (EntityID id : other.deleted) {
            bp1.removeValue(id);
          }

          p = bp1;
        }

        addChange(e, urn, p);
      }
    }
    deleted.addAll(other.deleted);
  }

  /**
   * Add all defined properties from a collection.
   *
   * @param c The collection to copy changes from.
   */
  public void addAll(Collection<? extends Entity> c) {
    for (Entity entity : c) {
      for (Property property : entity.getProperties()) {
        if (property.isDefined()) {
          addChange(entity, property);
        }
      }
    }
  }

  /**
   * Write this ChangeSet to a stream.
   *
   * @param out The stream to write to.
   * @throws IOException If there is a problem.
   */
  public void write(OutputStream out) throws IOException {
    // Number of entity IDs
    writeInt32(changes.size(), out);
    for (Map.Entry<EntityID, Map<Integer, Property>> next : changes.entrySet()) {
      EntityID id = next.getKey();
      Collection<Property> props = next.getValue().values();
      // EntityID, URN, number of properties
      writeInt32(id.getValue(), out);
      writeString(Registry.getCurrentRegistry().toURN_Str(getEntityURN(id)), out);
      writeInt32(props.size(), out);
      for (Property prop : props) {
        writeProperty(prop, out);
      }
    }
    writeInt32(deleted.size(), out);
    for (EntityID next : deleted) {
      writeInt32(next.getValue(), out);
    }
  }

  /**
   * Read this ChangeSet from a stream.
   *
   * @param in The stream to read from.
   * @throws IOException If there is a problem.
   */
  public void read(InputStream in) throws IOException {
    changes.clear();
    deleted.clear();
    int entityCount = readInt32(in);
    for (int i = 0; i < entityCount; ++i) {
      EntityID id = new EntityID(readInt32(in));
      int urn = Registry.getCurrentRegistry().toURN_Id(readString(in));
      int propCount = readInt32(in);
      for (int j = 0; j < propCount; ++j) {
        Property p = readProperty(in);
        if (p != null) {
          addChange(id, urn, p);
        }
      }
    }
    int deletedCount = readInt32(in);
    for (int i = 0; i < deletedCount; ++i) {
      EntityID id = new EntityID(readInt32(in));
      deleted.add(id);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("ChangeSet:");
    for (Entry<EntityID, Map<Integer, Property>> next : changes.entrySet()) {
      result.append(" Entity ");
      result.append(next.getKey());
      result.append(" (");
      result.append(getEntityURN(next.getKey()));
      result.append(") [");
      for (Iterator<Property> it = next.getValue().values().iterator(); it.hasNext();) {
        result.append(it.next());
        if (it.hasNext()) {
          result.append(", ");
        }
      }
      result.append("]");
    }
    result.append(" {Deleted ");
    for (Iterator<EntityID> it = deleted.iterator(); it.hasNext();) {
      result.append(it.next());
      if (it.hasNext()) {
        result.append(", ");
      }
    }
    result.append("}");
    return result.toString();
  }

  /**
   * Write this changeset to Logger.debug in a readable form.
   */
  public void debug() {
    Logger.debug("ChangeSet");
    for (Map.Entry<EntityID, Map<Integer, Property>> next : changes.entrySet()) {
      Logger.debug("  Entity " + next.getKey() + "(" + getEntityURN(next.getKey()) + ")");
      for (Iterator<Property> it = next.getValue().values().iterator(); it.hasNext();) {
        Logger.debug("    " + it.next());
      }
    }
    for (Iterator<EntityID> it = deleted.iterator(); it.hasNext();) {
      Logger.debug("  Deleted: " + it.next());
    }
  }

  public void fromChangeSetProto(ChangeSetProto changeSetProto) {
    changes.clear();
    deleted.clear();
    List<EntityChangeProto> changesList = changeSetProto.getChangesList();
    for (EntityChangeProto entityChange : changesList) {
      EntityID entityID = new EntityID(entityChange.getEntityID());
      int urn = entityChange.getUrn();

      List<PropertyProto> propertyProtoList = entityChange.getPropertiesList();
      for (PropertyProto propertyProto : propertyProtoList) {
        Property prop = MsgProtoBuf.propertyProto2Property(propertyProto);
        if (prop != null) {
          this.addChange(entityID, urn, prop);
        }
      }
    }
    // Add deleted entities
    for (Integer entityID : changeSetProto.getDeletesList()) {
      this.entityDeleted(new EntityID(entityID));
    }

  }

  public ChangeSetProto toChangeSetProto() {
    ChangeSetProto.Builder builder = ChangeSetProto.newBuilder();
    for (Entry<EntityID, Map<Integer, Property>> next : changes.entrySet()) {
      EntityID id = next.getKey();
      Collection<Property> props = next.getValue().values();
      // EntityID, URN, number of properties
      EntityChangeProto.Builder entityChangeBuilder = EntityChangeProto.newBuilder().setEntityID(id.getValue())
          .setUrn(getEntityURN(id));
      for (Property prop : props) {
        entityChangeBuilder.addProperties(prop.toPropertyProto());
      }
      builder.addChanges(entityChangeBuilder);
    }
    for (EntityID next : deleted) {
      builder.addDeletes(next.getValue());
    }
    return builder.build();
  }
}
package rescuecore2.worldmodel;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readProperty;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rescuecore2.messages.protobuf.MsgProtoBuf;
import rescuecore2.messages.protobuf.RCRSProto.EntityProto;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;

/**
 * Abstract base class for concrete Entity implementations.
 */
public abstract class AbstractEntity implements Entity {

  private final EntityID id;
  private final Set<EntityListener> listeners;
  private final Set<Property> properties;

  /**
   * Construct an AbstractEntity with a set of properties.
   *
   * @param id The ID of this entity.
   */
  protected AbstractEntity(EntityID id) {
    this.id = id;
    listeners = new HashSet<EntityListener>();
    properties = new HashSet<Property>();
  }

  /**
   * AbstractEntity copy constructor.
   *
   * @param other The AbstractEntity to copy.
   */
  protected AbstractEntity(AbstractEntity other) {
    this(other.getID());
  }

  @Override
  public void addEntityListener(EntityListener l) {
    synchronized (listeners) {
      listeners.add(l);
    }
  }

  @Override
  public void removeEntityListener(EntityListener l) {
    synchronized (listeners) {
      listeners.remove(l);
    }
  }

  @Override
  public Entity copy() {
    Entity result = copyImpl();
    for (Property original : getProperties()) {
      Property copy = result.getProperty(original.getURN());
      copy.takeValue(original);
    }
    return result;
  }

  /**
   * Create a copy of this entity. Property values do not need to be copied.
   *
   * @return A new Entity of the same type as this and with the same ID.
   */
  protected abstract Entity copyImpl();

  @Override
  public final Set<Property> getProperties() {
    return properties;
  }

  @Override
  public Property getProperty(int propertyURN) {
    return null;
  }

  @Override
  public EntityID getID() {
    return id;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    int count = 0;
    for (Property next : getProperties()) {
      if (next.isDefined()) {
        ++count;
      }
    }
    writeInt32(count, out);
    for (Property next : getProperties()) {
      if (next.isDefined()) {
        writeProperty(next, out);
      }
    }
  }

  @Override
  public void read(InputStream in) throws IOException {
    int count = readInt32(in);
    for (int i = 0; i < count; ++i) {
      Property prop = readProperty(in);
      if (prop == null) {
        continue;
      }
      Property existing = getProperty(prop.getURN());
      existing.takeValue(prop);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(getEntityName());
    result.append(" (");
    result.append(id);
    result.append(")");
    return result.toString();
  }

  /**
   * Get the full description of this object.
   *
   * @return The full description.
   */
  public String getFullDescription() {
    StringBuilder result = new StringBuilder();
    String name = getEntityName();
    int urn = getURN();
    result.append(name);
    result.append(" [");
    result.append(urn);
    result.append("]");
    result.append(" (");
    result.append(id);
    result.append(") [");
    for (Iterator<Property> it = getProperties().iterator(); it.hasNext();) {
      result.append(it.next().toString());
      if (it.hasNext()) {
        result.append(", ");
      }
    }
    result.append("]");
    return result.toString();
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof AbstractEntity) {
      AbstractEntity a = (AbstractEntity) o;
      return this.id.equals(a.id);
    }
    return false;
  }

  /**
   * Get the name of this entity. Default implementation returns the entity URN.
   *
   * @return The name of this entity.
   */
  protected String getEntityName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Register a set of properties.
   *
   * @param props The properties to register.
   */
  protected void registerProperties(Property... props) {
    for (Property p : props) {
      properties.add(p);
      if (p instanceof AbstractProperty) {
        ((AbstractProperty) p).setEntity(this);
      }
    }
  }

  /**
   * Notify all listeners that a property has changed.
   *
   * @param p        The changed property.
   * @param oldValue The old value.
   * @param newValue The new value.
   */
  protected void firePropertyChanged(Property p, Object oldValue, Object newValue) {
    Collection<EntityListener> copy;
    synchronized (listeners) {
      copy = new HashSet<EntityListener>(listeners);
    }
    for (EntityListener next : copy) {
      next.propertyChanged(this, p, oldValue, newValue);
    }
  }

  @Override
  public EntityProto toEntityProto() {
    EntityProto.Builder builder = EntityProto.newBuilder().setEntityID(id.getValue()).setUrn(getURN());
    for (Property next : getProperties()) {
      if (next.isDefined()) {
        builder.addProperties(next.toPropertyProto());
      }
    }
    return builder.build();
  }

  @Override
  public void fromEntityProto(EntityProto proto) {
    for (PropertyProto propertyProto : proto.getPropertiesList()) {
      Property prop = MsgProtoBuf.propertyProto2Property(propertyProto);
      if (prop == null) {
        continue;
      }
      Property existing = getProperty(prop.getURN());
      existing.takeValue(prop);
    }
  }
}
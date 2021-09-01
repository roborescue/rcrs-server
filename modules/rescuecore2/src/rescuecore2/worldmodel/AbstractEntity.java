package rescuecore2.worldmodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for concrete Entity implementations.
 */
public abstract class AbstractEntity implements Entity {

  private final EntityID id;
  private final Set<EntityListener> listeners;
  private final Set<Property<?>> properties;

  /**
   * Construct an AbstractEntity with a set of properties.
   *
   * @param id The ID of this entity.
   */
  protected AbstractEntity(EntityID id) {
    this.id = id;
    this.listeners = new HashSet<EntityListener>();
    this.properties = new HashSet<Property<?>>();
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
    synchronized (this.listeners) {
      listeners.add(l);
    }
  }

  @Override
  public void removeEntityListener(EntityListener l) {
    synchronized (this.listeners) {
      listeners.remove(l);
    }
  }

  @Override
  public Entity copy() {
    Entity result = copyImpl();
    for (Property<?> original : this.getProperties()) {
      Property<?> copy = result.getProperty(original.getURN());
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

  /**
   * Get the name of this entity. Default implementation returns the entity URN.
   *
   * @return The name of this entity.
   */
  protected String getEntityName() {
    return this.getURN();
  }

  @Override
  public EntityID getID() {
    return id;
  }

  @Override
  public final Set<Property<?>> getProperties() {
    return this.properties;
  }

  @Override
  public Property<?> getProperty(String propertyURN) {
    for (Property<?> prop : this.properties) {
      if (prop.getURN().equals(propertyURN)) {
        return prop;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(this.getEntityName());
    result.append(" (");
    result.append(this.id);
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
    String name = this.getEntityName();
    String urn = this.getURN();
    if (!name.equals(urn)) {
      result.append(name);
      result.append(" [");
      result.append(urn);
      result.append("]");
    } else {
      result.append(name);
    }
    result.append(" (");
    result.append(this.id);
    result.append(") [");
    for (Iterator<Property<?>> it = getProperties().iterator(); it.hasNext();) {
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
    return this.id.hashCode();
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
   * Register a set of properties.
   *
   * @param props The properties to register.
   */
  protected void registerProperties(Property<?>... props) {
    for (Property<?> p : props) {
      properties.add(p);
      if (p instanceof AbstractProperty) {
        ((AbstractProperty<?>) p).setEntity(this);
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
  protected void firePropertyChanged(Property<?> p, Object oldValue, Object newValue) {
    Collection<EntityListener> copy;
    synchronized (this.listeners) {
      copy = new HashSet<EntityListener>(this.listeners);
    }
    for (EntityListener next : copy) {
      next.propertyChanged(this, p, oldValue, newValue);
    }
  }

  public abstract void setEntity(Map<String, List<Object>> properties);
}
package rescuecore2.worldmodel;

import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;

/**
 * Abstract base class for Property implementations.
 */
public abstract class AbstractProperty implements Property {
  private boolean defined;
  private final int urn;
  private AbstractEntity entity;

  /**
   * Construct a property with a given type and assume that the value of this
   * property is initially undefined.
   *
   * @param urn The urn of the property.
   */
  protected AbstractProperty(int urn) {
    this(urn, false);
  }

  /**
   * Construct a property with a given type and assume that the value of this
   * property is initially undefined.
   *
   * @param urn The urn of the property.
   */
  protected AbstractProperty(URN urn) {
    this(urn.getURNId(), false);
  }

  /**
   * Construct a property with a given type and whether the value of this property
   * is initially defined or not.
   *
   * @param urn     The urn of the property.
   * @param defined Whether the value is initially defined or not.
   */
  protected AbstractProperty(int urn, boolean defined) {
    this.urn = urn;
    this.defined = defined;
    entity = null;
  }

  /**
   * Construct a property with a given type and whether the value of this property
   * is initially defined or not.
   *
   * @param urn     The urn of the property.
   * @param defined Whether the value is initially defined or not.
   */
  protected AbstractProperty(URN urn, boolean defined) {
    this(urn.getURNId(), defined);
  }

  /**
   * AbstractProperty copy constructor.
   *
   * @param other The AbstractProperty to copy.
   */
  protected AbstractProperty(AbstractProperty other) {
    this(other.getURN(), other.isDefined());
  }

  /**
   * Set the property status to defined.
   */
  protected void setDefined() {
    this.defined = true;
  }

  /**
   * Set this property's containing Entity.
   *
   * @param e The AbstractEntity that holds this property.
   */
  protected void setEntity(AbstractEntity e) {
    this.entity = e;
  }

  @Override
  public boolean isDefined() {
    return this.defined;
  }

  @Override
  public void undefine() {
    Object old = getValue();
    this.defined = false;
    fireChange(old, null);
  }

  @Override
  public int getURN() {
    return this.urn;
  }

  /**
   * Notify the entity that this property has changed.
   *
   * @param oldValue The old value of this property.
   * @param newValue The new value of this property.
   */
  protected void fireChange(Object oldValue, Object newValue) {
    if (this.entity != null) {
      this.entity.firePropertyChanged(this, oldValue, newValue);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(getURN());
    if (isDefined()) {
      result.append(" = ");
      result.append(getValue());
    } else {
      result.append(" (undefined)");
    }
    return result.toString();
  }

  protected PropertyProto.Builder basePropertyProto() {
    PropertyProto.Builder builder = PropertyProto.newBuilder().setUrn(getURN()).setDefined(isDefined());
    return builder;
  }
}
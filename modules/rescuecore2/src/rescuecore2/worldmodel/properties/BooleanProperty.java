package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.Property;

/**
 * A boolean property.
 */
public class BooleanProperty extends AbstractProperty {
  private boolean value;

  /**
   * Construct a BooleanProperty with no defined value.
   *
   * @param urn The urn of this property.
   */
  public BooleanProperty(int urn) {
    super(urn);
  }

  /**
   * Construct a BooleanProperty with no defined value.
   *
   * @param urn The urn of this property.
   */
  public BooleanProperty(URN urn) {
    super(urn);
  }

  /**
   * Construct a BooleanProperty with a defined value.
   *
   * @param urn   The urn of this property.
   * @param value The initial value of the property.
   */
  public BooleanProperty(int urn, boolean value) {
    super(urn, true);
    this.value = value;
  }

  /**
   * Construct a BooleanProperty with a defined value.
   *
   * @param urn   The urn of this property.
   * @param value The initial value of the property.
   */
  public BooleanProperty(URN urn, boolean value) {
    super(urn, true);
    this.value = value;
  }

  /**
   * BooleanProperty copy constructor.
   *
   * @param other The BooleanProperty to copy.
   */
  public BooleanProperty(BooleanProperty other) {
    super(other);
    this.value = other.value;
  }

  @Override
  public Boolean getValue() {
    if (!isDefined()) {
      return null;
    }
    return value;
  }

  /**
   * Set the value of this property. Future calls to {@link #isDefined()} will
   * return true.
   *
   * @param value The new value.
   */
  public void setValue(boolean value) {
    boolean old = this.value;
    boolean wasDefined = isDefined();
    this.value = value;
    setDefined();
    if (!wasDefined || old != value) {
      fireChange(old, value);
    }
  }

  @Override
  public void takeValue(Property p) {
    if (p instanceof BooleanProperty) {
      BooleanProperty b = (BooleanProperty) p;
      if (b.isDefined()) {
        setValue(b.getValue());
      } else {
        undefine();
      }
    } else {
      throw new IllegalArgumentException(this + " cannot take value from " + p);
    }
  }

  @Override
  public void write(OutputStream out) throws IOException {
    writeInt32(value ? 1 : 0, out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    setValue(readInt32(in) != 0);
  }

  @Override
  public BooleanProperty copy() {
    return new BooleanProperty(this);
  }

  @Override
  public PropertyProto toPropertyProto() {
    PropertyProto.Builder builder = basePropertyProto();
    if (isDefined()) {
      builder.setBoolValue(value);
    }
    return builder.build();
  }

  @Override
  public void fromPropertyProto(PropertyProto proto) {
    if (!proto.getDefined())
      return;
    setValue(proto.getBoolValue());
  }
}
package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.worldmodel.AbstractProperty;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A property that refers to an entity ID.
 */
public class EntityRefProperty extends AbstractProperty {
	private EntityID value;

	/**
	 * Construct an EntityRefProperty with no defined value.
	 * 
	 * @param urn The urn of this property.
	 */
	public EntityRefProperty(int urn) {
		super(urn);
	}

	/**
	 * Construct an EntityRefProperty with no defined value.
	 * 
	 * @param urn The urn of this property.
	 */
	public EntityRefProperty(URN urn) {
		super(urn);
	}

	/**
	 * Construct an EntityRefProperty with a defined value.
	 * 
	 * @param urn   The urn of this property.
	 * @param value The initial value of the property.
	 */
	public EntityRefProperty(int urn, EntityID value) {
		super(urn, true);
		this.value = value;
	}

	/**
	 * Construct an EntityRefProperty with a defined value.
	 * 
	 * @param urn   The urn of this property.
	 * @param value The initial value of the property.
	 */
	public EntityRefProperty(URN urn, EntityID value) {
		super(urn, true);
		this.value = value;
	}

	/**
	 * EntityRefProperty copy constructor.
	 * 
	 * @param other The EntityRefProperty to copy.
	 */
	public EntityRefProperty(EntityRefProperty other) {
		super(other);
		this.value = other.value;
	}

	@Override
	public EntityID getValue() {
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
	public void setValue(EntityID value) {
		EntityID old = this.value;
		boolean wasDefined = isDefined();
		this.value = value;
		setDefined();
		if (!wasDefined || !old.equals(value)) {
			fireChange(old, value);
		}
	}

	@Override
	public void takeValue(Property p) {
		if (p instanceof EntityRefProperty) {
			EntityRefProperty e = (EntityRefProperty) p;
			if (e.isDefined()) {
				setValue(e.getValue());
			} else {
				undefine();
			}
		} else {
			throw new IllegalArgumentException(
					this + " cannot take value from " + p);
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		writeInt32(value.getValue(), out);
	}

	@Override
	public void read(InputStream in) throws IOException {
		setValue(new EntityID(readInt32(in)));
	}

	@Override
	public EntityRefProperty copy() {
		return new EntityRefProperty(this);
	}

	@Override
	public PropertyProto toPropertyProto() {
		PropertyProto.Builder builder = basePropertyProto();
		if (isDefined()) {
			builder.setIntValue(value.getValue());
		}
    	return builder.build();
	}

	@Override
	public void fromPropertyProto(PropertyProto proto) {
		if (!proto.getDefined())
			return;
		setValue(new EntityID(proto.getIntValue()));
	}
}

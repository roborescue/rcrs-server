package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readDouble;
import static rescuecore2.misc.EncodingTools.writeDouble;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.worldmodel.AbstractProperty;

/**
   A single double-precision floating point number property.
 */
public class DoubleProperty extends AbstractProperty {
    private double value;

    /**
       Construct a DoubleProperty with no defined value.
       @param urn The urn of this property.
    */
    public DoubleProperty(int urn) {
        super(urn);
    }

    /**
       Construct a DoubleProperty with no defined value.
       @param urn The urn of this property.
    */
    public DoubleProperty(URN urn) {
        super(urn);
    }

    /**
       Construct a DoubleProperty with a defined value.
       @param urn The urn of this property.
       @param value The initial value of the property.
    */
    public DoubleProperty(int urn, double value) {
        super(urn, true);
        this.value = value;
    }

    /**
       Construct a DoubleProperty with a defined value.
       @param urn The urn of this property.
       @param value The initial value of the property.
    */
    public DoubleProperty(URN urn, double value) {
        super(urn, true);
        this.value = value;
    }

    /**
       DoubleProperty copy constructor.
       @param other The DoubleProperty to copy.
     */
    public DoubleProperty(DoubleProperty other) {
        super(other);
        this.value = other.value;
    }

    @Override
    public Double getValue() {
        if (!isDefined()) {
            return null;
        }
        return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
    */
    public void setValue(double value) {
        double old = this.value;
        boolean wasDefined = isDefined();
        this.value = value;
        setDefined();
        if (!wasDefined || old != value) {
            fireChange(old, value);
        }
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof DoubleProperty) {
            DoubleProperty d = (DoubleProperty)p;
            if (d.isDefined()) {
                setValue(d.getValue());
            }
            else {
                undefine();
            }
        }
        else {
            throw new IllegalArgumentException(this + " cannot take value from " + p);
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeDouble(value, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        setValue(readDouble(in));
    }

    @Override
    public DoubleProperty copy() {
        return new DoubleProperty(this);
    }

    @Override
    public PropertyProto toPropertyProto() {
    	PropertyProto.Builder builder = basePropertyProto();
		if (isDefined()) {
			builder.setDoubleValue(value);
		}
    	return builder.build();
    }
    @Override
    public void fromPropertyProto(PropertyProto proto) {
    	if (!proto.getDefined())
			return;
		setValue(proto.getDoubleValue());
	}
	
}

package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractProperty;

/**
   A single integer property.
 */
public class IntProperty extends AbstractProperty {
    private int value;

    /**
       Construct an IntProperty with no defined value.
       @param urn The urn of this property.
    */
    public IntProperty(String urn) {
        super(urn);
    }

    /**
       Construct an IntProperty with no defined value.
       @param urn The urn of this property.
    */
    public IntProperty(Enum<?> urn) {
        super(urn);
    }

    /**
       Construct an IntProperty with a defined value.
       @param urn The urn of this property.
       @param value The initial value of the property.
    */
    public IntProperty(String urn, int value) {
        super(urn, true);
        this.value = value;
    }

    /**
       Construct an IntProperty with a defined value.
       @param urn The urn of this property.
       @param value The initial value of the property.
    */
    public IntProperty(Enum<?> urn, int value) {
        super(urn, true);
        this.value = value;
    }

    /**
       IntProperty copy constructor.
       @param other The IntProperty to copy.
     */
    public IntProperty(IntProperty other) {
        super(other);
        this.value = other.value;
    }

    @Override
    public Integer getValue() {
        if (!isDefined()) {
            return null;
        }
        return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
    */
    public void setValue(int value) {
        int old = this.value;
        boolean wasDefined = isDefined();
        this.value = value;
        setDefined();
        if (!wasDefined || old != value) {
            fireChange(old, value);
        }
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof IntProperty) {
            IntProperty i = (IntProperty)p;
            if (i.isDefined()) {
                setValue(i.getValue());
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
        writeInt32(value, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        setValue(readInt32(in));
    }

    @Override
    public IntProperty copy() {
        return new IntProperty(this);
    }
}

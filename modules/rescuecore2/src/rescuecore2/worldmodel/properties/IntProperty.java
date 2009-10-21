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

    /**
       Get the value of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
    */
    public int getValue() {
        return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
    */
    public void setValue(int value) {
        this.value = value;
        setDefined();
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
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getURN());
        if (isDefined()) {
            result.append(" = ");
            result.append(value);
        }
        else {
            result.append(" (undefined)");
        }
        return result.toString();
    }
}
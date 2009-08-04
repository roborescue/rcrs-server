package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readDouble;
import static rescuecore2.misc.EncodingTools.writeDouble;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.PropertyType;
import rescuecore2.worldmodel.AbstractProperty;

/**
   A single double-precision floating point number property.
 */
public class DoubleProperty extends AbstractProperty {
    private double value;

    /**
       Construct a DoubleProperty with no defined value.
       @param type The type of this property.
    */
    public DoubleProperty(PropertyType type) {
        super(type);
    }

    /**
       Construct a DoubleProperty with a defined value.
       @param type The type of this property.
       @param value The initial value of the property.
    */
    public DoubleProperty(PropertyType type, double value) {
        super(type, true);
        this.value = value;
    }

    /**
       Get the value of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
    */
    public double getValue() {
        return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
    */
    public void setValue(double value) {
        this.value = value;
        setDefined();
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
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getName());
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
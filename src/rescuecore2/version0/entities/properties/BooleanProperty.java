package rescuecore2.version0.entities.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   A boolean property.
 */
public class BooleanProperty extends RescueProperty {
    private boolean value;

    /**
       Construct a BooleanProperty with no defined value.
       @param type The type of this property.
     */
    public BooleanProperty(PropertyType type) {
        super(type);
    }

    /**
       Construct a BooleanProperty with a defined value.
       @param type The type of this property.
       @param value The initial value of the property.
     */
    public BooleanProperty(PropertyType type, boolean value) {
        super(type, true);
        this.value = value;
    }

    /**
       Get the value of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
     */
    public boolean getValue() {
        return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
     */
    public void setValue(boolean value) {
        this.value = value;
        setDefined(true);
    }

    /**
       Clear the value of this property. Future calls to {@link #isDefined()} will return false.
     */
    public void clearValue() {
        setDefined(false);
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
package rescuecore2.version0.entities.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import rescuecore2.worldmodel.EntityID;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   A property that refers to an entity ID.
 */
public class EntityRefProperty extends RescueProperty {
    private EntityID value;

    /**
       Construct an EntityRefProperty with no defined value.
       @param type The type of this property.
     */
    public EntityRefProperty(PropertyType type) {
	super(type);
    }

    /**
       Construct an EntityRefProperty with a defined value.
       @param type The type of this property.
       @param value The initial value of the property.
     */
    public EntityRefProperty(PropertyType type, EntityID value) {
	super(type, true);
	this.value = value;
    }

    /**
       Get the value of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
     */
    public EntityID getValue() {
	return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
     */
    public void setValue(EntityID value) {
	this.value = value;
	setDefined(true);
    }

    /**
       Clear the value of this property. Future calls to {@link #isDefined()} will return false.
     */
    public void clearValue() {
	value = null;
	setDefined(false);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(value.getValue(), out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        value = new EntityID(readInt32(in));
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
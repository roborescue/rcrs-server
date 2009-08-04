package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.PropertyType;
import rescuecore2.worldmodel.AbstractProperty;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   A property that refers to an entity ID.
 */
public class EntityRefProperty extends AbstractProperty {
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
        setDefined();
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof EntityRefProperty) {
            EntityRefProperty e = (EntityRefProperty)p;
            if (e.isDefined()) {
                setValue(e.getValue());
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
        writeInt32(value.getValue(), out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        setValue(new EntityID(readInt32(in)));
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
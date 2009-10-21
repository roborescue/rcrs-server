package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractProperty;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
   A property that refers to a list of entity IDs.
 */
public class EntityRefListProperty extends AbstractProperty {
    private List<EntityID> ids;

    /**
       Construct an EntityRefListProperty with no defined value.
       @param urn The urn of this property.
    */
    public EntityRefListProperty(String urn) {
        super(urn);
        ids = new ArrayList<EntityID>();
    }

    /**
       Construct an EntityRefListProperty with no defined value.
       @param urn The urn of this property.
    */
    public EntityRefListProperty(Enum<?> urn) {
        super(urn);
        ids = new ArrayList<EntityID>();
    }

    /**
       Construct an EntityRefListProperty with a defined value.
       @param urn The urn of this property.
       @param ids The initial value of the property.
    */
    public EntityRefListProperty(String urn, List<EntityID> ids) {
        super(urn, true);
        this.ids = new ArrayList<EntityID>(ids);
    }

    /**
       Construct an EntityRefListProperty with a defined value.
       @param urn The urn of this property.
       @param ids The initial value of the property.
    */
    public EntityRefListProperty(Enum<?> urn, List<EntityID> ids) {
        super(urn, true);
        this.ids = new ArrayList<EntityID>(ids);
    }

    /**
       EntityRefListProperty copy constructor.
       @param other The EntityRefListProperty to copy.
     */
    public EntityRefListProperty(EntityRefListProperty other) {
        super(other);
        this.ids = new ArrayList<EntityID>(other.ids);
    }

    /**
       Get the list of EntityIDs. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
    */
    public List<EntityID> getValue() {
        return Collections.unmodifiableList(ids);
    }

    /**
       Set the list of ids. Future calls to {@link #isDefined()} will return true.
       @param newIDs The new id list.
    */
    public void setValue(List<EntityID> newIDs) {
        ids.clear();
        ids.addAll(newIDs);
        setDefined();
    }

    /**
       Add a value to the list.
       @param id The id to add.
     */
    public void addValue(EntityID id) {
        ids.add(id);
        setDefined();
    }

    /**
       Remove all entries from this list but keep it defined.
     */
    public void clearValues() {
        ids.clear();
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof EntityRefListProperty) {
            EntityRefListProperty e = (EntityRefListProperty)p;
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
        writeInt32(ids.size(), out);
        for (EntityID next : ids) {
            writeInt32(next.getValue(), out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException {
        int count = readInt32(in);
        ids.clear();
        for (int i = 0; i < count; ++i) {
            ids.add(new EntityID(readInt32(in)));
        }
        setDefined();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getURN());
        if (isDefined()) {
            result.append(" = {");
            for (Iterator<EntityID> it = ids.iterator(); it.hasNext();) {
                result.append(it.next());
                if (it.hasNext()) {
                    result.append(", ");
                }
            }
            result.append("}");
        }
        else {
            result.append(" (undefined)");
        }
        return result.toString();
    }
}
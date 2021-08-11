package rescuecore2.worldmodel;

/**
   Abstract base class for Property implementations.
*/
public abstract class AbstractProperty implements Property {
    private boolean defined;
    private final String urn;
    // CHECKSTYLE:OFF:IllegalType
    private AbstractEntity entity;
    // CHECKSTYLE:ON:IllegalType

    /**
       Construct a property with a given type and assume that the value of this property is initially undefined.
       @param urn The urn of the property.
     */
    protected AbstractProperty(String urn) {
        this(urn, false);
    }

    /**
       Construct a property with a given type and assume that the value of this property is initially undefined.
       @param urn The urn of the property.
     */
    protected AbstractProperty(Enum<?> urn) {
        this(urn.toString(), false);
    }

    /**
       Construct a property with a given type and whether the value of this property is initially defined or not.
       @param urn The urn of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(String urn, boolean defined) {
        this.urn = urn;
        this.defined = defined;
        entity = null;
    }

    /**
       Construct a property with a given type and whether the value of this property is initially defined or not.
       @param urn The urn of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(Enum<?> urn, boolean defined) {
        this(urn.toString(), defined);
    }

    /**
       AbstractProperty copy constructor.
       @param other The AbstractProperty to copy.
     */
    protected AbstractProperty(AbstractProperty other) {
        this(other.getURN(), other.isDefined());
    }

    /**
       Set the property status to defined.
     */
    protected void setDefined() {
        defined = true;
    }

    /**
       Set this property's containing Entity.
       @param e The AbstractEntity that holds this property.
    */
    // CHECKSTYLE:OFF:IllegalType
    protected void setEntity(AbstractEntity e) {
        // CHECKSTYLE:ON:IllegalType
        entity = e;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public void undefine() {
        Object old = getValue();
        defined = false;
        fireChange(old, null);
    }

    @Override
    public String getURN() {
        return urn;
    }

    /**
       Notify the entity that this property has changed.
       @param oldValue The old value of this property.
       @param newValue The new value of this property.
    */
    protected void fireChange(Object oldValue, Object newValue) {
        if (entity != null) {
            entity.firePropertyChanged(this, oldValue, newValue);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getURN());
        if (isDefined()) {
            result.append(" = ");
            result.append(getValue());
        }
        else {
            result.append(" (undefined)");
        }
        return result.toString();
    }
}

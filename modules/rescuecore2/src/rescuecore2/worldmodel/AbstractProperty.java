package rescuecore2.worldmodel;

/**
   Abstract base class for Property implementations.
*/
public abstract class AbstractProperty implements Property {
    private boolean defined;
    private final PropertyType type;

    /**
       Construct a property with a given type and assume that the value of this property is initially undefined.
       @param type The type of the property.
     */
    protected AbstractProperty(PropertyType type) {
        this(type, false);
    }

    /**
       Construct a property with a given type and whether the value of this property is initially defined or not.
       @param type The type of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(PropertyType type, boolean defined) {
        this.type = type;
        this.defined = defined;
    }

    /**
       AbstractProperty copy constructor.
       @param other The AbstractProperty to copy.
     */
    protected AbstractProperty(AbstractProperty other) {
        this(other.getType(), other.isDefined());
    }

    /**
       Set the property status to defined.
     */
    protected void setDefined() {
        defined = true;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public void undefine() {
        defined = false;
    }

    @Override
    public PropertyType getType() {
        return type;
    }

    @Override
    public int getID() {
        return type.getID();
    }

    @Override
    public String getName() {
        return type.getName();
    }
}
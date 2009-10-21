package rescuecore2.worldmodel;

/**
   Abstract base class for Property implementations.
*/
public abstract class AbstractProperty implements Property {
    private boolean defined;
    private final String urn;

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
        this(urn.name(), false);
    }

    /**
       Construct a property with a given type and whether the value of this property is initially defined or not.
       @param urn The urn of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(String urn, boolean defined) {
        this.urn = urn;
        this.defined = defined;
    }

    /**
       Construct a property with a given type and whether the value of this property is initially defined or not.
       @param urn The urn of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(Enum<?> urn, boolean defined) {
        this(urn.name(), defined);
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

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public void undefine() {
        defined = false;
    }

    @Override
    public String getURN() {
        return urn;
    }
}
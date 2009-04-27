package rescuecore2.worldmodel;

/**
   Abstract base class for Property implementations.
*/
public abstract class AbstractProperty implements Property {
    private boolean defined;
    private int id;

    /**
       Construct a property with a given id and assume that the value of this property is initially undefined.
       @param id The ID of the property.
     */
    protected AbstractProperty(int id) {
        this(id, false);
    }

    /**
       Construct a property with a given id and whether the value of this property is initially defined or not.
       @param id The ID of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(int id, boolean defined) {
        this.id = id;
        this.defined = defined;
    }

    /**
       Set whether the value is defined or not.
       @param b The new defined status.
     */
    protected void setDefined(boolean b) {
        defined = b;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public int getID() {
        return id;
    }
}
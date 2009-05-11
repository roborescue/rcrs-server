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
    public int getID() {
        return id;
    }
}
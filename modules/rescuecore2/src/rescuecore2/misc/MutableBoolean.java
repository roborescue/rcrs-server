package rescuecore2.misc;

/**
   A mutable boolean value.
*/
public class MutableBoolean {
    private boolean b;

    /**
       Create a mutable boolean value.
       @param b The initial value.
    */
    public MutableBoolean(boolean b) {
        this.b = b;
    }

    /**
       Set the value of this object.
       @param value The new value.
    */
    public void set(boolean value) {
        b = value;
    }

    /**
       Get the value of this object.
       @return The value.
    */
    public boolean get() {
        return b;
    }
}
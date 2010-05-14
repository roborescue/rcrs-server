package maps.legacy;

import java.io.InputStream;
import java.io.IOException;

/**
   Abstract base class for legacy objects.
*/
public abstract class LegacyObject {
    /** The ID of this object. */
    protected int id;
    /** The X coordinate. */
    protected int x;
    /** The Y coordinate. */
    protected int y;

    /**
       Read the data for this object.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public abstract void read(InputStream in) throws IOException;

    /**
       Get the ID of this object.
       @return The object ID.
    */
    public int getID() {
        return id;
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
    */
    public int getX() {
        return x;
    }

    /**
       Get the Y coordinate.
       @return The Y coordinate.
    */
    public int getY() {
        return y;
    }
}
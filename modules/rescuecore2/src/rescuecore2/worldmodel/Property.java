package rescuecore2.worldmodel;

import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;

import java.io.IOException;

/**
   Interface for the properties that make up an entity.
 */
public interface Property {
    /**
       Get the urn of this property.
       @return The urn of this property.
     */
    int getURN();

    /**
       Does this property have a defined value?
       @return True if a value has been set for this property, false otherwise.
     */
    boolean isDefined();

    /**
       Undefine the value of this property. Future calls to {@link #isDefined()} will return false.
     */
    void undefine();

    /**
       Take on the value of another property.
       @param other The other property to inspect.
       @throws IllegalArgumentException If the other property is the wrong type.
     */
    void takeValue(Property other);

    /**
       Write this property to a stream.
       @param out The stream to write to.
       @throws IOException If the write fails.
     */
    void write(OutputStream out) throws IOException;

    /**
       Read this property from a stream.
       @param in The stream to read from.
       @throws IOException If the read fails.
     */
    void read(InputStream in) throws IOException;

    /**
       Get the value of this property. If the property is undefined then the return value should be null.
       @return The value of this property.
    */
    Object getValue();

    /**
       Create a copy of this property.
       @return A copy of this property.
    */
    Property copy();
    
    PropertyProto toPropertyProto();
    void fromPropertyProto(PropertyProto proto);
}


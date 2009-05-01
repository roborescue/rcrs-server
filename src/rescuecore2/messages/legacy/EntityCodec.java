package rescuecore2.worldmodel;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Collection;

/**
   This class is responsible for turning Entities into byte arrays and vice versa.
 */
public interface EntityCodec {
    /**
       Write an entity to an output stream.
       @param e The Entity to encode.
       @param out The stream to write it to.
       @throws IOException If writing to the stream fails.
     */
    void encode(Entity e, OutputStream out) throws IOException;

    /**
       Write a set of entities to an output stream.
       @param e The Entities to encode.
       @param out The stream to write them to.
       @throws IOException If writing to the stream fails.
     */
    void encode(Collection<Entity> e, OutputStream out) throws IOException;

    /**
       Read an entity from an input stream.
       @param in The stream to read.
       @return A new Entity object, or null if the stream contains no further entities.
       @throws IOException If reading from the stream fails.
     */
    Entity decode(InputStream in) throws IOException;

    /**
       Read a set of entities from an input stream.
       @param in The stream to read.
       @return A set of new Entity objects, or an empty collection if the stream contains no further entities.
       @throws IOException If reading from the stream fails.
     */
    Collection<Entity> decodeEntities(InputStream in) throws IOException;
}
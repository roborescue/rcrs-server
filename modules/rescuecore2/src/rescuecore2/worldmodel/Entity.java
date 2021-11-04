package rescuecore2.worldmodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.json.JSONObject;

import rescuecore2.messages.protobuf.RCRSProto.EntityProto;

/**
 * Interface for all objects that live in a WorldModel. Entities are made up of
 * a fixed set of properties. The values of those properties may change but the
 * set of properties may not.
 */
public interface Entity {

  /**
   * Add an EntityListener.
   *
   * @param l The listener to add.
   */
  void addEntityListener(EntityListener l);

  /**
   * Remove an EntityListener.
   *
   * @param l The listener to remove.
   */
  void removeEntityListener(EntityListener l);

  /**
   * Get the ID of this Entity.
   *
   * @return The ID.
   */
  EntityID getID();

  /**
   * Get the urn of this Entity.
   *
   * @return The type.
   */
  int getURN();

  /**
   * Get all the properties that this entity has.
   *
   * @return A set of Properties. This will never be null, but may be empty.
   */
  Set<Property> getProperties();

  /**
   * Get a property by urn.
   *
   * @param urn The urn to look up.
   * @return The property with the given urn or null if no such property exists.
   */
  Property getProperty(int urn);

  /**
   * Write this Entity to a stream.
   *
   * @param out The stream to write to.
   * @throws IOException If the write fails.
   */
  void write(OutputStream out) throws IOException;

  /**
   * Read this Entity from a stream.
   *
   * @param in The stream to read from.
   * @throws IOException If the read fails.
   */
  void read(InputStream in) throws IOException;

  /**
   * Create a copy of this entity.
   *
   * @return A new Entity with the same ID and property values.
   */
  Entity copy();

  /**
   * Return the Entity state in JSON format
   *
   * @return Entity states
   */
  JSONObject toJson();

  EntityProto toEntityProto();

  void fromEntityProto(EntityProto proto);
}
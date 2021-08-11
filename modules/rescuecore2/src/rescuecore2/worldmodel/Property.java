package rescuecore2.worldmodel;

import java.util.List;

/**
 * Interface for the properties that make up an entity.
 */
public interface Property<T> {

  /**
   * Get the urn of this property.
   *
   * @return The urn of this property.
   */
  String getURN();

  /**
   * Does this property have a defined value?
   *
   * @return True if a value has been set for this property, false otherwise.
   */
  boolean isDefined();

  /**
   * Undefine the value of this property. Future calls to {@link #isDefined()}
   * will return false.
   */
  void undefine();

  /**
   * Take on the value of another property.
   *
   * @param other
   *          The other property to inspect.
   * @throws IllegalArgumentException
   *           If the other property is the wrong type.
   */
  void takeValue( Property<?> other );

  /**
   * Get the value of this property. If the property is undefined then the
   * return value should be null.
   *
   * @return The value of this property.
   */
  T getValue();

  /**
   * Create a copy of this property.
   *
   * @return A copy of this property.
   */
  Property<T> copy();

  /**
   * Set this property with the content of the object
   *
   * @param fields
   *          The list content to set the property object.
   */
  void setFields( List<Object> fields );

  /**
   * Get the property in the list format
   *
   * @return The property object in list format.
   *
   */
  List<Object> getFields();

  /**
   * Convert the list format with the content in the list content
   *
   * @param fields
   *          The list content to convert to the property value format.
   * @return Property value
   */
  T convertToValue( List<Object> fields );
}
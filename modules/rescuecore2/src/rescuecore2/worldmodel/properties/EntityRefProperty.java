package rescuecore2.worldmodel.properties;

import java.util.ArrayList;
import java.util.List;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A property that refers to an entity ID.
 */
public class EntityRefProperty extends AbstractProperty<EntityID> {

  public static final int VALUE = 0;

  private EntityID        value;


  /**
   * Construct an EntityRefProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public EntityRefProperty( String urn ) {
    super( urn );
  }


  /**
   * Construct an EntityRefProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public EntityRefProperty( Enum<?> urn ) {
    super( urn );
  }


  /**
   * Construct an EntityRefProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public EntityRefProperty( String urn, EntityID value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * Construct an EntityRefProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public EntityRefProperty( Enum<?> urn, EntityID value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * EntityRefProperty copy constructor.
   *
   * @param other
   *          The EntityRefProperty to copy.
   */
  public EntityRefProperty( EntityRefProperty other ) {
    super( other );
    this.value = other.value;
  }


  @Override
  public EntityID getValue() {
    if ( !isDefined() ) {
      return null;
    }
    return value;
  }


  /**
   * Set the value of this property. Future calls to {@link #isDefined()} will
   * return true.
   *
   * @param value
   *          The new value.
   */
  public void setValue( EntityID value ) {
    EntityID old = this.value;
    boolean wasDefined = isDefined();
    this.value = value;
    setDefined();
    if ( !wasDefined || !old.equals( value ) ) {
      fireChange( old, value );
    }
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof EntityRefProperty ) {
      EntityRefProperty e = (EntityRefProperty) p;
      if ( e.isDefined() ) {
        setValue( e.getValue() );
      } else {
        undefine();
      }
    } else {
      throw new IllegalArgumentException(
          this + " cannot take value from " + p );
    }
  }


  @Override
  public EntityRefProperty copy() {
    return new EntityRefProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.value = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();
    fields.add( EntityRefProperty.VALUE, this.value.getValue() );

    return fields;
  }


  @Override
  public EntityID convertToValue( List<Object> fields ) {
    return new EntityID( (int) fields.get( EntityRefProperty.VALUE ) );
  }
}
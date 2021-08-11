package rescuecore2.worldmodel.properties;

import java.util.ArrayList;
import java.util.List;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.Property;

/**
 * A single integer property.
 */
public class IntProperty extends AbstractProperty<Integer> {

  public static final int VALUE = 0;

  private int             value;


  /**
   * Construct an IntProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public IntProperty( String urn ) {
    super( urn );
  }


  /**
   * Construct an IntProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public IntProperty( Enum<?> urn ) {
    super( urn );
  }


  /**
   * Construct an IntProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public IntProperty( String urn, int value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * Construct an IntProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public IntProperty( Enum<?> urn, int value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * IntProperty copy constructor.
   *
   * @param other
   *          The IntProperty to copy.
   */
  public IntProperty( IntProperty other ) {
    super( other );
    this.value = other.value;
  }


  @Override
  public Integer getValue() {
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
  public void setValue( int value ) {
    int old = this.value;
    boolean wasDefined = isDefined();
    this.value = value;
    setDefined();
    if ( !wasDefined || old != value ) {
      fireChange( old, value );
    }
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof IntProperty ) {
      IntProperty i = (IntProperty) p;
      if ( i.isDefined() ) {
        setValue( i.getValue() );
      } else {
        undefine();
      }
    } else {
      throw new IllegalArgumentException(
          this + " cannot take value from " + p );
    }
  }


  @Override
  public IntProperty copy() {
    return new IntProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.value = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();
    fields.add( IntArrayProperty.VALUE, this.value );

    return fields;
  }


  @Override
  public Integer convertToValue( List<Object> fields ) {
    return (int) fields.get( IntProperty.VALUE );
  }
}
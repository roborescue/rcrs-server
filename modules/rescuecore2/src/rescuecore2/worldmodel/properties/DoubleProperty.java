package rescuecore2.worldmodel.properties;

import java.util.ArrayList;
import java.util.List;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.Property;

/**
 * A single double-precision floating point number property.
 */
public class DoubleProperty extends AbstractProperty<Double> {

  public static final int VALUE = 0;

  private double          value;


  /**
   * Construct a DoubleProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public DoubleProperty( String urn ) {
    super( urn );
  }


  /**
   * Construct a DoubleProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public DoubleProperty( Enum<?> urn ) {
    super( urn );
  }


  /**
   * Construct a DoubleProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public DoubleProperty( String urn, double value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * Construct a DoubleProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public DoubleProperty( Enum<?> urn, double value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * DoubleProperty copy constructor.
   *
   * @param other
   *          The DoubleProperty to copy.
   */
  public DoubleProperty( DoubleProperty other ) {
    super( other );
    this.value = other.value;
  }


  @Override
  public Double getValue() {
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
  public void setValue( double value ) {
    double old = this.value;
    boolean wasDefined = isDefined();
    this.value = value;
    setDefined();
    if ( !wasDefined || old != value ) {
      fireChange( old, value );
    }
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof DoubleProperty ) {
      DoubleProperty d = (DoubleProperty) p;
      if ( d.isDefined() ) {
        setValue( d.getValue() );
      } else {
        undefine();
      }
    } else {
      throw new IllegalArgumentException(
          this + " cannot take value from " + p );
    }
  }


  @Override
  public DoubleProperty copy() {
    return new DoubleProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.value = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();
    fields.add( DoubleProperty.VALUE, this.value );

    return fields;
  }


  @Override
  public Double convertToValue( List<Object> fields ) {
    return (Double) fields.get( DoubleProperty.VALUE );
  }
}
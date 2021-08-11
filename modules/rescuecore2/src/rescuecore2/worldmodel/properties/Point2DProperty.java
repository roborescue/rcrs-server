package rescuecore2.worldmodel.properties;

import java.util.ArrayList;
import java.util.List;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractProperty;

/**
 * A Point2D property.
 */
public class Point2DProperty extends AbstractProperty<Point2D> {

  public static final int X = 0;
  public static final int Y = 1;

  private Point2D         value;


  /**
   * Construct a Point2DProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public Point2DProperty( String urn ) {
    super( urn );
  }


  /**
   * Construct a Point2DProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public Point2DProperty( Enum<?> urn ) {
    super( urn );
  }


  /**
   * Construct a Point2DProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public Point2DProperty( String urn, Point2D value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * Construct a Point2DProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param value
   *          The initial value of the property.
   */
  public Point2DProperty( Enum<?> urn, Point2D value ) {
    super( urn, true );
    this.value = value;
  }


  /**
   * Point2DProperty copy constructor.
   *
   * @param other
   *          The Point2DProperty to copy.
   */
  public Point2DProperty( Point2DProperty other ) {
    super( other );
    this.value = other.value;
  }


  @Override
  public Point2D getValue() {
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
  public void setValue( Point2D value ) {
    this.value = value;
    setDefined();
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof Point2DProperty ) {
      Point2DProperty i = (Point2DProperty) p;
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
  public Point2DProperty copy() {
    return new Point2DProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.value = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();
    fields.add( Point2DProperty.X, this.value.getX() );
    fields.add( Point2DProperty.Y, this.value.getY() );

    return fields;
  }


  @Override
  public Point2D convertToValue( List<Object> fields ) {
    return new Point2D( (double) fields.get( Point2DProperty.X ),
        (double) fields.get( Point2DProperty.Y ) );
  }
}
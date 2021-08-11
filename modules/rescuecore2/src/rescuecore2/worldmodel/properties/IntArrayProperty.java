package rescuecore2.worldmodel.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.Property;

/**
 * An integer-array property.
 */
public class IntArrayProperty extends AbstractProperty<List<Integer>> {

  public static final int VALUE = 0;

  private List<Integer>   data;


  /**
   * Construct an IntArrayProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public IntArrayProperty( String urn ) {
    super( urn );
    data = new ArrayList<Integer>();
  }


  /**
   * Construct an IntArrayProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public IntArrayProperty( Enum<?> urn ) {
    super( urn );
    data = new ArrayList<Integer>();
  }


  /**
   * Construct an IntArrayProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param values
   *          The initial values of the property.
   */
  public IntArrayProperty( String urn, int[] values ) {
    super( urn, true );
    data = new ArrayList<Integer>( values.length );
    for ( Integer next : values ) {
      data.add( next );
    }
  }


  /**
   * Construct an IntArrayProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param values
   *          The initial values of the property.
   */
  public IntArrayProperty( Enum<?> urn, int[] values ) {
    super( urn, true );
    data = new ArrayList<Integer>( values.length );
    for ( Integer next : values ) {
      data.add( next );
    }
  }


  /**
   * IntArrayProperty copy constructor.
   *
   * @param other
   *          The IntArrayProperty to copy.
   */
  public IntArrayProperty( IntArrayProperty other ) {
    super( other );
    this.data = new ArrayList<Integer>( other.data );
  }


  @Override
  public List<Integer> getValue() {
    if ( !isDefined() ) {
      return null;
    }
    return Collections.unmodifiableList( data );
  }


  /**
   * Set the value of this property. Future calls to {@link #isDefined()} will
   * return true.
   *
   * @param values
   *          The new values.
   */
  public void setValue( List<Integer> values ) {
    List<Integer> old = getValue();
    this.data = new ArrayList<Integer>();
    for ( Integer next : values ) {
      data.add( next );
    }
    setDefined();
    fireChange( old, getValue() );
  }


  /**
   * Add a value to the array.
   *
   * @param i
   *          The value to add.
   */
  public void push( int i ) {
    List<Integer> old = getValue();
    setDefined();
    data.add( i );
    fireChange( old, getValue() );
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof IntArrayProperty ) {
      IntArrayProperty i = (IntArrayProperty) p;
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
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append( getURN() );
    if ( isDefined() ) {
      result.append( " = {" );
      for ( Iterator<Integer> it = data.iterator(); it.hasNext(); ) {
        result.append( it.next() );
        if ( it.hasNext() ) {
          result.append( ", " );
        }
      }
      result.append( "}" );
    } else {
      result.append( " (undefined)" );
    }
    return result.toString();
  }


  @Override
  public IntArrayProperty copy() {
    return new IntArrayProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.data = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();

    int[] data = new int[this.data.size()];
    for ( int i = 0; i < this.data.size(); i++ ) {
      data[i] = this.data.get( i );
    }
    fields.add( IntArrayProperty.VALUE, data );

    return fields;
  }


  @Override
  public List<Integer> convertToValue( List<Object> fields ) {
    List<Integer> value = new ArrayList<Integer>();

    int[] data = (int[]) fields.get( IntArrayProperty.VALUE );
    for ( int i = 0; i < data.length; i++ ) {
      value.add( data[i] );
    }
    return value;
  }
}
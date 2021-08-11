package rescuecore2.worldmodel.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A property that refers to a list of entity IDs.
 */
public class EntityRefListProperty extends AbstractProperty<List<EntityID>> {

  public static final int VALUE = 0;

  private List<EntityID>  ids;


  /**
   * Construct an EntityRefListProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public EntityRefListProperty( String urn ) {
    super( urn );
    ids = new ArrayList<EntityID>();
  }


  /**
   * Construct an EntityRefListProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public EntityRefListProperty( Enum<?> urn ) {
    super( urn );
    ids = new ArrayList<EntityID>();
  }


  /**
   * Construct an EntityRefListProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param ids
   *          The initial value of the property.
   */
  public EntityRefListProperty( String urn, List<EntityID> ids ) {
    super( urn, true );
    this.ids = new ArrayList<EntityID>( ids );
  }


  /**
   * Construct an EntityRefListProperty with a defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param ids
   *          The initial value of the property.
   */
  public EntityRefListProperty( Enum<?> urn, List<EntityID> ids ) {
    super( urn, true );
    this.ids = new ArrayList<EntityID>( ids );
  }


  /**
   * EntityRefListProperty copy constructor.
   *
   * @param other
   *          The EntityRefListProperty to copy.
   */
  public EntityRefListProperty( EntityRefListProperty other ) {
    super( other );
    this.ids = new ArrayList<EntityID>( other.ids );
  }


  @Override
  public List<EntityID> getValue() {
    if ( !isDefined() ) {
      return null;
    }
    return Collections.unmodifiableList( ids );
  }


  /**
   * Set the list of ids. Future calls to {@link #isDefined()} will return true.
   *
   * @param newIDs
   *          The new id list.
   */
  public void setValue( List<EntityID> newIDs ) {
    List<EntityID> old = new ArrayList<EntityID>( ids );
    ids.clear();
    ids.addAll( newIDs );
    setDefined();
    fireChange( old, Collections.unmodifiableList( ids ) );
  }


  /**
   * Add a value to the list.
   *
   * @param id
   *          The id to add.
   */
  public void addValue( EntityID id ) {
    List<EntityID> old = new ArrayList<EntityID>( ids );
    ids.add( id );
    setDefined();
    fireChange( old, Collections.unmodifiableList( ids ) );
  }


  /**
   * Remove a value from the list.
   *
   * @param id
   *          The id to remove.
   */
  public void removeValue( EntityID id ) {
    List<EntityID> old = new ArrayList<EntityID>( ids );
    ids.remove( id );

    if ( ids.isEmpty() ) undefine();

    fireChange( old, Collections.unmodifiableList( ids ) );
  }


  /**
   * Remove all entries from this list but keep it defined.
   */
  public void clearValues() {
    List<EntityID> old = new ArrayList<EntityID>( ids );
    ids.clear();
    fireChange( old, Collections.unmodifiableList( ids ) );
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof EntityRefListProperty ) {
      EntityRefListProperty e = (EntityRefListProperty) p;
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
  public EntityRefListProperty copy() {
    return new EntityRefListProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.ids = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();

    int[] ids = new int[this.ids.size()];
    for ( int i = 0; i < this.ids.size(); i++ ) {
      ids[i] = this.ids.get( i ).getValue();
    }
    fields.add( EntityRefListProperty.VALUE, ids );

    return fields;
  }


  @Override
  public List<EntityID> convertToValue( List<Object> fields ) {
    List<EntityID> ids = new ArrayList<EntityID>();

    int[] values = (int[]) fields.get( EntityRefListProperty.VALUE );
    for ( int i = 0; i < values.length; i++ ) {
      ids.add( new EntityID( values[i] ) );
    }

    return ids;
  }
}
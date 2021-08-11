package rescuecore2.worldmodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import rescuecore2.log.Logger;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;

/**
 * This class is used for accumulating changes to entities.
 */
public class ChangeSet {

  private Map<EntityID, Map<String, Property<?>>> changes;
  private Set<EntityID>                           deletes;
  private Map<EntityID, String>                   entityURNs;


  /**
   * Create an empty ChangeSet.
   */
  public ChangeSet() {
    this.changes = new LazyMap<EntityID, Map<String, Property<?>>>() {

      @Override
      public Map<String, Property<?>> createValue() {
        return new HashMap<String, Property<?>>();
      }
    };
    this.entityURNs = new HashMap<EntityID, String>();
    this.deletes = new HashSet<EntityID>();
  }


  /**
   * Copy constructor.
   *
   * @param other
   *          The ChangeSet to copy.
   */
  public ChangeSet( ChangeSet other ) {
    this();
    this.merge( other );
  }


  /**
   * Add a change.
   *
   * @param e
   *          The entity that has changed.
   * @param p
   *          The property that has changed.
   */
  public void addChange( Entity e, Property<?> p ) {
    this.addChange( e.getID(), e.getURN(), p );
  }


  /**
   * Add a change.
   *
   * @param e
   *          The ID of the entity that has changed.
   * @param urn
   *          The URN of the entity that has changed.
   * @param p
   *          The property that has changed.
   */
  public void addChange( EntityID e, String urn, Property<?> p ) {
    if ( this.deletes.contains( e ) ) {
      return;
    }
    Property<?> prop = p.copy();
    this.changes.get( e ).put( prop.getURN(), prop );
    this.entityURNs.put( e, urn );
  }


  /**
   * Register a deleted entity.
   *
   * @param e
   *          The ID of the entity that has been deleted.
   */
  public void entityDeleted( EntityID e ) {
    this.deletes.add( e );
    this.changes.remove( e );
  }


  /**
   * Get the properties that have changed for an entity.
   *
   * @param e
   *          The entity ID to look up.
   * @return The set of changed properties. This may be empty but will never be
   *         null.
   */
  public Set<Property<?>> getChangedProperties( EntityID e ) {
    return new HashSet<Property<?>>( this.changes.get( e ).values() );
  }


  /**
   * Look up a property change for an entity by property URN.
   *
   * @param e
   *          The entity ID to look up.
   * @param urn
   *          The property URN to look up.
   * @return The changed property with the right URN, or null if the property is
   *         not found or has not changed.
   */
  public Property<?> getChangedProperty( EntityID e, String urn ) {
    Map<String, Property<?>> props = changes.get( e );
    if ( props != null ) {
      return props.get( urn );
    }
    return null;
  }


  /**
   * Get the IDs of all changed entities.
   *
   * @return A set of IDs of changed entities.
   */
  public Set<EntityID> getChangedEntities() {
    return new HashSet<EntityID>( this.changes.keySet() );
  }


  /**
   * Get the IDs of all deleted entities.
   *
   * @return A set of IDs of deleted entities.
   */
  public Set<EntityID> getDeletedEntities() {
    return new HashSet<EntityID>( this.deletes );
  }


  /**
   * Get the URN of a changed entity.
   *
   * @param id
   *          The ID of the entity.
   * @return The URN of the changed entity.
   */
  public String getEntityURN( EntityID id ) {
    return this.entityURNs.get( id );
  }


  /**
   * Merge another ChangeSet into this one.
   *
   * @param other
   *          The other ChangeSet.
   */
  public void merge( ChangeSet other ) {
    for ( Map.Entry<EntityID, Map<String, Property<?>>> next : other.changes
        .entrySet() ) {
      EntityID e = next.getKey();
      String urn = other.getEntityURN( e );
      for ( Property<?> p : next.getValue().values() ) {

        if ( ( p instanceof EntityRefListProperty )
            && ( this.changes.get( e ).containsKey( urn ) && ( this.changes
                .get( e ).get( urn ) instanceof EntityRefListProperty ) ) ) {

          EntityRefListProperty bp1 = (EntityRefListProperty) p.copy();
          EntityRefListProperty bp2 = (EntityRefListProperty) this.changes
              .get( e ).get( urn );

          if ( bp2.isDefined() ) {
            for ( EntityID id : bp2.getValue() )
              bp1.addValue( id );
          }

          for ( EntityID id : this.deletes ) {
            bp1.removeValue( id );
          }

          for ( EntityID id : other.deletes ) {
            bp1.removeValue( id );
          }

          p = bp1;
        }

        this.addChange( e, urn, p );
      }
    }
    this.deletes.addAll( other.deletes );
  }


  /**
   * Add all defined properties from a collection.
   *
   * @param c
   *          The collection to copy changes from.
   */
  public void addAll( Collection<? extends Entity> c ) {
    for ( Entity entity : c ) {
      for ( Property<?> property : entity.getProperties() ) {
        if ( property.isDefined() ) {
          this.addChange( entity, property );
        }
      }
    }
  }


  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append( "ChangeSet:" );
    for ( Map.Entry<EntityID, Map<String, Property<?>>> next : this.changes
        .entrySet() ) {
      result.append( " Entity " );
      result.append( next.getKey() );
      result.append( " (" );
      result.append( getEntityURN( next.getKey() ) );
      result.append( ") [" );
      for ( Iterator<Property<?>> it = next.getValue().values().iterator(); it
          .hasNext(); ) {
        result.append( it.next() );
        if ( it.hasNext() ) {
          result.append( ", " );
        }
      }
      result.append( "]" );
    }
    result.append( " {Deleted " );
    for ( Iterator<EntityID> it = this.deletes.iterator(); it.hasNext(); ) {
      result.append( it.next() );
      if ( it.hasNext() ) {
        result.append( ", " );
      }
    }
    result.append( "}" );
    return result.toString();
  }


  /**
   * Write this changeset to Logger.debug in a readable form.
   */
  public void debug() {
    Logger.debug( "ChangeSet" );
    for ( Map.Entry<EntityID, Map<String, Property<?>>> next : this.changes
        .entrySet() ) {
      Logger.debug( "  Entity " + next.getKey() + "("
          + getEntityURN( next.getKey() ) + ")" );
      for ( Iterator<Property<?>> it = next.getValue().values().iterator(); it
          .hasNext(); ) {
        Logger.debug( "    " + it.next() );
      }
    }
    for ( Iterator<EntityID> it = this.deletes.iterator(); it.hasNext(); ) {
      Logger.debug( "  Deleted: " + it.next() );
    }
  }
}
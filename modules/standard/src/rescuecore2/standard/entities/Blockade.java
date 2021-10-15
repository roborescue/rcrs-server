package rescuecore2.standard.entities;

import java.awt.Polygon;
import java.awt.Shape;

import org.json.JSONObject;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * A blockade.
 */
public class Blockade extends StandardEntity {

  private IntProperty       x;
  private IntProperty       y;
  private EntityRefProperty position;
  private IntArrayProperty  apexes;
  private IntProperty       repairCost;

  private Shape             shape;


  /**
   * Construct a Blockade object with entirely undefined property values.
   *
   * @param id
   *          The ID of this entity.
   */
  public Blockade( EntityID id ) {
    super( id );
    x = new IntProperty( StandardPropertyURN.X );
    y = new IntProperty( StandardPropertyURN.Y );
    position = new EntityRefProperty( StandardPropertyURN.POSITION );
    apexes = new IntArrayProperty( StandardPropertyURN.APEXES );
    repairCost = new IntProperty( StandardPropertyURN.REPAIR_COST );
    registerProperties( x, y, position, apexes, repairCost );
    shape = null;
    addEntityListener( new ApexesListener() );
  }


  /**
   * Blockade copy constructor.
   *
   * @param other
   *          The Blockade to copy.
   */
  public Blockade( Blockade other ) {
    super( other );
    x = new IntProperty( other.x );
    y = new IntProperty( other.y );
    position = new EntityRefProperty( other.position );
    apexes = new IntArrayProperty( other.apexes );
    repairCost = new IntProperty( other.repairCost );
    registerProperties( x, y, position, apexes, repairCost );
    shape = null;
    addEntityListener( new ApexesListener() );
  }


  @Override
  public Pair<Integer, Integer>
      getLocation( WorldModel<? extends StandardEntity> world ) {
    if ( !x.isDefined() || !y.isDefined() ) {
      return null;
    }
    return new Pair<Integer, Integer>( x.getValue(), y.getValue() );
  }


  @Override
  protected Entity copyImpl() {
    return new Blockade( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.BLOCKADE;
  }


  @Override
  protected String getEntityName() {
    return "Blockade";
  }


  @Override
  public Property getProperty(int urn ) {
    StandardPropertyURN type;
    try {
      type = StandardPropertyURN.fromInt( urn );
    } catch ( IllegalArgumentException e ) {
      return super.getProperty( urn );
    }
    switch ( type ) {
      case X:
        return x;
      case Y:
        return y;
      case POSITION:
        return position;
      case APEXES:
        return apexes;
      case REPAIR_COST:
        return repairCost;
      default:
        return super.getProperty( urn );
    }
  }


  /**
   * Get the X property.
   *
   * @return The X property.
   */
  public IntProperty getXProperty() {
    return x;
  }


  /**
   * Get the X coordinate.
   *
   * @return The X coordinate.
   */
  public int getX() {
    return x.getValue();
  }


  /**
   * Set the X coordinate.
   *
   * @param x
   *          The new X coordinate.
   */
  public void setX( int x ) {
    this.x.setValue( x );
  }


  /**
   * Find out if the X property has been defined.
   *
   * @return True if the X property has been defined, false otherwise.
   */
  public boolean isXDefined() {
    return x.isDefined();
  }


  /**
   * Undefine the X property.
   */
  public void undefineX() {
    x.undefine();
  }


  /**
   * Get the Y property.
   *
   * @return The Y property.
   */
  public IntProperty getYProperty() {
    return y;
  }


  /**
   * Get the Y coordinate.
   *
   * @return The Y coordinate.
   */
  public int getY() {
    return y.getValue();
  }


  /**
   * Set the Y coordinate.
   *
   * @param y
   *          The new y coordinate.
   */
  public void setY( int y ) {
    this.y.setValue( y );
  }


  /**
   * Find out if the Y property has been defined.
   *
   * @return True if the Y property has been defined, false otherwise.
   */
  public boolean isYDefined() {
    return y.isDefined();
  }


  /**
   * Undefine the Y property.
   */
  public void undefineY() {
    y.undefine();
  }


  /**
   * Get the apexes property.
   *
   * @return The apexes property.
   */
  public IntArrayProperty getApexesProperty() {
    return apexes;
  }


  /**
   * Get the apexes of this area.
   *
   * @return The apexes.
   */
  public int[] getApexes() {
    return apexes.getValue();
  }


  /**
   * Set the apexes.
   *
   * @param apexes
   *          The new apexes.
   */
  public void setApexes( int[] apexes ) {
    this.apexes.setValue( apexes );
  }


  /**
   * Find out if the apexes property has been defined.
   *
   * @return True if the apexes property has been defined, false otherwise.
   */
  public boolean isApexesDefined() {
    return apexes.isDefined();
  }


  /**
   * Undefine the apexes property.
   */
  public void undefineApexes() {
    apexes.undefine();
  }


  /**
   * Get the position property.
   *
   * @return The position property.
   */
  public EntityRefProperty getPositionProperty() {
    return position;
  }


  /**
   * Get the position of this blockade.
   *
   * @return The position.
   */
  public EntityID getPosition() {
    return position.getValue();
  }


  /**
   * Set the position.
   *
   * @param position
   *          The new position.
   */
  public void setPosition( EntityID position ) {
    this.position.setValue( position );
  }


  /**
   * Find out if the position property has been defined.
   *
   * @return True if the position property has been defined, false otherwise.
   */
  public boolean isPositionDefined() {
    return position.isDefined();
  }


  /**
   * Undefine the position property.
   */
  public void undefinePosition() {
    position.undefine();
  }


  /**
   * Get the repair cost property.
   *
   * @return The repair cost property.
   */
  public IntProperty getRepairCostProperty() {
    return repairCost;
  }


  /**
   * Get the repair cost of this blockade.
   *
   * @return The repair cost.
   */
  public int getRepairCost() {
    return repairCost.getValue();
  }


  /**
   * Set the repair cost.
   *
   * @param cost
   *          The new repair cost.
   */
  public void setRepairCost( int cost ) {
    this.repairCost.setValue( cost );
  }


  /**
   * Find out if the repair cost property has been defined.
   *
   * @return True if the repair cost property has been defined, false otherwise.
   */
  public boolean isRepairCostDefined() {
    return repairCost.isDefined();
  }


  /**
   * Undefine the repair cost property.
   */
  public void undefineRepairCost() {
    repairCost.undefine();
  }


  /**
   * Get this area as a Java Shape object.
   *
   * @return A Shape describing this area.
   */
  public Shape getShape() {
    if ( shape == null ) {
      int[] allApexes = getApexes();
      int count = allApexes.length / 2;
      int[] xs = new int[count];
      int[] ys = new int[count];
      for ( int i = 0; i < count; ++i ) {
        xs[i] = allApexes[i * 2];
        ys[i] = allApexes[i * 2 + 1];
      }
      shape = new Polygon( xs, ys, count );
    }
    return shape;
  }


  private class ApexesListener implements EntityListener {

    @Override
    public void propertyChanged( Entity e, Property p, Object oldValue,
        Object newValue ) {
      if ( p == apexes ) {
        shape = null;
      }
    }
  }


  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put( StandardPropertyURN.APEXES.toString(),
        this.isApexesDefined() ? this.getApexes() : JSONObject.NULL );
    jsonObject.put( StandardPropertyURN.REPAIR_COST.toString(),
        this.isRepairCostDefined() ? this.getRepairCost() : JSONObject.NULL );

    return jsonObject;
  }
}

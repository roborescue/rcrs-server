package rescuecore2.standard.entities;

import java.util.List;
import java.util.Map;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * The Road object.
 */
public class Road extends Area {

  /**
   * Construct a Road object with entirely undefined property values.
   *
   * @param id
   *          The ID of this entity.
   */
  public Road( EntityID id ) {
    super( id );
  }


  /**
   * Road copy constructor.
   *
   * @param other
   *          The Road to copy.
   */
  public Road( Road other ) {
    super( other );
  }


  @Override
  protected Entity copyImpl() {
    return new Road( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.ROAD;
  }


  @Override
  protected String getEntityName() {
    return "Road";
  }


  @Override
  public void setEntity( Map<String, List<Object>> properties ) {
    StandardPropertyURN type;

    for ( String urn : properties.keySet() ) {
      List<Object> fields = properties.get( urn );

      type = StandardPropertyURN.fromString( urn );
      switch ( type ) {
        case X:
          this.setX( this.getXProperty().convertToValue( fields ) );
          break;
        case Y:
          this.setY( this.getYProperty().convertToValue( fields ) );
          break;
        case EDGES:
          this.setEdges( this.getEdgesProperty().convertToValue( fields ) );
          break;
        case BLOCKADES:
          this.setBlockades(
              this.getBlockadesProperty().convertToValue( fields ) );
          break;
        default:
      }
    }
  }
}
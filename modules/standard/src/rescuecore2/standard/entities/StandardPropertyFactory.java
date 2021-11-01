package rescuecore2.standard.entities;

import rescuecore2.registry.AbstractPropertyFactory;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * PropertyFactory that builds standard Robocup Standard properties.
 */
public final class StandardPropertyFactory extends AbstractPropertyFactory<StandardPropertyURN> {

  /**
   * Singleton class. Use this instance to do stuff.
   */
  public static final StandardPropertyFactory INSTANCE = new StandardPropertyFactory();

  /**
   * Singleton class: private constructor.
   */
  private StandardPropertyFactory() {
    super(StandardPropertyURN.class);
  }

  @Override
  public Property makeProperty(StandardPropertyURN urn) {
    switch (urn) {
      case START_TIME:
      case LONGITUDE:
      case LATITUDE:
      case WIND_FORCE:
      case WIND_DIRECTION:
      case X:
      case Y:
      case FLOORS:
      case BUILDING_ATTRIBUTES:
      case FIERYNESS:
      case BROKENNESS:
      case BUILDING_CODE:
      case BUILDING_AREA_GROUND:
      case BUILDING_AREA_TOTAL:
      case DIRECTION:
      case STAMINA:
      case HP:
      case DAMAGE:
      case BURIEDNESS:
      case WATER_QUANTITY:
      case TEMPERATURE:
      case IMPORTANCE:
      case TRAVEL_DISTANCE:
      case REPAIR_COST:
      case CAPACITY:
      case BED_CAPACITY:
      case REFILL_CAPACITY:
      case OCCUPIED_BEDS:
      case WAITING_LIST_SIZE:
        return new IntProperty(urn);
      case APEXES:
      case POSITION_HISTORY:
        return new IntArrayProperty(urn);
      case IGNITION:
        return new BooleanProperty(urn);
      case POSITION:
        return new EntityRefProperty(urn);
      case BLOCKADES:
        return new EntityRefListProperty(urn);
      case EDGES:
        return new EdgeListProperty(urn);
      default:
        throw new IllegalArgumentException("Unrecognised property urn: " + urn);
    }
  }
}
package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractPropertyFactory;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;


/**
   PropertyFactory that builds standard Robocup Standard properties.
 */
public final class StandardPropertyFactory extends AbstractPropertyFactory<StandardPropertyURN> {
    /**
       Singleton class. Use this instance to do stuff.
     */
    public static final StandardPropertyFactory INSTANCE = new StandardPropertyFactory();

    /**
       Singleton class: private constructor.
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
        case LENGTH:
        case ROAD_KIND:
        case CARS_PASS_TO_HEAD:
        case CARS_PASS_TO_TAIL:
        case HUMANS_PASS_TO_HEAD:
        case HUMANS_PASS_TO_TAIL:
        case WIDTH:
        case BLOCK:
        case REPAIR_COST:
        case LINES_TO_HEAD:
        case LINES_TO_TAIL:
        case WIDTH_FOR_WALKERS:
        case X:
        case Y:
        case FLOORS:
        case BUILDING_ATTRIBUTES:
        case FIERYNESS:
        case BROKENNESS:
        case BUILDING_CODE:
        case BUILDING_AREA_GROUND:
        case BUILDING_AREA_TOTAL:
        case POSITION_EXTRA:
        case DIRECTION:
        case STAMINA:
        case HP:
        case DAMAGE:
        case BURIEDNESS:
        case WATER_QUANTITY:
        case TEMPERATURE:
        case IMPORTANCE:
            return new IntProperty(urn);
        case SHORTCUT_TO_TURN:
        case POCKET_TO_TURN_ACROSS:
        case SIGNAL_TIMING:
        case BUILDING_APEXES:
            return new IntArrayProperty(urn);
        case MEDIAN_STRIP:
        case IGNITION:
        case SIGNAL:
            return new BooleanProperty(urn);
        case HEAD:
        case TAIL:
        case POSITION:
            return new EntityRefProperty(urn);
        case EDGES:
        case ENTRANCES:
        case POSITION_HISTORY:
            return new EntityRefListProperty(urn);
        default:
            throw new IllegalArgumentException("Unrecognised property urn: " + urn);
        }
    }
}
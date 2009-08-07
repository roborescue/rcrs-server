package kernel.standard;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import kernel.Perception;
import kernel.Agent;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntityType;
import rescuecore2.standard.entities.StandardPropertyType;

/**
   Legacy implementation of perception.
 */
public class StandardPerception implements Perception {
    private static final String VIEW_DISTANCE_KEY = "perception.standard.view-distance";
    private static final String FAR_FIRE_DISTANCE_KEY = "perception.standard.far-fire-distance";
    private static final String USE_FAR_FIRES_KEY = "perception.standard.use-far-fires";
    private static final String HP_PRECISION_KEY = "perception.standard.hp-precision";
    private static final String DAMAGE_PRECISION_KEY = "perception.standard.damage-precision";

    private static final int DEFAULT_HP_PRECISION = 1000;
    private static final int DEFAULT_DAMAGE_PRECISION = 100;

    private int viewDistance;
    private int farFireDistance;
    private boolean useFarFires;
    private int hpPrecision;
    private int damagePrecision;
    private StandardWorldModel world;
    private int time;
    private Set<Building> unburntBuildings;
    private Map<Building, Integer> ignitionTimes;

    /**
       Create a StandardPerception object.
    */
    public StandardPerception() {
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        world = StandardWorldModel.createStandardWorldModel(model);
        viewDistance = config.getIntValue(VIEW_DISTANCE_KEY);
        farFireDistance = config.getIntValue(FAR_FIRE_DISTANCE_KEY, 0);
        useFarFires = config.getBooleanValue(USE_FAR_FIRES_KEY, true);
        hpPrecision = config.getIntValue(HP_PRECISION_KEY, DEFAULT_HP_PRECISION);
        damagePrecision = config.getIntValue(DAMAGE_PRECISION_KEY, DEFAULT_DAMAGE_PRECISION);
        ignitionTimes = new HashMap<Building, Integer>();
        unburntBuildings = new HashSet<Building>();
        time = 0;
        for (StandardEntity next : world) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (b.getFieryness() == 0) {
                    unburntBuildings.add(b);
                }
                else {
                    ignitionTimes.put(b, time);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Standard perception";
    }

    @Override
    public void setTime(int timestep) {
        // Look for buildings that caught fire last timestep
        for (Iterator<Building> it = unburntBuildings.iterator(); it.hasNext();) {
            Building next = it.next();
            int fieryness = next.getFieryness();
            // Fieryness 1, 2 and 3 mean on fire
            // CHECKSTYLE:OFF:MagicNumber
            if (fieryness > 0 && fieryness < 4) {
                // CHECKSTYLE:ON:MagicNumber
                ignitionTimes.put(next, time);
                it.remove();
            }
        }
        time = timestep;
    }

    @Override
    public Collection<Entity> getVisibleEntities(Agent agent) {
        StandardEntity agentEntity = (StandardEntity)agent.getControlledEntity();
        Collection<Entity> result = new HashSet<Entity>();
        // Look for roads/nodes/buildings/humans within range
        Pair<Integer, Integer> location = agentEntity.getLocation(world);
        if (location != null) {
            int x = location.first().intValue();
            int y = location.second().intValue();
            Collection<StandardEntity> nearby = world.getObjectsInRange(x, y, viewDistance);
            // Copy entities and set property values
            for (StandardEntity next : nearby) {
                StandardEntity copy = null;
                switch ((StandardEntityType)next.getType()) {
                case ROAD:
                    copy = (StandardEntity)next.copy();
                    filterRoadProperties((Road)copy);
                    break;
                case BUILDING:
                case REFUGE:
                case FIRE_STATION:
                case AMBULANCE_CENTRE:
                case POLICE_OFFICE:
                    copy = (StandardEntity)next.copy();
                    filterBuildingProperties((Building)copy);
                    break;
                case CIVILIAN:
                case FIRE_BRIGADE:
                case AMBULANCE_TEAM:
                case POLICE_FORCE:
                    copy = (StandardEntity)next.copy();
                    // Always send all properties of the agent-controlled object
                    if (next != agentEntity) {
                        filterHumanProperties((Human)copy);
                    }
                    break;
                default:
                    // Ignore other types
                    break;
                }
                if (copy != null) {
                    result.add(copy);
                }
            }
            // Now look for far fires
            if (useFarFires) {
                for (Map.Entry<Building, Integer> next : ignitionTimes.entrySet()) {
                    Building b = next.getKey();
                    int ignitionTime = next.getValue();
                    int timeDelta = time - ignitionTime;
                    int visibleRange = timeDelta * farFireDistance;
                    int range = world.getDistance(agentEntity, b);
                    if (range <= visibleRange) {
                        Building copy = (Building)b.copy();
                        filterFarBuildingProperties(copy);
                        result.add(copy);
                    }
                }
            }
        }
        return result;
    }

    private void filterRoadProperties(Road road) {
        // Update BLOCK only
        for (Property next : road.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case BLOCK:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterBuildingProperties(Building building) {
        // Update TEMPERATURE, FIERYNESS and BROKENNESS
        for (Property next : building.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case TEMPERATURE:
            case FIERYNESS:
            case BROKENNESS:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterFarBuildingProperties(Building building) {
        // Update FIERYNESS only
        for (Property next : building.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case FIERYNESS:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterHumanProperties(Human human) {
        // Update POSITION, POSITION_EXTRA, DIRECTION, STAMINA, HP, DAMAGE, BURIEDNESS
        for (Property next : human.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case POSITION:
            case POSITION_EXTRA:
            case DIRECTION:
            case STAMINA:
            case BURIEDNESS:
                break;
            case HP:
                roundProperty((IntProperty)next, hpPrecision);
                break;
            case DAMAGE:
                roundProperty((IntProperty)next, damagePrecision);
                break;
            default:
                next.undefine();
            }
        }
    }

    private void roundProperty(IntProperty p, int precision) {
        if (precision != 1) {
            p.setValue(round(p.getValue(), precision));
        }
    }

    private int round(int value, int precision) {
        int remainder = value % precision;
        value -= remainder;
        if (remainder >= precision / 2) {
            value += precision;
        }
        return value;
    }
}
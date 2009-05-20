package kernel.legacy;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import kernel.Perception;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.entities.Road;
import rescuecore2.version0.entities.Building;
import rescuecore2.version0.entities.Human;
import rescuecore2.version0.entities.RescuePropertyType;

/**
   Legacy implementation of perception.
 */
public class LegacyPerception implements Perception<RescueObject, IndexedWorldModel> {
    private static final int HP_PRECISION = 1000;
    private static final int DAMAGE_PRECISION = 100;

    private int viewDistance;
    private int farFireDistance;
    private IndexedWorldModel world;
    private int time;
    private Set<Building> unburntBuildings;
    private Map<Building, Integer> ignitionTimes;

    /**
       Create a LegacyPerception object.
       @param config The configuration of the kernel.
     */
    public LegacyPerception(Config config) {
        this.viewDistance = config.getIntValue("vision");
        this.farFireDistance = config.getIntValue("fire_cognition_spreading_speed");
        ignitionTimes = new HashMap<Building, Integer>();
        unburntBuildings = new HashSet<Building>();
        time = 0;
    }

    @Override
    public void setWorldModel(IndexedWorldModel newWorld) {
        time = 0;
        this.world = newWorld;
        unburntBuildings.clear();
        ignitionTimes.clear();
        for (RescueObject next : world) {
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
    public Collection<RescueObject> getVisibleEntities(RescueObject agent) {
        Collection<RescueObject> result = new HashSet<RescueObject>();
        // Look for roads/nodes/buildings/humans within range
        Pair<Integer, Integer> location = agent.getLocation(world);
        if (location != null) {
            int x = location.first().intValue();
            int y = location.second().intValue();
            Collection<RescueObject> nearby = world.getObjectsInRange(x, y, viewDistance);
            // Copy entities and set property values
            for (RescueObject next : nearby) {
                RescueObject copy = null;
                switch (next.getType()) {
                case ROAD:
                    copy = (RescueObject)next.copy();
                    filterRoadProperties((Road)copy);
                    break;
                case BUILDING:
                case REFUGE:
                case FIRE_STATION:
                case AMBULANCE_CENTRE:
                case POLICE_OFFICE:
                    copy = (RescueObject)next.copy();
                    filterBuildingProperties((Building)copy);
                    break;
                case CIVILIAN:
                case FIRE_BRIGADE:
                case AMBULANCE_TEAM:
                case POLICE_FORCE:
                    copy = (RescueObject)next.copy();
                    // Always send all properties of the agent-controlled object
                    if (next != agent) {
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
            for (Map.Entry<Building, Integer> next : ignitionTimes.entrySet()) {
                Building b = next.getKey();
                int ignitionTime = next.getValue();
                int timeDelta = time - ignitionTime;
                int visibleRange = timeDelta * farFireDistance;
                int range = world.getDistance(agent, b);
                if (range <= visibleRange) {
                    Building copy = (Building)b.copy();
                    filterFarBuildingProperties(copy);
                    result.add(copy);
                }
            }
        }
        return result;
    }

    private void filterRoadProperties(Road road) {
        // Update BLOCK only
        for (Property next : road.getProperties()) {
            switch ((RescuePropertyType)next.getType()) {
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
            switch ((RescuePropertyType)next.getType()) {
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
            switch ((RescuePropertyType)next.getType()) {
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
            switch ((RescuePropertyType)next.getType()) {
            case POSITION:
            case POSITION_EXTRA:
            case DIRECTION:
            case STAMINA:
            case BURIEDNESS:
                break;
            case HP:
                roundProperty((IntProperty)next, HP_PRECISION);
                break;
            case DAMAGE:
                roundProperty((IntProperty)next, DAMAGE_PRECISION);
                break;
            default:
                next.undefine();
            }
        }
    }

    private void roundProperty(IntProperty p, int precision) {
        p.setValue(round(p.getValue(), precision));
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
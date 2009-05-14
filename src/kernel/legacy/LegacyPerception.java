package kernel.legacy;

import java.util.Collection;
import java.util.HashSet;

import kernel.Perception;

import rescuecore2.worldmodel.Property;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.entities.Road;
import rescuecore2.version0.entities.Building;
import rescuecore2.version0.entities.Human;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   Legacy implementation of perception.
 */
public class LegacyPerception implements Perception<RescueObject, IndexedWorldModel> {
    private int viewDistance;
    private int farFireDistance;
    private IndexedWorldModel world;

    /**
       Create a LegacyPerception object.
       @param config The configuration of the kernel.
     */
    public LegacyPerception(Config config) {
        this.viewDistance = config.getIntValue("vision");
        this.farFireDistance = config.getIntValue("fire_cognition_spreading_speed");
    }

    @Override
    public void setWorldModel(IndexedWorldModel newWorld) {
        this.world = newWorld;
        //        world.indexClass(EntityConstants.ROAD, EntityConstants.NODE, EntityConstants.BUILDING, EntityConstants.REFUGE, EntityConstants.FIRE_STATION, EntityConstants.AMBULANCE_CENTRE, EntityConstants.POLICE_OFFICE, EntityConstants.CIVILIAN, EntityConstants.FIRE_BRIGADE, EntityConstants.AMBULANCE_TEAM, EntityConstants.POLICE_FORCE);
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
                // Update roads, buildings and humans
                // Nodes have only static data
                if (next instanceof Road) {
                    copy = (RescueObject)next.copy();
                    filterRoadProperties((Road)copy);
                }
                if (next instanceof Building) {
                    copy = (RescueObject)next.copy();
                    filterBuildingProperties((Building)copy);
                }
                if (next instanceof Human) {
                    copy = (RescueObject)next.copy();
                    // Always send all properties of the agent-controlled object
                    if (next != agent) {
                        filterHumanProperties((Human)copy);
                    }
                }
                if (copy != null) {
                    result.add(copy);
                }
            }
        }
        // Now look for far fires
        return result;
    }

    private void filterRoadProperties(Road road) {
        // Update BLOCK only
        for (Property next : road.getProperties()) {
            switch (PropertyType.fromID(next.getID())) {
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
            switch (PropertyType.fromID(next.getID())) {
            case TEMPERATURE:
            case FIERYNESS:
            case BROKENNESS:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterHumanProperties(Human human) {
        // Update POSITION, POSITION_EXTRA, DIRECTION, STAMINA, HP, DAMAGE, BURIEDNESS
        // TODO: Round hp/damage
        for (Property next : human.getProperties()) {
            switch (PropertyType.fromID(next.getID())) {
            case POSITION:
            case POSITION_EXTRA:
            case DIRECTION:
            case STAMINA:
            case HP:
            case DAMAGE:
            case BURIEDNESS:
                break;
            default:
                next.undefine();
            }
        }
    }
}
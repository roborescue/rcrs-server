package kernel.legacy;

import java.util.Collection;
import java.util.HashSet;

import kernel.Perception;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.config.Config;
import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.entities.EntityConstants;
import rescuecore2.version0.entities.Road;
import rescuecore2.version0.entities.Node;
import rescuecore2.version0.entities.Building;
import rescuecore2.version0.entities.Human;

/**
   Legacy implementation of perception.
 */
public class LegacyPerception implements Perception<RescueObject> {
    private int viewDistance;
    private int farFireDistance;
    private WorldModel<RescueObject> world;
    private IndexedWorldModel index;

    /**
       Create a LegacyPerception object.
       @param config The configuration of the kernel.
     */
    public LegacyPerception(Config config) {
        this.viewDistance = config.getIntValue("vision");
        this.farFireDistance = config.getIntValue("fire_cognition_spreading_speed");
    }

    @Override
    public void setWorldModel(WorldModel<RescueObject> world) {
        this.world = world;
        index = new IndexedWorldModel(world, viewDistance);
        index.index();
        index.indexClass(EntityConstants.ROAD, EntityConstants.NODE, EntityConstants.BUILDING, EntityConstants.REFUGE, EntityConstants.FIRE_STATION, EntityConstants.AMBULANCE_CENTRE, EntityConstants.POLICE_OFFICE, EntityConstants.CIVILIAN, EntityConstants.FIRE_BRIGADE, EntityConstants.AMBULANCE_TEAM, EntityConstants.POLICE_FORCE);
    }


    @Override
    public Collection<RescueObject> getVisibleEntities(RescueObject agent) {
        Collection<RescueObject> result = new HashSet<RescueObject>();
        // Look for roads/nodes/buildings/humans within range
        int[] location = new int[2];
        if (index.locate(agent, location)) {
            int x = location[0];
            int y = location[1];
            Collection<RescueObject> nearby = index.getObjectsInRange(x, y, viewDistance);
            // Copy entities and set property values
            for (RescueObject next : nearby) {
                RescueObject copy = (RescueObject)next.copy();
                if (copy instanceof Road) {
                    processRoadProperties((Road)copy);
                }
                if (copy instanceof Node) {
                    processNodeProperties((Node)copy);
                }
                if (copy instanceof Building) {
                    processBuildingProperties((Building)copy);
                }
                if (copy instanceof Human) {
                    processHumanProperties((Human)copy);
                }
                result.add(copy);
            }
        }
        // Now look for far fires
        return result;
    }

    private void processRoadProperties(Road road) {
    }

    private void processNodeProperties(Node node) {
    }

    private void processBuildingProperties(Building building) {
    }

    private void processHumanProperties(Human human) {
    }
}
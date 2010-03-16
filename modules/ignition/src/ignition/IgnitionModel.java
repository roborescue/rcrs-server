package ignition;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Building;

import java.util.Set;

/**
   A model for determining which buildings ignite at any timestep.
 */
public interface IgnitionModel {
    /**
       Find out which buildings have ignited.
       @param world The world model.
       @param time The current time.
       @return A list of newly ignited buildings.
    */
    Set<Building> findIgnitionPoints(StandardWorldModel world, int time);
}

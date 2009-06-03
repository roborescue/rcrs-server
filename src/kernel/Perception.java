package kernel;

import java.util.Collection;

import rescuecore2.worldmodel.Entity;

/**
   Implementations of this interface are responsible for determining what entities/properties each agent can see.
 */
public interface Perception {
    /**
       Determine what Entities are visible to a particular agent. The returned Entities should be copies of Entities in the ground-truth WorldModel. Only visible properties should have defined values.
       @param agent The agent that is perceiving the world.
       @return A collection of entities that the agent can perceive.
     */
    Collection<Entity> getVisibleEntities(Agent agent);

    /**
       Notify this perception object of the current time.
       @param timestep The current timestep.
    */
    void setTime(int timestep);
}
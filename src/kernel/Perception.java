package kernel;

import java.util.Collection;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Implementations of this interface are responsible for determining what entities/properties each agent can see.
   @param <S> The subclass of WorldModel that this class understands.
   @param <T> The subclass of Entity that this class understands.
 */
public interface Perception<T extends Entity, S extends WorldModel<? super T>> extends WorldModelAware<S> {
    /**
       Determine what Entities are visible to a particular agent. The returned Entities should be copies of Entities in the ground-truth WorldModel. Only visible properties should have defined values.
       @param agent The agent that is perceiving the world.
       @return A collection of entities that the agent can perceive.
     */
    Collection<T> getVisibleEntities(Agent<? extends T> agent);

    /**
       Notify this perception object of the current time.
       @param timestep The current timestep.
    */
    void setTime(int timestep);
}
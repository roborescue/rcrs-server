package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Interface for objects that are aware of the world model.
   @param <S> The subclass of WorldModel that this object understands.
   @param <T> The subclass of Entity that this object understands.
 */
public interface WorldModelAware<T extends Entity, S extends WorldModel<T>> {
    /**
       Set the world model.
       @param model The new world model.
    */
    void setWorldModel(S model);
}
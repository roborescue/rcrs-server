package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Interface for objects that are aware of the world model.
   @param <S> The subclass of WorldModel that this object understands.
 */
public interface WorldModelAware<S extends WorldModel> {
    /**
       Set the world model.
       @param model The new world model.
    */
    void setWorldModel(S model);
}
package rescuecore2.view;

import rescuecore2.worldmodel.WorldModel;

/**
   An abstract base class for ViewLayer implementations.
   @param <T> The subclass of WorldModel that this layer understands.
 */
public abstract class AbstractViewLayer<T extends WorldModel> implements ViewLayer<T> {
    private T world;

    @Override
    public void setWorldModel(T newWorld) {
        this.world = newWorld;
    }

    /**
       Get the world model.
       @return The world model.
     */
    protected T getWorld() {
        return world;
    }
}
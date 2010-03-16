package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   A class for obtaining information about the state of the kernel.
*/
public class KernelState {
    private int time;
    private WorldModel<? extends Entity> model;

    /**
       Construct a snapshot of the kernel state.
       @param time The current time.
       @param model The world model snapshot.
    */
    public KernelState(int time, WorldModel<? extends Entity> model) {
        this.time = time;
        this.model = model;
    }

    /**
       Get the current time.
       @return The current time.
    */
    public int getTime() {
        return time;
    }

    /**
       Get the world model.
       @return The world model.
    */
    public WorldModel<? extends Entity> getWorldModel() {
        return model;
    }
}

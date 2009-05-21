package kernel;

/**
   Interface for objects that are interested in kernel events.
 */
public interface KernelListener {
    /**
       Notification that the kernel has built the world model.
    */
    void worldModelComplete();

    /**
       Notification that a timestep has started.
       @param time The current time.
    */
    void timestep(int time);
}
package kernel;

import rescuecore2.config.Config;

/**
   Termination conditions tell the kernel when to stop running a simulation.
 */
public interface TerminationCondition {
    /**
       Initialise this termination condition.
       @param config The configuration.
     */
    void initialise(Config config);

    /**
       Return whether this termination condition has been met.
       @param state The state of the kernel.
       @return True if this termination condition has been met and the simulation should stop, false otherwise.
     */
    boolean shouldStop(KernelState state);
}

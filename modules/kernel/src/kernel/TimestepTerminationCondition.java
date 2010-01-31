package kernel;

import rescuecore2.config.Config;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A TerminationCondition that terminates the simulation after a specified timestep.
*/
public class TimestepTerminationCondition implements TerminationCondition {
    private static final Log LOG = LogFactory.getLog(TimestepTerminationCondition.class);

    /**
       The config key describing the number of timesteps to run.
     */
    private static final String TIMESTEPS_KEY = "kernel.timesteps";

    private int time;

    @Override
    public void initialise(Config config) {
        time = config.getIntValue(TIMESTEPS_KEY);
    }

    @Override
    public boolean shouldStop(KernelState state) {
        if (state.getTime() >= time) {
            LOG.info("TimestepTerminationCondition fired: " + state.getTime() + " >= " + time);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Timestep >= " + time + "";
    }
}
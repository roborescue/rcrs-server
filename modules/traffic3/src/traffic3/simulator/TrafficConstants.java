package traffic3.simulator;

import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.number.NumberGenerator;

import rescuecore2.config.Config;

/**
   A bunch of useful constants for the traffic simulator.
*/
public final class TrafficConstants {
    // Agent force constants
    private static double NUDGE_MAGNITUDE = 0.001;
    private static double AGENT_FORCE_COEFFICIENT_A = 0.0001;
    private static double AGENT_FORCE_COEFFICIENT_B = 0.001;
    private static double AGENT_FORCE_COEFFICIENT_K = 0.00001;
    private static double AGENT_DISTANCE_CUTOFF = 10000;
    private static double AGENT_FORCE_LIMIT = 0.0001;
    private static NumberGenerator<Double> nudge;

    // Wall force constants
    private static double WALL_DISTANCE_CUTOFF = 2000;
    private static double WALL_FORCE_COEFFICIENT_A = 0.01;
    private static double WALL_FORCE_COEFFICIENT_B = 0.7;
    //    private static final double WALL_FORCE_COEFFICIENT_K = 0.00001;

    private TrafficConstants() {}

    /**
       Initialise constants from a configuration file.
       @param config The Config to read.
    */
    static void init(Config config) {
    	AGENT_FORCE_COEFFICIENT_A=config.getFloatValue("traffic3.agent.force.coefficient.A",AGENT_FORCE_COEFFICIENT_A);
    	AGENT_FORCE_COEFFICIENT_B=config.getFloatValue("traffic3.agent.force.coefficient.B",AGENT_FORCE_COEFFICIENT_B);
    	AGENT_FORCE_COEFFICIENT_K=config.getFloatValue("traffic3.agent.force.coefficient.K",AGENT_FORCE_COEFFICIENT_K);
    	AGENT_DISTANCE_CUTOFF=config.getFloatValue("traffic3.agent.distance.cutoff",AGENT_DISTANCE_CUTOFF);
    	AGENT_FORCE_LIMIT=config.getFloatValue("traffic3.agent.force.limit",AGENT_FORCE_LIMIT);
    	WALL_DISTANCE_CUTOFF=config.getFloatValue("traffic3.wall.distance.cutoff",WALL_DISTANCE_CUTOFF);
    	WALL_FORCE_COEFFICIENT_A=config.getFloatValue("traffic3.wall.force.coefficient.A",WALL_FORCE_COEFFICIENT_A);
    	WALL_FORCE_COEFFICIENT_B=config.getFloatValue("traffic3.wall.force.coefficient.B",WALL_FORCE_COEFFICIENT_B);
    	NUDGE_MAGNITUDE=config.getFloatValue("traffic3.nudge-magnitude",NUDGE_MAGNITUDE);
        nudge = new ContinuousUniformGenerator(-NUDGE_MAGNITUDE, NUDGE_MAGNITUDE, config.getRandom());
    }

    /**
       Get a (randomised) nudge for agents that are co-located.
       @return A random nudge force.
    */
    public static double getColocatedAgentNudge() {
        return nudge.nextValue();
    }

    /**
       Get the maximum distance at which agents affect each other.
       @return The agent force distance cutoff.
    */
    public static double getAgentDistanceCutoff() {
        return AGENT_DISTANCE_CUTOFF;
    }

    /**
       Get the agent force function coefficient "A".
       @return The agent force function coefficient "A".
    */
    public static double getAgentForceCoefficientA() {
        return AGENT_FORCE_COEFFICIENT_A;
    }

    /**
       Get the agent force function coefficient "B".
       @return The agent force function coefficient "B".
    */
    public static double getAgentForceCoefficientB() {
        return AGENT_FORCE_COEFFICIENT_B;
    }

    /**
       Get the agent force function coefficient "K".
       @return The agent force function coefficient "K".
    */
    public static double getAgentForceCoefficientK() {
        return AGENT_FORCE_COEFFICIENT_K;
    }

    /**
       Get the maximum total agent force.
       @return The maximum total agent force.
    */
    public static double getAgentForceLimit() {
        return AGENT_FORCE_LIMIT;
    }

    /**
       Get the maximum distance at which walls affect agents.
       @return The wall force distance cutoff.
    */
    public static double getWallDistanceCutoff() {
        return WALL_DISTANCE_CUTOFF;
    }

    /**
       Get the wall force function coefficient "A".
       @return The wall force function coefficient "A".
    */
    public static double getWallForceCoefficientA() {
        return WALL_FORCE_COEFFICIENT_A;
    }

    /**
       Get the wall force function coefficient "B".
       @return The wall force function coefficient "B".
    */
    public static double getWallForceCoefficientB() {
        return WALL_FORCE_COEFFICIENT_B;
    }

    /**
       Get the wall force function coefficient "K".
       @return The wall force function coefficient "K".
    */
    /*
    public static double getWallForceCoefficientK() {
        return WALL_FORCE_COEFFICIENT_K;
    }
    */
}
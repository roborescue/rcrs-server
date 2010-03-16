package kernel;

/**
   Some useful constants for the kernel.
*/
public final class KernelConstants {
    /** The config key for gis implementations. */
    public static final String GIS_KEY = "kernel.gis";

    /** The config key for perception implementations. */
    public static final String PERCEPTION_KEY = "kernel.perception";

    /** The config key for communication model implementations. */
    public static final String COMMUNICATION_MODEL_KEY = "kernel.communication";

    /** The config key for agent implementations. */
    public static final String AGENTS_KEY = "kernel.agents";

    /** The config key for simulator implementations. */
    public static final String SIMULATORS_KEY = "kernel.simulators";

    /** The config key for viewer implementations. */
    public static final String VIEWERS_KEY = "kernel.viewers";

    /** The config key for component implementations. */
    public static final String COMPONENTS_KEY = "kernel.components";

    /** Whether to run the kernel in inline-only mode. */
    public static final String INLINE_ONLY_KEY = "kernel.inline-only";

    /** The config key for ignoring agent commands at the start of the simulation. */
    public static final String IGNORE_AGENT_COMMANDS_KEY = "kernel.agents.ignoreuntil";

    private KernelConstants() {}
}

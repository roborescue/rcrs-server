package rescuecore2;

/**
 * Some useful constants that are shared across all parts of the Robocup Rescue
 * software. Note that this does NOT include constants for entity/message types.
 */
public final class Constants {
  /** prefix for control message URN */
  public static final int CONTROL_MSG_URN_PREFIX = 0x0100;
  public static final int CONTROL_MSG_COMPONENT_URN_PREFIX = 0x0200;

  /** Config key for message factories. */
  public static final String FACTORY_KEY = "factory";
  /** Config key for looking up jars for inspection by a LoadableTypeProcessor. */
  public static final String JAR_DIR_KEY = "loadabletypes.inspect.dir";
  /**
   * Config key for specifying whether to do a deep inspection of jars for
   * loadable types.
   */
  public static final String DEEP_JAR_INSPECTION_KEY = "loadabletypes.inspect.deep";
  /** Default location for looking up jar files. */
  public static final String DEFAULT_JAR_DIR = "../jars";
  /** Default deep inspection. */
  public static final boolean DEFAULT_DEEP_JAR_INSPECTION = true;
  /**
   * Config key for specifying jar names to ignore when finding loadable types.
   */
  public static final String IGNORE_JARS_KEY = "loadabletypes.ignore";
  /** Default list of jar names to ignore when finding loadable types. */
  public static final String DEFAULT_IGNORE_JARS = "rescuecore2.jar";

  /** Config key for the kernel host name. */
  public static final String KERNEL_HOST_NAME_KEY = "kernel.host";
  /** Default kernel host name. */
  public static final String DEFAULT_KERNEL_HOST_NAME = "localhost";
  /** Config key for the kernel port number. */
  public static final String KERNEL_PORT_NUMBER_KEY = "kernel.port";
  /** Default kernel port number. */
  public static final int DEFAULT_KERNEL_PORT_NUMBER = 27931;
  /** Config key for the gis port number. */
  public static final String GIS_PORT_NUMBER_KEY = "gis.port";
  /** Default gis port number. */
  public static final int DEFAULT_GIS_PORT_NUMBER = 27932;

  /** The random seed key. */
  public static final String RANDOM_SEED_KEY = "random.seed";
  /** The random implementation class key. */
  public static final String RANDOM_CLASS_KEY = "random.class";
  /** The default random implementation class. */
  public static final String RANDOM_CLASS_DEFAULT = "org.uncommons.maths.random.MersenneTwisterRNG";

  /** The name of the communication model class. */
  public static final String COMMUNICATION_MODEL_KEY = "kernel.communication-model";
  /** The name of the perception class. */
  public static final String PERCEPTION_KEY = "kernel.perception";

  /** Config key for the top-level score function. */
  public static final String SCORE_FUNCTION_KEY = "score.function";

  private Constants() {
  }
}
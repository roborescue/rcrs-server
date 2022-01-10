package rescuecore2.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedGenerator;

import rescuecore2.Constants;
import rescuecore2.log.Logger;

/**
 * This class represents a config file and any other config files that might
 * have been included with a !include directive. Config files must be defined
 * relative to a base directory so that includes can be resolved.
 */
public class Config {
  private static final String ARRAY_REGEX = " |,";

  /**
   * The raw data and caches of int/float/boolean/array interpretations.
   */
  private Map<String, String> data;
  // Entries that should not be cached
  private Set<String> noCache;
  private Map<String, Integer> intData;
  private Map<String, Double> floatData;
  private Map<String, Boolean> booleanData;
  private Map<String, List<String>> arrayData;

  private Set<ConfigConstraint> constraints;
  private Set<ConfigConstraint> violatedConstraints;

  private static Random seedGenerator;
  private Random random;

  /**
   * Create an empty config.
   */
  public Config() {
    data = new HashMap<String, String>();
    noCache = new HashSet<String>();
    intData = new HashMap<String, Integer>();
    floatData = new HashMap<String, Double>();
    booleanData = new HashMap<String, Boolean>();
    arrayData = new HashMap<String, List<String>>();
    constraints = new HashSet<ConfigConstraint>();
    violatedConstraints = new HashSet<ConfigConstraint>();
  }

  /**
   * Create a config that reads from a given file. Additional config files can be
   * read later with the {@link #read(String)} method.
   *
   * @param file The config file to read. Must not be null.
   * @throws ConfigException If there is an error parsing the config file or one
   *                         of its descendants.
   */
  public Config(File file) throws ConfigException {
    this();
    read(file);
  }

  /**
   * Copy constructor. The new Config will contain all keys and values that are
   * currently in this config.
   *
   * @param other The Config to copy.
   */
  public Config(Config other) {
    this();
    this.data.putAll(other.data);
  }

  /**
   * Read a config file from a resource that this class' classloader can find.
   *
   * @param resource The name of the resource.
   * @throws ConfigException If there is an error parsing the config file or one
   *                         of its descendants.
   */
  public void read(String resource) throws ConfigException {
    if (resource == null) {
      throw new IllegalArgumentException("Resource cannot be null");
    }
    new ResourceContext(resource).process(this);
    checkAllConstraints();
  }

  /**
   * Read a config file and add its contents. Existing entries with the same name
   * will be overwritten.
   *
   * @param file The config file to read. Must not be null. If this is a directory
   *             then all files in the directory tree will be read.
   * @throws ConfigException If there is an error parsing the config file or one
   *                         of its descendants.
   */
  public void read(File file) throws ConfigException {
    if (file == null) {
      throw new IllegalArgumentException("File cannot be null");
    }
    new FileContext(file).process(this);
    checkAllConstraints();
  }

  /**
   * Read config information from a Reader and add its contents. Existing entries
   * with the same name will be overwritten.
   *
   * @param reader The Reader to read from. Must not be null.
   * @param name   The name of the reader being read. This is used when reporting
   *               errors in the file.
   * @throws ConfigException If there is an error parsing the config file or one
   *                         of its descendants.
   */
  public void read(Reader reader, String name) throws ConfigException {
    if (reader == null) {
      throw new IllegalArgumentException("Reader cannot be null");
    }
    new ReaderContext(reader, name).process(this);
    checkAllConstraints();
  }

  private void readWithContext(BufferedReader reader, Context context) throws ConfigException {
    String line = "";
    int lineNumber = 0;
    String name = context.getName();
    try {
      while (line != null) {
        try {
          line = reader.readLine();
        } catch (IOException e) {
          throw new ConfigException(name, e);
        }
        ++lineNumber;
        if (line != null) {
          // Strip off everything after a #
          int hashIndex = line.indexOf("#");
          if (hashIndex != -1) {
            line = line.substring(0, hashIndex).trim();
          }
          line = line.trim();
          // Ignore empty lines
          if ("".equals(line)) {
            continue;
          }
          LineType.process(line, context, this, lineNumber);
        }
      }
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        Logger.error("Error reading config", e);
      }
    }
  }

  /**
   * Write this config to a PrintWriter.
   *
   * @param out The PrintWriter to write to. Must not be null.
   * @throws IOException If there is an error writing to the stream.
   */
  public void write(PrintWriter out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("Output cannot be null");
    }
    for (Map.Entry<String, String> next : data.entrySet()) {
      out.print(next.getKey());
      out.print(" : ");
      out.println(next.getValue());
    }
  }

  /**
   * Merge all keys and values from another Config into this one.
   *
   * @param other The Config to merge from.
   */
  public void merge(Config other) {
    clearCache();
    this.data.putAll(other.data);
    checkAllConstraints();
  }

  /**
   * Add a constraint.
   *
   * @param c The constraint to add.
   */
  public void addConstraint(ConfigConstraint c) {
    constraints.add(c);
    checkConstraint(c);
  }

  /**
   * Remove a constraint.
   *
   * @param c The constraint to remove.
   */
  public void removeConstraint(ConfigConstraint c) {
    constraints.remove(c);
    violatedConstraints.remove(c);
  }

  /**
   * Remove constraints on a key.
   *
   * @param key The key to remove constraints from.
   */
  /*
   * public void removeConstraint(String key) { ConstrainedConfigValue c =
   * constraints.get(key); if (c != null) { removeConstraint(c); } }
   */

  /**
   * Get all violated constraints.
   *
   * @return All violated constraints.
   */
  public Set<ConfigConstraint> getViolatedConstraints() {
    return Collections.unmodifiableSet(violatedConstraints);
  }

  /**
   * Get the constraint for a particular key.
   *
   * @param key The key to look up.
   * @return The constraint for that key, or null if there is no constraint.
   */
  /*
   * public ConstrainedConfigValue getConstraint(String key) { return
   * constraints.get(key); }
   */

  /**
   * Find out if a value violates its key's constraints.
   *
   * @param key The key to look up.
   * @return True if the key's value violates the constraints.
   */
  /*
   * public boolean isConstraintViolated(String key) { ConstrainedConfigValue c =
   * getConstraint(key); if (c != null) { return violatedConstraints.contains(c);
   * } return false; }
   */

  /**
   * Get all keys in this config.
   *
   * @return An immutable view of all keys.
   */
  public Set<String> getAllKeys() {
    return Collections.unmodifiableSet(data.keySet());
  }

  /**
   * Find out if a key is defined.
   *
   * @param key The key to test.
   * @return True if the key has a non-null value.
   */
  public boolean isDefined(String key) {
    return data.containsKey(key);
  }

  /**
   * Get the value of a key as a String.
   *
   * @param key The key to look up. Must not be null.
   * @return The value associated with that key.
   * @throws NoSuchConfigOptionException If the key is not defined.
   */
  public String getValue(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (!data.containsKey(key)) {
      throw new NoSuchConfigOptionException(key);
    }
    return processDollarNotation(key, data.get(key));
  }

  /**
   * Get the value of a key as a String or use a default value.
   *
   * @param key          The key to look up. Must not be null.
   * @param defaultValue The default value to return if the key has no defined
   *                     value.
   * @return The value associated with that key, or the default value of the key
   *         has no value.
   */
  public String getValue(String key, String defaultValue) {
    try {
      return getValue(key);
    } catch (NoSuchConfigOptionException e) {
      return defaultValue;
    }
  }

  /**
   * Get the value of a key as an integer.
   *
   * @param key The key to look up. Must not be null.
   * @return The value associated with that key interpreted as an integer.
   * @throws NoSuchConfigOptionException If the key is not defined.
   * @throws NumberFormatException       If the value of the key cannot be
   *                                     interpreted as an integer.
   */
  public int getIntValue(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (!noCache.contains(key) && intData.containsKey(key)) {
      return intData.get(key);
    }
    int result = Integer.parseInt(getValue(key));
    intData.put(key, result);
    return result;
  }

  /**
   * Get the value of a key as an integer or use a default value.
   *
   * @param key          The key to look up. Must not be null.
   * @param defaultValue The default value to return if the key has no defined
   *                     value.
   * @return The value associated with that key interpreted as an integer, or the
   *         default value of the key has no value.
   * @throws NumberFormatException If the value of the key cannot be interpreted
   *                               as an integer.
   */
  public int getIntValue(String key, int defaultValue) {
    try {
      return getIntValue(key);
    } catch (NoSuchConfigOptionException e) {
      return defaultValue;
    }
  }

  /**
   * Get the value of a key as a floating point number.
   *
   * @param key The key to look up. Must not be null.
   * @return The value associated with that key interpreted as a floating point
   *         number.
   * @throws NoSuchConfigOptionException If the key is not defined.
   * @throws NumberFormatException       If the value of the key cannot be
   *                                     interpreted as a floating point number.
   */
  public double getFloatValue(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (!noCache.contains(key) && floatData.containsKey(key)) {
      return floatData.get(key);
    }
    double result = Double.parseDouble(getValue(key));
    floatData.put(key, result);
    return result;
  }

  /**
   * Get the value of a key as a floating point number or use a default value.
   *
   * @param key          The key to look up. Must not be null.
   * @param defaultValue The default value to return if the key has no defined
   *                     value.
   * @return The value associated with that key interpreted as a floating point
   *         number, or the default value of the key has no value.
   * @throws NumberFormatException If the value of the key cannot be interpreted
   *                               as a floating point number.
   */
  public double getFloatValue(String key, double defaultValue) {
    try {
      return getFloatValue(key);
    } catch (NoSuchConfigOptionException e) {
      return defaultValue;
    }
  }

  /**
   * Get the value of a key as a boolean. "true", "t", "yes", "y" and "1" (case
   * insensitive) are all interpreted as true, all other values are false.
   *
   * @param key The key to look up. Must not be null.
   * @return The value associated with that key interpreted as a boolean.
   * @throws NoSuchConfigOptionException If the key is not defined.
   */
  public boolean getBooleanValue(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (!noCache.contains(key) && booleanData.containsKey(key)) {
      return booleanData.get(key);
    }
    boolean result = false;
    String value = getValue(key);
    if ("true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)
        || "y".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
      result = true;
    }
    booleanData.put(key, result);
    return result;
  }

  /**
   * Get the value of a key as a boolean or use a default value. "true", "t",
   * "yes", "y" and "1" (case insensitive) are all interpreted as true, all other
   * values are false.
   *
   * @param key          The key to look up. Must not be null.
   * @param defaultValue The default value to return if the key has no defined
   *                     value.
   * @return The value associated with that key interpreted as a boolean, or the
   *         default value of the key has no value.
   */
  public boolean getBooleanValue(String key, boolean defaultValue) {
    try {
      return getBooleanValue(key);
    } catch (NoSuchConfigOptionException e) {
      return defaultValue;
    }
  }

  /**
   * Get the value of a key as an array of strings. The value will be split on
   * space and comma characters and the resulting list of tokens is returned.
   *
   * @param key The key to look up. Must not be null.
   * @return The value associated with that key interpreted as an array of
   *         space-and-comma-separated tokens.
   * @throws NoSuchConfigOptionException If the key is not defined.
   */
  public List<String> getArrayValue(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (!noCache.contains(key) && arrayData.containsKey(key)) {
      List<String> entry = arrayData.get(key);
      if (entry != null) {
        return entry;
      }
    }
    List<String> result = splitArrayValue(getValue(key));
    arrayData.put(key, result);
    return result;
  }

  /**
   * Get the value of a key (or a default value) as an array of strings. The value
   * will be split on space and comma characters and the resulting list of tokens
   * is returned.
   *
   * @param key          The key to look up. Must not be null.
   * @param defaultValue The default value to use if the key is not defined. If
   *                     this is null then null will be returned, otherwise it
   *                     will be split according to the usual rules.
   * @return The value associated with the key (or the default value) interpreted
   *         as an array of space or comma-separated tokens.
   */
  public List<String> getArrayValue(String key, String defaultValue) {
    try {
      return getArrayValue(key);
    } catch (NoSuchConfigOptionException e) {
      if (defaultValue == null) {
        return null;
      }
      return splitArrayValue(defaultValue);
    }
  }

  private List<String> splitArrayValue(String value) {
    List<String> result = new ArrayList<String>();
    String[] s = value.split(ARRAY_REGEX);
    for (String next : s) {
      if (!"".equals(next)) {
        result.add(next);
      }
    }
    return result;
  }

  /**
   * Set the value of a key.
   *
   * @param key   The key to set. Must not be null.
   * @param value The new value. If this is null then {@link #removeKey(String)}
   *              is called with the given key.
   */
  public void setValue(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (value == null) {
      removeKey(key);
      return;
    }
    clearCache(key);
    noCache.remove(key);
    data.put(key, value);
    checkAllConstraints();
  }

  /**
   * Append a value to a key. If there is no value for the key then this is
   * equivalent to {@link #setValue(String, String)}. This method calls
   * {@link #appendValue(String, String, String)} with a space character as the
   * separator.
   *
   * @param key   The key to append.
   * @param value The value to append.
   */
  public void appendValue(String key, String value) {
    appendValue(key, value, " ");
  }

  /**
   * Append a value to a key. If there is no value for the key then this is
   * equivalent to {@link #setValue(String, String)}.
   *
   * @param key       The key to append.
   * @param value     The value to append.
   * @param separator A string to add before the new value to separate it from the
   *                  previous value.
   */
  public void appendValue(String key, String value, String separator) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
    clearCache(key);
    if (data.containsKey(key)) {
      String old = data.get(key);
      data.put(key, old + separator + value);
    } else {
      data.put(key, value);
    }
    checkAllConstraints();
  }

  /**
   * Set the value of a key as an integer.
   *
   * @param key   The key to set. Must not be null.
   * @param value The new value.
   */
  public void setIntValue(String key, int value) {
    setValue(key, Integer.valueOf(value).toString());
    intData.put(key, value);
  }

  /**
   * Set the value of a key as a floating point number.
   *
   * @param key   The key to set. Must not be null.
   * @param value The new value.
   */
  public void setFloatValue(String key, double value) {
    setValue(key, Double.valueOf(value).toString());
    floatData.put(key, value);
  }

  /**
   * Set the value of a key as a boolean.
   *
   * @param key   The key to set. Must not be null.
   * @param value The new value.
   */
  public void setBooleanValue(String key, boolean value) {
    setValue(key, value ? "true" : "false");
    booleanData.put(key, value);
  }

  /**
   * Remove a key from the config.
   *
   * @param key The key to remove. Must not be null.
   */
  public void removeKey(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    clearCache(key);
    noCache.remove(key);
    data.remove(key);
  }

  /**
   * Remove all keys.
   */
  public void removeAllKeys() {
    data.clear();
    noCache.clear();
    intData.clear();
    floatData.clear();
    booleanData.clear();
    arrayData.clear();
  }

  /**
   * Remove all except a set of keys.
   *
   * @param exceptions The keys to keep.
   */
  public void removeExcept(String... exceptions) {
    removeExcept(Arrays.asList(exceptions));
  }

  /**
   * Remove all except a set of keys.
   *
   * @param exceptions The keys to keep.
   */
  public void removeExcept(Collection<String> exceptions) {
    data.keySet().retainAll(exceptions);
    noCache.retainAll(exceptions);
  }

  /**
   * Remove all keys that do not match any of the given regular expressions.
   *
   * @param exceptions The regular expressions describing keys to keep.
   */
  public void removeExceptRegex(Collection<String> exceptions) {
    Set<String> toRemove = new HashSet<String>(data.keySet());
    Logger.debug("Removing all except " + exceptions);
    for (String exception : exceptions) {
      Pattern p = Pattern.compile(exception);
      for (String key : data.keySet()) {
        if (p.matcher(key).matches()) {
          Logger.debug(key + " matches " + exception);
          toRemove.remove(key);
        }
      }
    }
    Logger.debug("Removing " + toRemove);
    for (String next : toRemove) {
      data.remove(next);
      noCache.remove(next);
    }
  }

  /**
   * Get the random number generator defined by this config.
   *
   * @return The random number generator.
   */
  public Random getRandom() {
    synchronized (this) {
      if (random == null) {
        String className = getValue(Constants.RANDOM_CLASS_KEY, Constants.RANDOM_CLASS_DEFAULT);
        String seed = getValue(Constants.RANDOM_SEED_KEY, "");
        try {
          Class<? extends Random> clazz = Class.forName(className).asSubclass(Random.class);
          Logger.debug("Instantiating random number generator: " + className);
          if ("".equals(seed)) {
            // Create a default seed from the current time. We don't need cryptographic
            // strength here, and using the default constructor will exhaust /dev/random
            // quickly, causing long delays on startup.
            long mtime = new Date().getTime();
            seed = String.valueOf(mtime);
          }

          Logger.debug("Using seed " + seed);
          // CHECKSTYLE:OFF:MagicNumber
          BigInteger bi = new BigInteger(seed, 16);
          // CHECKSTYLE:ON:MagicNumber
          // Look for a constructor that takes a byte array
          try {
            Logger.trace("Trying to find a SeedGenerator constructor");
            Constructor<? extends Random> constructor = clazz.getConstructor(SeedGenerator.class);
            random = constructor.newInstance(new StaticSeedGenerator(bi.toByteArray()));
            Logger.trace("Success");
          } catch (IllegalAccessException e) {
            Logger.trace("SeedGenerator constructor for " + className, e);
          } catch (InstantiationException e) {
            Logger.trace("SeedGenerator constructor for " + className, e);
          } catch (NoSuchMethodException e) {
            Logger.trace("SeedGenerator constructor for " + className, e);
          } catch (InvocationTargetException e) {
            Logger.trace("SeedGenerator constructor for " + className, e);
          }
          // If that failed try a long argument
          if (random == null) {
            Logger.trace("Trying to find a long constructor");
            try {
              Constructor<? extends Random> constructor = clazz.getConstructor(Long.TYPE);
              random = constructor.newInstance(bi.longValue());
              Logger.trace("Success");
            } catch (IllegalAccessException e) {
              Logger.trace("Long constructor for " + className, e);
            } catch (InstantiationException e) {
              Logger.trace("Long constructor for " + className, e);
            } catch (NoSuchMethodException e) {
              Logger.trace("Long constructor for " + className, e);
            } catch (InvocationTargetException e) {
              Logger.trace("Long constructor for " + className, e);
            }
          }
          if (random == null) {
            // Just instantiate the RNG
            try {
              Logger.trace("Trying to find no-arg constructor");
              random = clazz.getDeclaredConstructor().newInstance();
              Logger.trace("Success");
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e) {
              Logger.trace("No-arg constructor for " + className, e);
            }
          }
        } catch (ClassNotFoundException e) {
          Logger.debug("Class not found: " + className);
        }
        if (random == null) {
          // Everything failed
          // Just return a sensible RNG
          Logger.debug("Using fallback RNG");
          random = new MersenneTwisterRNG();
        }
      }
      return random;
    }
  }

  private void clearCache() {
    intData.clear();
    floatData.clear();
    booleanData.clear();
    arrayData.clear();
  }

  private void clearCache(String key) {
    intData.remove(key);
    floatData.remove(key);
    booleanData.remove(key);
    arrayData.remove(key);
  }

  private String processDollarNotation(String key, String value) {
    int index = value.indexOf("${");
    if (index == -1) {
      return value;
    }
    noCache.add(key);
    int end = value.indexOf("}", index);
    int colon = value.indexOf(":", index);
    String reference = value.substring(index + 2, end);
    String defaultValue = null;
    if (colon > index && colon < end) {
      reference = value.substring(index + 2, colon);
      defaultValue = value.substring(colon + 1, end);
    }
    StringBuilder result = new StringBuilder();
    result.append(value.substring(0, index));
    result.append(resolveReferences(reference, defaultValue));
    result.append(processDollarNotation(key, value.substring(end + 1)));
    return result.toString();
  }

  private String resolveReferences(String s, String defaultValue) {
    try {
      return getValue(s);
    } catch (NoSuchConfigOptionException e) {
      if (defaultValue != null) {
        return defaultValue;
      }
      throw e;
    }
  }

  private void checkAllConstraints() {
    violatedConstraints.clear();
    for (ConfigConstraint next : constraints) {
      checkConstraint(next);
    }
  }

  private void checkConstraint(ConfigConstraint c) {
    if (c == null) {
      return;
    }
    if (c.isViolated(this)) {
      violatedConstraints.add(c);
    } else {
      violatedConstraints.remove(c);
    }
  }

  private interface Context {
    /**
     * Get the name of this context for error logging.
     *
     * @return The name of this context.
     */
    String getName();

    /**
     * Read the context and update a config.
     *
     * @param config The config to update.
     * @throws ConfigException If there is a problem processing the context.
     */
    void process(Config config) throws ConfigException;

    /**
     * Get a new context for reading an included file.
     *
     * @param path The path to include.
     * @return A new context object.
     * @throws ConfigException If there is a problem creating the include context.
     */
    Context include(String path) throws ConfigException;
  }

  private static class FileContext implements Context {
    private File file;

    public FileContext(File file) {
      this.file = file;
    }

    @Override
    public String getName() {
      return file.getAbsolutePath();
    }

    @Override
    public void process(Config config) throws ConfigException {
      if (!file.exists()) {
        throw new ConfigException(getName(), "File does not exist");
      }
      if (file.isDirectory()) {
        for (File next : file.listFiles()) {
          new FileContext(next).process(config);
        }
        return;
      } else {
        try {
          config.readWithContext(new BufferedReader(new FileReader(file)), this);
        } catch (IOException e) {
          throw new ConfigException(getName(), e);
        }
      }
    }

    @Override
    public Context include(String path) throws ConfigException {
      File newFile = new File(file.getParentFile(), path);
      return new FileContext(newFile);
    }
  }

  private static class ReaderContext implements Context {
    private BufferedReader reader;
    private String name;

    public ReaderContext(Reader r, String name) {
      this.name = name;
      if (r instanceof BufferedReader) {
        reader = (BufferedReader) r;
      } else {
        reader = new BufferedReader(r);
      }
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void process(Config config) throws ConfigException {
      config.readWithContext(reader, this);
    }

    @Override
    public Context include(String path) throws ConfigException {
      throw new ConfigException(name, "Cannot process include directives when reading raw data streams");
    }
  }

  private static class ResourceContext implements Context {
    private String name;

    public ResourceContext(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void process(Config config) throws ConfigException {
      InputStream in = getClass().getClassLoader().getResourceAsStream(name);
      if (in == null) {
        throw new ConfigException(name, "Resource not found");
      }
      config.readWithContext(new BufferedReader(new InputStreamReader(in)), this);
    }

    @Override
    public Context include(String path) throws ConfigException {
      return new ResourceContext(path);
    }
  }

  private static class StaticSeedGenerator implements SeedGenerator {
    private byte[] data;

    StaticSeedGenerator(byte[] data) {
      this.data = data;
    }

    @Override
    public byte[] generateSeed(int length) {
      byte[] result = new byte[length];
      System.arraycopy(data, 0, result, 0, Math.min(data.length, result.length));
      return result;
    }
  }

  private enum LineType {
    INCLUDE {
      @Override
      public void process(Matcher matcher, Context context, Config config, int lineNumber) throws ConfigException {
        String includeName = matcher.group(1).trim();
        if ("".equals(includeName)) {
          throw new ConfigException(context.getName(), lineNumber, "Empty include directive");
        }
        Logger.trace("Reading included config '" + includeName + "'");
        Context newContext = context.include(includeName);
        newContext.process(config);
      }

      @Override
      protected String getRegex() {
        return "^!include\\s*(.*)";
      }
    },

    ADDITIVE {
      @Override
      public void process(Matcher matcher, Context context, Config config, int lineNumber) throws ConfigException {
        String key = matcher.group(1).trim();
        String value = matcher.group(2).trim();
        if ("".equals(key)) {
          throw new ConfigException(context.getName(), lineNumber, "Empty key");
        }
        if ("".equals(value)) {
          throw new ConfigException(context.getName(), lineNumber, "Empty value");
        }
        String existing = config.getValue(key, null);
        if (existing != null && !"".equals(existing)) {
          Logger.trace("Appending '" + value + "' to '" + key + "'");
          value = existing + " " + value;
        } else {
          Logger.trace("Setting '" + key + "' to '" + value + "'");
        }
        config.setValue(key, value);
      }

      @Override
      protected String getRegex() {
        return "^([^+]*)(?:\\+:|=)(.*)";
      }
    },

    NORMAL {
      @Override
      public void process(Matcher matcher, Context context, Config config, int lineNumber) throws ConfigException {
        String key = matcher.group(1).trim();
        String value = matcher.group(2).trim();
        if ("".equals(key)) {
          throw new ConfigException(context.getName(), lineNumber, "Empty key");
        }
        if ("".equals(value)) {
          throw new ConfigException(context.getName(), lineNumber, "Empty value");
        }
        Logger.trace("Setting '" + key + "' to '" + value + "'");
        if (config.isDefined(key) && !value.equals(config.getValue(key))) {
          Logger.warn("Redefining config key '" + key + "' as '" + value + "'");
        }
        config.setValue(key, value);
      }

      @Override
      protected String getRegex() {
        return "^([^:=]*)(?::|=)(.*)";
      }
    };

    private Pattern pattern;

    private LineType() {
      pattern = Pattern.compile(getRegex());
    }

    public Pattern getPattern() {
      return pattern;
    }

    protected abstract String getRegex();

    protected abstract void process(Matcher matcher, Context context, Config config, int lineNumber)
        throws ConfigException;

    public static void process(String line, Context context, Config config, int lineNumber) throws ConfigException {
      for (LineType next : values()) {
        Matcher matcher = next.getPattern().matcher(line);
        if (matcher.matches()) {
          next.process(matcher, context, config, lineNumber);
          return;
        }
      }
      throw new ConfigException(context.getName(), lineNumber, "Unrecognised config option: '" + line + "'");
    }
  }

  public Map<String, String> getData() {
    return data;
  }
}

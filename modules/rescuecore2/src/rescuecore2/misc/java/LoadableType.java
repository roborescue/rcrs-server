package rescuecore2.misc.java;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rescuecore2.components.Agent;
import rescuecore2.components.Component;
import rescuecore2.components.Simulator;
import rescuecore2.components.Viewer;
import rescuecore2.log.Logger;
import rescuecore2.registry.Factory;

/**
 * This class contains information about a type that can be detected in a jar
 * manifest or class file. For example, a MessageFactory implementation in a jar
 * file can be designated in the jar manifest under a "MessageFactory"
 * attribute, or implementation classes could be inferred by looking for a class
 * name regular expression like "(.*MessageFactory).class".
 *
 * Classes in the jar file can be inspected and checked to see if they extend
 * (or implement) a particular class (interface).
 */
public class LoadableType {
  public static final LoadableType GENERIC_FACTORY = new LoadableType("Factory", "(.+Factory).class", Factory.class);
  /** An Agent loadable type. */
  public static final LoadableType AGENT = new LoadableType("Agent",
      "(.+(?:FireBrigade|PoliceForce|AmbulanceTeam|Centre|Center|Civilian)).class", Agent.class);
  /** A Simulator loadable type. */
  public static final LoadableType SIMULATOR = new LoadableType("Simulator", "(.+Simulator).class", Simulator.class);
  /** A Viewer loadable type. */
  public static final LoadableType VIEWER = new LoadableType("Viewer", "(.+Viewer).class", Viewer.class);
  /** A Component loadable type. */
  public static final LoadableType COMPONENT = new LoadableType("Component", null, Component.class);

  private String manifestKey;
  private Pattern regex;
  private Class<?> clazz;

  /**
   * Construct a new LoadableType.
   *
   * @param manifestKey The key to look for in a jar manifest for this type.
   * @param regex       A regex to use for determining if a class name should be
   *                    tested.
   * @param clazz       A superclass for checking if candidate classes should be
   *                    extracted.
   */
  public LoadableType(String manifestKey, String regex, Class<?> clazz) {
    this.manifestKey = manifestKey;
    this.regex = regex == null ? null : Pattern.compile(regex);
    this.clazz = clazz;
  }

  /**
   * Inspect a jar manifest and extract the entries in the manifestKey attribute
   * if it exists. This method will check that entries also specify valid class
   * names.
   *
   * @param mf The manifest to check.
   * @return A list of class names.
   */
  public List<String> processManifest(Manifest mf) {
    Attributes att = mf.getMainAttributes();
    String value = att.getValue(manifestKey);
    List<String> result = new ArrayList<String>();
    if (value != null) {
      for (String next : value.split(" ")) {
        try {
          Class<?> testClass = Class.forName(next);
          if (!clazz.isAssignableFrom(testClass)) {
            Logger.warn("Manifest entry '" + manifestKey + "' contains invalid class name: '" + next
                + "' is not a subclass of '" + clazz.getName() + "'");
          } else if (testClass.isInterface()) {
            Logger.warn(
                "Manifest entry '" + manifestKey + "' contains invalid class name: '" + next + "' is an interface");
          } else {
            result.add(next);
          }
        } catch (ClassNotFoundException e) {
          Logger.warn("Manifest entry '" + manifestKey + "' contains invalid class name: '" + next + "' not found");
        }
      }
    }
    return result;
  }

  /**
   * Inspect an entry in a jar file and see if it names a conformant class.
   *
   * @param e The JarEntry to check.
   * @return The class name, or null if the entry does not name a conformant
   *         class.
   */
  public String processJarEntry(JarEntry e) {
    if (regex == null) {
      return null;
    }
    Matcher m = regex.matcher(e.getName());
    if (m.matches()) {
      String className = null;
      try {
        className = m.group(1).replace("/", ".");
        Class<?> testClass = Class.forName(className);
        if (clazz.isAssignableFrom(testClass) && !testClass.isInterface()) {
          return className;
        }
      } catch (ClassNotFoundException ex) {
        Logger.warn("Class " + className + " not found");
      }
    }
    return null;
  }
}
package rescuecore2.misc.java;

import java.util.List;
import java.util.ArrayList;
import java.util.jar.Manifest;
import java.util.jar.JarEntry;
import java.util.jar.Attributes;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import rescuecore2.messages.MessageFactory;
import rescuecore2.worldmodel.EntityFactory;
import rescuecore2.components.Agent;
import rescuecore2.components.Simulator;
import rescuecore2.components.Viewer;

/**
   This class contains information about a type that can be detected in a jar manifest or class file. For example, a MessageFactory implementation in a jar file can be designated in the jar manifest under a "MessageFactory" attribute, or implementation classes could be inferred by looking for a class name regular expression like "(.*MessageFactory).class".

   Classes in the jar file can be inspected and checked to see if they extend (or implement) a particular class (interface).
 */
public class LoadableType {
    /** A MessageFactory loadable type. */
    public static final LoadableType MESSAGE_FACTORY = new LoadableType("MessageFactory", "(.+MessageFactory).class", MessageFactory.class);
    /** An EntityFactory loadable type. */
    public static final LoadableType ENTITY_FACTORY = new LoadableType("EntityFactory", "(.+EntityFactory).class", EntityFactory.class);
    /** An Agent loadable type. */
    public static final LoadableType AGENT = new LoadableType("Agent", "(.+(?:FireBrigade|PoliceForce|AmbulanceTeam|Centre|Center)).class", Agent.class);
    /** A Simulator loadable type. */
    public static final LoadableType SIMULATOR = new LoadableType("Simulator", "(.+Simulator).class", Simulator.class);
    /** A Viewer loadable type. */
    public static final LoadableType VIEWER = new LoadableType("Viewer", "(.+Viewer).class", Viewer.class);

    private String manifestKey;
    private Pattern regex;
    private Class clazz;

    /**
       Construct a new LoadableType.
       @param manifestKey The key to look for in a jar manifest for this type.
       @param regex A regex to use for determining if a class name should be tested.
       @param clazz A superclass for checking if candidate classes should be extracted.
     */
    public LoadableType(String manifestKey, String regex, Class clazz) {
        this.manifestKey = manifestKey;
        this.regex = Pattern.compile(regex);
        this.clazz = clazz;
    }

    /**
       Inspect a jar manifest and extract the entries in the manifestKey attribute if it exists.
       @param mf The manifest to check.
       @return A list of class names.
    */
    public List<String> processManifest(Manifest mf) {
        Attributes att = mf.getMainAttributes();
        String value = att.getValue(manifestKey);
        List<String> result = new ArrayList<String>();
        if (value != null) {
            for (String next : value.split(" ")) {
                result.add(next);
            }
        }
        return result;
    }

    /**
       Inspect an entry in a jar file and see if it names a conformant class.
       @param e The JarEntry to check.
       @return The class name, or null if the entry does not name a conformant class.
    */
    @SuppressWarnings("unchecked")
    public String processJarEntry(JarEntry e) {
        Matcher m = regex.matcher(e.getName());
        if (m.matches()) {
            try {
                String className = m.group(1).replace("/", ".");
                Class testClass = Class.forName(className);
                if (clazz.isAssignableFrom(testClass) && !testClass.isInterface()) {
                    return className;
                }
            }
            catch (ClassNotFoundException ex) {
                System.out.println("Class not found");
            }
        }
        return null;
    }
}

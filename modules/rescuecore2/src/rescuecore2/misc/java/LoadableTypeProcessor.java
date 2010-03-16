package rescuecore2.misc.java;

import static rescuecore2.misc.java.JavaTools.instantiateFactory;

import rescuecore2.config.Config;
import rescuecore2.registry.MessageFactory;
import rescuecore2.registry.EntityFactory;
import rescuecore2.registry.PropertyFactory;
import rescuecore2.registry.Registry;
import rescuecore2.Constants;
import rescuecore2.log.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
   A utility class for processing loadable types from jar files.
 */
public class LoadableTypeProcessor {
    private List<LoadableTypeCallback> callbacks;
    private Set<LoadableType> types;
    private boolean deep;
    private String dir;
    private Set<String> ignore;

    /**
       Construct a LoadableTypeProcessor that will perform a deep inspection.
       @param config The system configuration.
    */
    public LoadableTypeProcessor(Config config) {
        callbacks = new ArrayList<LoadableTypeCallback>();
        types = new HashSet<LoadableType>();
        deep = config.getBooleanValue(Constants.DEEP_JAR_INSPECTION_KEY, Constants.DEFAULT_DEEP_JAR_INSPECTION);
        dir = config.getValue(Constants.JAR_DIR_KEY, Constants.DEFAULT_JAR_DIR);
        ignore = new HashSet<String>();
        ignore.addAll(config.getArrayValue(Constants.IGNORE_JARS_KEY, Constants.DEFAULT_IGNORE_JARS));
    }

    /**
       Add the message, property and entity factory register callbacks.
       @param registry The Registry to register factory classes with.
    */
    public void addFactoryRegisterCallbacks(Registry registry) {
        addCallback(new MessageFactoryRegisterCallback(registry));
        addCallback(new EntityFactoryRegisterCallback(registry));
        addCallback(new PropertyFactoryRegisterCallback(registry));
    }

    /**
       Add a LoadableTypeCallback function.
       @param callback The callback to add.
    */
    public void addCallback(LoadableTypeCallback callback) {
        callbacks.add(callback);
        types.addAll(callback.getTypes());
    }

    /**
       Add a config updating callback. This will append class names to a Config entry when a particular LoadableType returns an acceptable class.
       @param type The type to look for.
       @param config The config to update.
       @param configKey The key to update.
    */
    public void addConfigUpdater(LoadableType type, Config config, String configKey) {
        addCallback(new ConfigCallback(type, config, configKey));
    }

    /**
       Set whether to do a "deep" inspection or just inspect the manifest. If true then all entries will be tested to see if they match the target regex and class.
       @param newDeep Whether to do a deep inspection or not.
    */
    public void setDeepInspection(boolean newDeep) {
        this.deep = newDeep;
    }

    /**
       Set the name of the directory to process.
       @param name The name of the directory.
    */
    public void setDirectory(String name) {
        dir = name;
    }

    /**
       Process all jars in a directory.
       @throws IOException If there is a problem reading the jar files.
    */
    public void process() throws IOException {
        File baseDir = new File(dir);
        Logger.info("Processing jar directory: " + baseDir.getAbsolutePath());
        File[] jarFiles = baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dirName, String name) {
                    return name.endsWith(".jar");
                }
            });
        if (jarFiles == null) {
            return;
        }
        for (File next : jarFiles) {
            JarFile jar = new JarFile(next);
            processJarFile(jar);
        }
    }

    /**
       Inspect an individual jar file for loadable types.
       @param jar The jar file to inspect.
       @throws IOException If there is a problem reading the jar file.
    */
    public void processJarFile(JarFile jar) throws IOException {
        String name = jar.getName();
        String tail = name.substring(name.lastIndexOf("/") + 1);
        if (ignore.contains(tail)) {
            return;
        }
        Logger.info("Processing " + jar.getName());
        Manifest mf = jar.getManifest();
        if (mf != null) {
            Logger.debug("Inspecting manifest...");
            for (LoadableType type : types) {
                for (String next : type.processManifest(mf)) {
                    fireCallback(type, next);
                }
            }
        }
        if (deep) {
            // Look for well-named classes
            Logger.debug("Looking for likely class names...");
            for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
                JarEntry next = e.nextElement();
                for (LoadableType type : types) {
                    String s = type.processJarEntry(next);
                    if (s != null) {
                        fireCallback(type, s);
                    }
                }
            }
        }
    }

    private void fireCallback(LoadableType type, String classname) {
        for (LoadableTypeCallback next : callbacks) {
            if (next.getTypes().contains(type)) {
                next.classFound(type, classname);
            }
        }
    }

    private static class ConfigCallback implements LoadableTypeCallback {
        private LoadableType type;
        private Config config;
        private String key;

        public ConfigCallback(LoadableType type, Config config, String key) {
            this.type = type;
            this.config = config;
            this.key = key;
        }

        @Override
        public void classFound(LoadableType otherType, String className) {
            Logger.info("Adding " + className + " to " + key);
            if (config.isDefined(key)) {
                List<String> existing = config.getArrayValue(key);
                if (!existing.contains(className)) {
                    config.appendValue(key, className);
                }
            }
            else {
                config.setValue(key, className);
            }
        }

        @Override
        public Collection<LoadableType> getTypes() {
            return Collections.singleton(type);
        }
    }

    /**
       A LoadableTypeCallback that will registry MessageFactory implementations.
    */
    public static final class MessageFactoryRegisterCallback implements LoadableTypeCallback {
        private Registry registry;

        private MessageFactoryRegisterCallback(Registry registry) {
            this.registry = registry;
        }

        @Override
        public void classFound(LoadableType type, String className) {
            MessageFactory factory = instantiateFactory(className, MessageFactory.class);
            if (factory != null) {
                registry.registerMessageFactory(factory);
                Logger.info("Registered message factory '" + className + "' with registry " + registry.getName());
            }
        }

        @Override
        public Collection<LoadableType> getTypes() {
            return Collections.singleton(LoadableType.MESSAGE_FACTORY);
        }
    }

    /**
       A LoadableTypeCallback that will registry EntityFactory implementations.
    */
    public static final class EntityFactoryRegisterCallback implements LoadableTypeCallback {
        private Registry registry;

        private EntityFactoryRegisterCallback(Registry registry) {
            this.registry = registry;
        }

        @Override
        public void classFound(LoadableType type, String className) {
            EntityFactory factory = instantiateFactory(className, EntityFactory.class);
            if (factory != null) {
                registry.registerEntityFactory(factory);
                Logger.info("Registered entity factory '" + className + "' with registry " + registry.getName());
            }
        }

        @Override
        public Collection<LoadableType> getTypes() {
            return Collections.singleton(LoadableType.ENTITY_FACTORY);
        }
    }

    /**
       A LoadableTypeCallback that will registry PropertyFactory implementations.
    */
    public static final class PropertyFactoryRegisterCallback implements LoadableTypeCallback {
        private Registry registry;

        private PropertyFactoryRegisterCallback(Registry registry) {
            this.registry = registry;
        }

        @Override
        public void classFound(LoadableType type, String className) {
            PropertyFactory factory = instantiateFactory(className, PropertyFactory.class);
            if (factory != null) {
                registry.registerPropertyFactory(factory);
                Logger.info("Registered property factory '" + className + "' with registry " + registry.getName());
            }
        }

        @Override
        public Collection<LoadableType> getTypes() {
            return Collections.singleton(LoadableType.PROPERTY_FACTORY);
        }
    }
}

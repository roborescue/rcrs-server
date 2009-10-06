package rescuecore2.misc.java;

import static rescuecore2.misc.java.JavaTools.instantiateFactory;

import rescuecore2.config.Config;
import rescuecore2.messages.MessageRegistry;
import rescuecore2.messages.MessageFactory;
import rescuecore2.worldmodel.EntityRegistry;
import rescuecore2.worldmodel.EntityFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
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
    private boolean deep;

    /**
       Construct a LoadableTypeProcessor that will perform a deep inspection.
    */
    public LoadableTypeProcessor() {
        callbacks = new ArrayList<LoadableTypeCallback>();
        deep = true;
    }

    /**
       Add a LoadableTypeCallback function.
       @param callback The callback to add.
     */
    public void addCallback(LoadableTypeCallback callback) {
        callbacks.add(callback);
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
       Process all jars in a directory.
       @param dirName The name of the directory.
       @param types The LoadableTypes to process.
       @throws IOException If there is a problem reading the jar files.
     */
    public void processJarDirectory(String dirName, LoadableType... types) throws IOException {
        processJarDirectory(new File(dirName), types);
    }

    /**
       Process all jars in a directory.
       @param baseDir The directory.
       @param types The LoadableTypes to process.
       @throws IOException If there is a problem reading the jar files.
     */
    public void processJarDirectory(File baseDir, LoadableType... types) throws IOException {
        File[] jarFiles = baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
        for (File next : jarFiles) {
            JarFile jar = new JarFile(next);
            processJarFile(jar, types);
        }
    }

    /**
       Inspect a jar file for loadable types.
       @param jar The jar file to inspect.
       @param types The LoadableTypes to process.
       @throws IOException If there is a problem reading the jar file.
     */
    public void processJarFile(JarFile jar, LoadableType... types) throws IOException {
        System.out.println("Processing " + jar.getName());
        Manifest mf = jar.getManifest();
        if (mf != null) {
            System.out.println("Inspecting manifest...");
            for (LoadableType type : types) {
                for (String next : type.processManifest(mf)) {
                    fireCallback(type, next);
                }
            }
        }
        if (deep) {
            // Look for well-named classes
            System.out.println("Looking for likely class names...");
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
            next.classFound(type, classname);
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
            if (this.type == otherType) {
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
        }
    }

    /**
       A LoadableTypeCallback that will registry MessageFactory implementations.
     */
    public static class MessageFactoryRegisterCallback implements LoadableTypeCallback {
        @Override
        public void classFound(LoadableType type, String className) {
            if (LoadableType.MESSAGE_FACTORY == type) {
                MessageFactory factory = instantiateFactory(className, MessageFactory.class);
                if (factory != null) {
                    MessageRegistry.register(factory);
                    System.out.println("Registered message factory: " + className);
                }
            }
        }
    }

    /**
       A LoadableTypeCallback that will registry EntityFactory implementations.
     */
    public static class EntityFactoryRegisterCallback implements LoadableTypeCallback {
        @Override
        public void classFound(LoadableType type, String className) {
            if (LoadableType.ENTITY_FACTORY == type) {
                EntityFactory factory = instantiateFactory(className, EntityFactory.class);
                if (factory != null) {
                    EntityRegistry.register(factory);
                    System.out.println("Registered entity factory: " + className);
                }
            }
        }
    }
}
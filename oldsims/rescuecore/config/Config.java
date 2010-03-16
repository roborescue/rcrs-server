package rescuecore.config;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;

/**
   This class represents a config file and any other config files that might have been included with a !include directive. Config files must be defined relative to a base directory so that includes can be resolved.
 */
public class Config {
    private static final String INCLUDE = "!include";

    /**
       The raw data and caches of int/float/boolean interpretations.
    */
    private Map<String, String> data;
    private Map<String, Integer> intData;
    private Map<String, Double> floatData;
    private Map<String, Boolean> booleanData;

    /**
       Create an empty config.
    */
    public Config() {
        data = new HashMap<String, String>();
        intData = new HashMap<String, Integer>();
        floatData = new HashMap<String, Double>();
        booleanData = new HashMap<String, Boolean>();
    }

    /**
       Create a config that reads from a given file. Additional config files can be read later with the {@link read(String)} method.
       @param file The config file to read. Must not be null.
       @throws IOException If there is an error reading the file.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    public Config(File file) throws IOException, ConfigException {
        this();
        read(file);
    }

    /**
       Read a config file and add its contents. Existing entries with the same name will be overwritten.
       @param file The config file to read. Must not be null. If this is a directory then all files in the directory will be read.
       @throws IOException If there is an error reading the file.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    public void read(File file) throws IOException, ConfigException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
        }
        if (file.isDirectory()) {
            for (File next : file.listFiles()) {
                read(next);
            }
        }
        else {
            readConfigFile(file);
        }
    }

    /**
       Read config information from a Reader and add its contents. Existing entries with the same name will be overwritten.
       @param in The Reader to read from. Must not be null.
       @throws IOException If there is an error reading the file.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    private void readConfigFile(File in) throws IOException, ConfigException {
        BufferedReader reader = new BufferedReader(new FileReader(in));
        String name = in.getAbsolutePath();
        String line = "";
        int lineNumber = 0;
        try {
            while (line != null) {
                line = reader.readLine();
                ++lineNumber;
                if (line != null) {
                    line = line.trim();
                    // Ignore empty lines
                    if ("".equals(line)) {
                        continue;
                    }
                    // Ignore lines that start with #
                    if (line.startsWith("#")) {
                        continue;
                    }
                    // Look for a !include
                    else if (line.startsWith(INCLUDE)) {
                        if (INCLUDE.equals(line)) {
                            throw new ConfigException(name, "Line " + lineNumber + ": Empty include directive");
                        }
                        String includeName = line.substring(INCLUDE.length() + 1).trim();
                        if ("".equals(includeName)) {
                            throw new ConfigException(name, "Line " + lineNumber + ": Empty include directive");
                        }
                        read(new File(in.getParentFile(), includeName));
                    }
                    else {
                        int index = line.indexOf(':');
                        if (index == -1) {
                            throw new ConfigException(name, "Line " + lineNumber + ": No ':' found");
                        }
                        if (index == line.length() - 1) {
                            throw new ConfigException(name, "Line " + lineNumber + ": No value found");
                        }
                        if (index == 0) {
                            throw new ConfigException(name, "Line " + lineNumber + ": No key found");
                        }
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();
                        data.put(key, value);
                        intData.remove(key);
                        floatData.remove(key);
                        booleanData.remove(key);
                    }
                }
            }
        }
        finally {
            reader.close();
        }
    }

    /**
       Write this config to a PrintWriter.
       @param out The PrintWriter to write to. Must not be null.
       @throws IOException If there is an error writing to the stream.
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
       Get all keys in this config.
       @return An immutable view of all keys.
    */
    public Set<String> getAllKeys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
       Get the value of a key as a String.
       @param key The key to look up. Must not be null.
       @return The value associated with that key.
       @throws NoSuchConfigOptionException If the key is not defined.
    */
    public String getValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (!data.containsKey(key)) {
            throw new NoSuchConfigOptionException(key);
        }
        return data.get(key);
    }

    /**
       Get the value of a key as an integer.
       @param key The key to look up. Must not be null.
       @return The value associated with that key interpreted as an integer.
       @throws NoSuchConfigOptionException If the key is not defined.
       @throws NumberFormatException If the value of the key cannot be interpreted as an integer.
    */
    public int getIntValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (intData.containsKey(key)) {
            return intData.get(key);
        }
        int result = Integer.parseInt(getValue(key));
        intData.put(key, result);
        return result;
    }

    /**
       Get the value of a key as a floating point number.
       @param key The key to look up. Must not be null.
       @return The value associated with that key interpreted as a floating point number.
       @throws NoSuchConfigOptionException If the key is not defined.
       @throws NumberFormatException If the value of the key cannot be interpreted as a floating point number.
    */
    public double getFloatValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (floatData.containsKey(key)) {
            return floatData.get(key);
        }
        double result = Double.parseDouble(getValue(key));
        floatData.put(key, result);
        return result;
    }

    /**
       Get the value of a key as a boolean. "true", "t", "yes", "y" and "1" (case insensitive) are all interpreted as true, all other values are false.
       @param key The key to look up. Must not be null.
       @return The value associated with that key interpreted as a boolean.
       @throws NoSuchConfigOptionException If the key is not defined.
    */
    public boolean getBooleanValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (booleanData.containsKey(key)) {
            return booleanData.get(key);
        }
        boolean result = false;
        String value = getValue(key);
        if ("true".equalsIgnoreCase(value)
            || "t".equalsIgnoreCase(value)
            || "yes".equalsIgnoreCase(value)
            || "y".equalsIgnoreCase(value)
            || "1".equalsIgnoreCase(value)) {
            result = true;
        }
        booleanData.put(key, result);
        return result;
    }

    /**
       Set the value of a key.
       @param key The key to set. Must not be null.
       @param value The new value. If this is null then {@link #removeKey(String)} is called with the given key.
    */
    public void setValue(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            removeKey(key);
            return;
        }
        data.put(key, value);
        intData.remove(key);
        floatData.remove(key);
        booleanData.remove(key);
    }

    /**
       Set the value of a key as an integer.
       @param key The key to set. Must not be null.
       @param value The new value.
    */
    public void setIntValue(String key, int value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        data.put(key, Integer.valueOf(value).toString());
        intData.put(key, value);
        floatData.remove(key);
        booleanData.remove(key);
    }

    /**
       Set the value of a key as a floating point number.
       @param key The key to set. Must not be null.
       @param value The new value.
    */
    public void setFloatValue(String key, double value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        data.put(key, Double.valueOf(value).toString());
        intData.remove(key);
        floatData.put(key, value);
        booleanData.remove(key);
    }

    /**
       Set the value of a key as a boolean.
       @param key The key to set. Must not be null.
       @param value The new value.
    */
    public void setBooleanValue(String key, boolean value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        data.put(key, value ? "true" : "false");
        intData.remove(key);
        floatData.remove(key);
        booleanData.put(key, value);
    }

    /**
       Remove a key from the config.
       @param key The key to remove. Must not be null.
    */
    public void removeKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        data.remove(key);
        intData.remove(key);
        floatData.remove(key);
        booleanData.remove(key);
    }

    /**
       Remove all keys.
    */
    public void removeAllKeys() {
        data.clear();
        intData.clear();
        floatData.clear();
        booleanData.clear();
    }
}

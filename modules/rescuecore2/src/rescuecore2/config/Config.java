package rescuecore2.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.misc.Pair;

/**
   This class represents a config file and any other config files that might have been included with a !include directive. Config files must be defined relative to a base directory so that includes can be resolved.
 */
public class Config {
    private static final String INCLUDE = "!include";

    /**
       The raw data and caches of int/float/boolean/array interpretations.
    */
    private Map<String, String> data;
    // Entries that should not be cached
    private Set<String> noCache;
    private Map<String, Integer> intData;
    private Map<String, Double> floatData;
    private Map<String, Boolean> booleanData;
    private Map<String, Pair<String, List<String>>> arrayData;

    /**
       Create an empty config.
    */
    public Config() {
        data = new HashMap<String, String>();
        noCache = new HashSet<String>();
        intData = new HashMap<String, Integer>();
        floatData = new HashMap<String, Double>();
        booleanData = new HashMap<String, Boolean>();
        arrayData = new HashMap<String, Pair<String, List<String>>>();
    }

    /**
       Create a config that reads from a given file. Additional config files can be read later with the {@link #read(String)} method.
       @param file The config file to read. Must not be null.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    public Config(File file) throws ConfigException {
        this();
        read(file);
    }

    /**
       Copy constructor. The new Config will contain all keys and values that are currently in this config.
       @param other The Config to copy.
    */
    public Config(Config other) {
        this();
        this.data.putAll(other.data);
    }

    /**
       Read a config file from a resource that this class' classloader can find.
       @param resource The name of the resource.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    public void read(String resource) throws ConfigException {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }
        new ResourceContext(resource).process(this);
    }

    /**
       Read a config file and add its contents. Existing entries with the same name will be overwritten.
       @param file The config file to read. Must not be null. If this is a directory then all files in the directory tree will be read.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    public void read(File file) throws ConfigException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        new FileContext(file).process(this);
    }

    /**
       Read config information from a Reader and add its contents. Existing entries with the same name will be overwritten.
       @param reader The Reader to read from. Must not be null.
       @param name The name of the reader being read. This is used when reporting errors in the file.
       @throws ConfigException If there is an error parsing the config file or one of its descendants.
    */
    public void read(Reader reader, String name) throws ConfigException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        new ReaderContext(reader, name).process(this);
    }

    private void readWithContext(BufferedReader reader, Context context) throws ConfigException {
        String line = "";
        int lineNumber = 0;
        String name = context.getName();
        try {
            while (line != null) {
                try {
                    line = reader.readLine();
                }
                catch (IOException e) {
                    throw new ConfigException(name, e);
                }
                ++lineNumber;
                if (line != null) {
                    line = line.trim();
                    // Strip off everything after a #
                    int hashIndex = line.indexOf("#");
                    if (hashIndex != -1) {
                        line = line.substring(0, hashIndex).trim();
                    }
                    // Ignore empty lines
                    if ("".equals(line)) {
                        continue;
                    }
                    // Look for a !include
                    else if (line.startsWith(INCLUDE)) {
                        if (INCLUDE.equals(line)) {
                            throw new ConfigException(name, "Line " + lineNumber + ": Empty include directive");
                        }
                        String includeName = line.substring(INCLUDE.length() + 1).trim();
                        // includeName cannot be the empty string because the line was trimmed when it was read from the stream.
                        Context newContext = context.include(includeName);
                        newContext.process(this);
                    }
                    else {
                        int index1 = line.indexOf(':');
                        int index2 = line.indexOf('=');
                        if (index1 == -1 && index2 == -1) {
                            throw new ConfigException(name, "Line " + lineNumber + ": No ':' or '=' found");
                        }
                        int index;
                        if (index1 == -1) {
                            index = index2;
                        }
                        else if (index2 == -1) {
                            index = index1;
                        }
                        else {
                            index = Math.min(index1, index2);
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
                        noCache.remove(key);
                        clearCache(key);
                    }
                }
            }
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                // FIXME: Log it!
                e.printStackTrace();
            }
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
       Merge all keys and values from another Config into this one.
       @param other The Config to merge from.
     */
    public void merge(Config other) {
        clearCache();
        this.data.putAll(other.data);
    }

    /**
       Get all keys in this config.
       @return An immutable view of all keys.
    */
    public Set<String> getAllKeys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
       Find out if a key is defined.
       @param key The key to test.
       @return True if the key has a non-null value.
    */
    public boolean isDefined(String key) {
        return data.containsKey(key);
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
        return processDollarNotation(key, data.get(key));
    }

    /**
       Get the value of a key as a String or use a default value.
       @param key The key to look up. Must not be null.
       @param defaultValue The default value to return if the key has no defined value.
       @return The value associated with that key, or the default value of the key has no value.
    */
    public String getValue(String key, String defaultValue) {
        try {
            return getValue(key);
        }
        catch (NoSuchConfigOptionException e) {
            return defaultValue;
        }
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
        if (!noCache.contains(key) && intData.containsKey(key)) {
            return intData.get(key);
        }
        int result = Integer.parseInt(getValue(key));
        intData.put(key, result);
        return result;
    }

    /**
       Get the value of a key as an integer or use a default value.
       @param key The key to look up. Must not be null.
       @param defaultValue The default value to return if the key has no defined value.
       @return The value associated with that key interpreted as an integer, or the default value of the key has no value.
       @throws NumberFormatException If the value of the key cannot be interpreted as an integer.
    */
    public int getIntValue(String key, int defaultValue) {
        try {
            return getIntValue(key);
        }
        catch (NoSuchConfigOptionException e) {
            return defaultValue;
        }
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
        if (!noCache.contains(key) && floatData.containsKey(key)) {
            return floatData.get(key);
        }
        double result = Double.parseDouble(getValue(key));
        floatData.put(key, result);
        return result;
    }

    /**
       Get the value of a key as a floating point number or use a default value.
       @param key The key to look up. Must not be null.
       @param defaultValue The default value to return if the key has no defined value.
       @return The value associated with that key interpreted as a floating point number, or the default value of the key has no value.
       @throws NumberFormatException If the value of the key cannot be interpreted as a floating point number.
    */
    public double getFloatValue(String key, double defaultValue) {
        try {
            return getFloatValue(key);
        }
        catch (NoSuchConfigOptionException e) {
            return defaultValue;
        }
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
        if (!noCache.contains(key) && booleanData.containsKey(key)) {
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
       Get the value of a key as a boolean or use a default value. "true", "t", "yes", "y" and "1" (case insensitive) are all interpreted as true, all other values are false.
       @param key The key to look up. Must not be null.
       @param defaultValue The default value to return if the key has no defined value.
       @return The value associated with that key interpreted as a boolean, or the default value of the key has no value.
    */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        try {
            return getBooleanValue(key);
        }
        catch (NoSuchConfigOptionException e) {
            return defaultValue;
        }
    }

    /**
       Get the value of a key as an array of strings. The value will be split on space and comma characters and the resulting list of tokens is returned.
       @param key The key to look up. Must not be null.
       @return The value associated with that key interpreted as an array of space-and-comma-separated tokens.
       @throws NoSuchConfigOptionException If the key is not defined.
    */
    public List<String> getArrayValue(String key) {
        return getArrayValue(key, " |,");
    }

    /**
       Get the value of a key as an array of strings. The value will be split using the given regex and the resulting list of tokens is returned.
       @param key The key to look up. Must not be null.
       @param regex The regular expression to split the value on. This is passed to {@link String#split(String)} directly. Must not be null or the empty string.
       @return The value associated with that key interpreted as an array of regex-separated tokens.
       @throws NoSuchConfigOptionException If the key is not defined.
    */
    public List<String> getArrayValue(String key, String regex) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (regex == null) {
            throw new IllegalArgumentException("Regex cannot be null");
        }
        if ("".equals(regex)) {
            throw new IllegalArgumentException("Regex cannot be the empty string");
        }
        if (!noCache.contains(key) && arrayData.containsKey(key)) {
            Pair<String, List<String>> entry = arrayData.get(key);
            if (entry.first().equals(regex)) {
                return entry.second();
            }
        }
        String value = getValue(key);
        List<String> result = new ArrayList<String>();
        String[] s = value.split(regex);
        for (String next : s) {
            if (!"".equals(next)) {
                result.add(next);
            }
        }
        Pair<String, List<String>> entry = new Pair<String, List<String>>(regex, result);
        arrayData.put(key, entry);
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
        clearCache(key);
        noCache.remove(key);
        data.put(key, value);
    }

    /**
       Append a value to a key. If there is no value for the key then this is equivalent to {@link #setValue(String, String)}. This method calls {@link #appendValue(String, String, String)} with a space character as the separator.
       @param key The key to append.
       @param value The value to append.
     */
    public void appendValue(String key, String value) {
        appendValue(key, value, " ");
    }

    /**
       Append a value to a key. If there is no value for the key then this is equivalent to {@link #setValue(String, String)}.
       @param key The key to append.
       @param value The value to append.
       @param separator A string to add before the new value to separate it from the previous value.
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
        }
        else {
            data.put(key, value);
        }
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
        clearCache(key);
        noCache.remove(key);
        data.put(key, Integer.valueOf(value).toString());
        intData.put(key, value);
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
        clearCache(key);
        noCache.remove(key);
        data.put(key, Double.valueOf(value).toString());
        floatData.put(key, value);
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
        clearCache(key);
        noCache.remove(key);
        data.put(key, value ? "true" : "false");
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
        clearCache(key);
        noCache.remove(key);
        data.remove(key);
    }

    /**
       Remove all keys.
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
       Remove all except a set of keys.
       @param exceptions The keys to keep.
    */
    public void removeExcept(Collection<String> exceptions) {
        data.keySet().retainAll(exceptions);
        noCache.retainAll(exceptions);
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
        }
        catch (NoSuchConfigOptionException e) {
            if (defaultValue != null) {
                return defaultValue;
            }
            throw e;
        }
    }

    private interface Context {
        /**
           Get the name of this context for error logging.
           @return The name of this context.
        */
        String getName();

        /**
           Read the context and update a config.
           @param config The config to update.
           @throws ConfigException If there is a problem processing the context.
        */
        void process(Config config) throws ConfigException;

        /**
           Get a new context for reading an included file.
           @param path The path to include.
           @return A new context object.
           @throws ConfigException If there is a problem creating the include context.
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
            }
            else {
                try {
                    config.readWithContext(new BufferedReader(new FileReader(file)), this);
                }
                catch (IOException e) {
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
                reader = (BufferedReader)r;
            }
            else {
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
}
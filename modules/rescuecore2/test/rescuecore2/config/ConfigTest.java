package rescuecore2.config;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Set;
import java.util.List;

public class ConfigTest {
    private Config config;
    private File baseDir;

    private final static double TOLERANCE = 0.00001;

    private final static String BASIC_CONFIG = "supportfiles/config/basic.cfg";
    private final static String INCLUDE_CONFIG = "supportfiles/config/include.cfg";
    private final static String INCLUDE_CONFIG_2 = "supportfiles/config/include2.cfg";
    private final static String MORE_CONFIG = "supportfiles/config/more.cfg";
    private final static String WHITESPACE_CONFIG = "supportfiles/config/whitespace.cfg";
    private final static String CONFIG_DIR = "supportfiles/config/configdir";
    private final static String CONFIG_TREE = "supportfiles/config/configtree";
    private final static String CONFIG_INCLUDE_SUBDIR_FILE = "supportfiles/config/include_subdir_file.cfg";
    private final static String CONFIG_INCLUDE_SUBDIR = "supportfiles/config/include_subdir.cfg";
    private final static String NONEXISTANT_INCLUDE = "supportfiles/config/nonexistant_include.cfg";
    private final static String EMPTY_INCLUDE = "supportfiles/config/empty_include.cfg";
    private final static String MALFORMED_1 = "supportfiles/config/malformed1.cfg";
    private final static String MALFORMED_2 = "supportfiles/config/malformed2.cfg";
    private final static String MALFORMED_3 = "supportfiles/config/malformed3.cfg";
    private final static String MALFORMED_4 = "supportfiles/config/malformed4.cfg";
    private final static String MALFORMED_5 = "supportfiles/config/malformed5.cfg";
    private final static String MALFORMED_6 = "supportfiles/config/malformed6.cfg";
    private final static String BOOLEAN_CONFIG = "supportfiles/config/boolean.cfg";
    private final static String ARRAY_CONFIG = "supportfiles/config/array.cfg";

    private final static String RESOURCE_CONFIG = "rescuecore2/config/resource.cfg";

    private final static String DOLLAR_CONFIG = "supportfiles/config/dollar.cfg";
    private final static String DOLLAR_CONFIG_2 = "supportfiles/config/dollar2.cfg";
    private final static String DOLLAR_CONFIG_BAD = "supportfiles/config/dollar-bad.cfg";
    private final static String DOLLAR_CONFIG_DEFAULT = "supportfiles/config/dollar-default.cfg";

    @Before
    public void setup() {
        baseDir = new File(System.getProperty("tests.basedir"));
        config = new Config();
    }

    @Test
    public void testConstructor() {
	// The config should be empty
        assertEquals(0, config.getAllKeys().size());
    }

    @Test
    public void testReadFile() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
    }

    @Test
    public void testReadReader() throws IOException, ConfigException {
        config.read(new BufferedReader(new FileReader(new File(baseDir, BASIC_CONFIG))), BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
    }

    @Test(expected=ConfigException.class)
    public void testReadReaderWithInclude() throws IOException, ConfigException {
        config.read(new BufferedReader(new FileReader(new File(baseDir, INCLUDE_CONFIG))), INCLUDE_CONFIG);
    }

    @Test
    public void testReadResource() throws IOException, ConfigException {
        config.read(RESOURCE_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("something", config.getValue("resourcekey"));
        assertEquals("-9", config.getValue("resourceint"));
        assertEquals("27.4", config.getValue("resourcefloat"));
        assertEquals("false", config.getValue("resourceboolean"));
    }

    @Test(expected=NoSuchConfigOptionException.class)
    public void testCommentsIgnored() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        config.getValue("ignore");
    }

    @Test
    public void testReadFromConstructor() throws IOException, ConfigException {
        config = new Config(new File(baseDir, BASIC_CONFIG));
        assertEquals(4, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
    }

    @Test
    public void testReadWithIncludes() throws IOException, ConfigException {
        read(INCLUDE_CONFIG);
        assertEquals(5, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("newvalue", config.getValue("newkey"));
        // The int value should have been replaced by the value in INCLUDE_CONFIG
        assertEquals("2", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));        
    }

    @Test
    public void testReadWithIncludes2() throws IOException, ConfigException {
        read(INCLUDE_CONFIG_2);
        assertEquals(5, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("newvalue", config.getValue("newkey"));
        // The int value should have been replaced by the value in BASIC_CONFIG
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));        
    }

    @Test
    public void testReadMultiple() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
        read(MORE_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("newvalue", config.getValue("key"));
        assertEquals("6", config.getValue("int"));
        assertEquals("9.4", config.getValue("float"));
        assertEquals("false", config.getValue("boolean"));
    }

    @Test
    public void testReadMultipleWithIncludes() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
        read(INCLUDE_CONFIG);
        assertEquals(5, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("newvalue", config.getValue("newkey"));
        // The int value should have been replaced by the value in INCLUDE_CONFIG
        assertEquals("2", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));        
    }

    @Test
    public void testReadWithIncludeSubdir() throws IOException, ConfigException {
        read(CONFIG_INCLUDE_SUBDIR);
        assertEquals(3, config.getAllKeys().size());
        assertEquals("value1", config.getValue("key1"));
        assertEquals("a", config.getValue("key_a"));
        assertEquals("b", config.getValue("key_b"));
    }

    @Test
    public void testReadWithIncludeSubdirFile() throws IOException, ConfigException {
        read(CONFIG_INCLUDE_SUBDIR_FILE);
        assertEquals(2, config.getAllKeys().size());
        assertEquals("value1", config.getValue("key1"));
        assertEquals("a", config.getValue("key_a"));
    }

    @Test
    public void testReadEntireDirectory() throws IOException, ConfigException {
        read(CONFIG_DIR);
        assertEquals(2, config.getAllKeys().size());
        assertEquals("a", config.getValue("key_a"));
        assertEquals("b", config.getValue("key_b"));
    }

    @Test
    public void testReadEntireDirectoryTree() throws IOException, ConfigException {
        read(CONFIG_TREE);
        assertEquals(2, config.getAllKeys().size());
        assertEquals("a", config.getValue("key_a"));
        assertEquals("b", config.getValue("key_b"));
    }

    @Test
    public void testWhitespace() throws IOException, ConfigException {
        read(WHITESPACE_CONFIG);
        assertEquals("value1", config.getValue("key1"));
        assertEquals("value2", config.getValue("key2"));
        assertEquals("value3", config.getValue("key3"));
        assertEquals("value4", config.getValue("key4"));
        assertEquals("value5", config.getValue("key5"));
        assertEquals("value6", config.getValue("key6"));
        assertEquals("value7", config.getValue("key7"));
        assertEquals("value8", config.getValue("key8"));
        assertEquals("value9", config.getValue("key9"));
        assertEquals("value10", config.getValue("key10"));
    }

    @Test(expected=NoSuchConfigOptionException.class)
    public void testGetCaseSensitive() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals("value", config.getValue("key"));
        config.getValue("KEY");
    }

    @Test
    public void testGetAllKeys() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        Set<String> keys = config.getAllKeys();
        assertEquals(4, keys.size());
        assertTrue(keys.contains("key"));
        assertTrue(keys.contains("int"));
        assertTrue(keys.contains("float"));
        assertTrue(keys.contains("boolean"));
    }

    @Test
    public void testStrings() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("value", config.getValue("key"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
        config.setValue("newkey", "newvalue");
        assertEquals(5, config.getAllKeys().size());
        assertTrue(config.getAllKeys().contains("newkey"));
        assertEquals("value", config.getValue("key"));
        assertEquals("newvalue", config.getValue("newkey"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
        config.setValue("key", "changedvalue");
        assertEquals(5, config.getAllKeys().size());
        assertEquals("changedvalue", config.getValue("key"));
        assertEquals("newvalue", config.getValue("newkey"));
        assertEquals("5", config.getValue("int"));
        assertEquals("3.4", config.getValue("float"));
        assertEquals("true", config.getValue("boolean"));
    }

    @Test
    public void testIntegers() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        try {
            config.getIntValue("key");
            fail("Expected a NumberFormatException when trying to read a string value as an integer");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("value", config.getValue("key"));
        assertEquals(5, config.getIntValue("int"));
        assertEquals("5", config.getValue("int"));
        try {
            config.getIntValue("float");
            fail("Expected a NumberFormatException when trying to read a float value as an integer");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("3.4", config.getValue("float"));
        try {
            config.getIntValue("boolean");
            fail("Expected a NumberFormatException when trying to read a boolean value as an integer");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("true", config.getValue("boolean"));

        config.setIntValue("newkey", 12);
        assertEquals(5, config.getAllKeys().size());
        assertTrue(config.getAllKeys().contains("newkey"));
        assertEquals(12, config.getIntValue("newkey"));
        assertEquals("12", config.getValue("newkey"));
        assertEquals(5, config.getIntValue("int"));
        assertEquals("5", config.getValue("int"));
        config.setIntValue("int", -4);
        assertEquals(5, config.getAllKeys().size());
        assertEquals(12, config.getIntValue("newkey"));
        assertEquals("12", config.getValue("newkey"));
        assertEquals(-4, config.getIntValue("int"));
        assertEquals("-4", config.getValue("int"));
    }

    @Test
    public void testDoubles() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        try {
            config.getFloatValue("key");
            fail("Expected a NumberFormatException when trying to read a string value as a floating point number");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("value", config.getValue("key"));
        assertEquals(5, config.getFloatValue("int"), TOLERANCE);
        assertEquals("5", config.getValue("int"));
        assertEquals(3.4, config.getFloatValue("float"), TOLERANCE);
        assertEquals("3.4", config.getValue("float"));
        try {
            config.getFloatValue("boolean");
            fail("Expected a NumberFormatException when trying to read a boolean value as a floating point number");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("true", config.getValue("boolean"));

        config.setFloatValue("newkey", -9.3);
        assertEquals(5, config.getAllKeys().size());
        assertTrue(config.getAllKeys().contains("newkey"));
        assertEquals(-9.3, config.getFloatValue("newkey"), TOLERANCE);
        assertEquals("-9.3", config.getValue("newkey"));
        assertEquals(3.4, config.getFloatValue("float"), TOLERANCE);
        assertEquals("3.4", config.getValue("float"));
        config.setFloatValue("float", 127.02);
        assertEquals(5, config.getAllKeys().size());
        assertEquals(-9.3, config.getFloatValue("newkey"), TOLERANCE);
        assertEquals("-9.3", config.getValue("newkey"));
        assertEquals(127.02, config.getFloatValue("float"), TOLERANCE);
        assertEquals("127.02", config.getValue("float"));
    }

    @Test
    public void testBooleans() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertFalse(config.getBooleanValue("key"));
        assertEquals("value", config.getValue("key"));
        assertFalse(config.getBooleanValue("int"));
        assertEquals("5", config.getValue("int"));
        assertFalse(config.getBooleanValue("float"));
        assertEquals("3.4", config.getValue("float"));
        assertTrue(config.getBooleanValue("boolean"));
        assertEquals("true", config.getValue("boolean"));

        config.setBooleanValue("newkey", true);
        assertEquals(5, config.getAllKeys().size());
        assertTrue(config.getAllKeys().contains("newkey"));
        assertTrue(config.getBooleanValue("newkey"));
        assertEquals("true", config.getValue("newkey"));
        assertTrue(config.getBooleanValue("boolean"));
        assertEquals("true", config.getValue("boolean"));

        config.setBooleanValue("boolean", false);
        assertEquals(5, config.getAllKeys().size());
        assertTrue(config.getBooleanValue("newkey"));
        assertEquals("true", config.getValue("newkey"));
        assertFalse(config.getBooleanValue("boolean"));
        assertEquals("false", config.getValue("boolean"));
    }

    @Test
    public void testParseBooleans() throws IOException, ConfigException {
        read(BOOLEAN_CONFIG);
        assertTrue(config.getBooleanValue("key1"));
        assertTrue(config.getBooleanValue("key2"));
        assertTrue(config.getBooleanValue("key3"));
        assertTrue(config.getBooleanValue("key4"));
        assertTrue(config.getBooleanValue("key5"));
        assertTrue(config.getBooleanValue("key6"));
        assertTrue(config.getBooleanValue("key7"));
        assertTrue(config.getBooleanValue("key8"));
        assertTrue(config.getBooleanValue("key9"));
        assertFalse(config.getBooleanValue("key10"));
        assertFalse(config.getBooleanValue("key11"));
        assertFalse(config.getBooleanValue("key12"));
        assertFalse(config.getBooleanValue("key13"));
        assertFalse(config.getBooleanValue("key14"));
        assertFalse(config.getBooleanValue("key15"));
        assertFalse(config.getBooleanValue("key16"));
        assertFalse(config.getBooleanValue("key17"));
    }

    @Test
    public void testArrays() throws IOException, ConfigException {
        read(ARRAY_CONFIG);
        assertEquals(6, config.getAllKeys().size());
        assertEquals(1, config.getArrayValue("key1").size());
        assertEquals(2, config.getArrayValue("key2").size());
        assertEquals(2, config.getArrayValue("key3").size());
        assertEquals(1, config.getArrayValue("key4").size());
        assertEquals(2, config.getArrayValue("key5").size());
        assertEquals(2, config.getArrayValue("key6").size());
        assertTrue(config.getArrayValue("key1").contains("value"));
        assertTrue(config.getArrayValue("key2").contains("value1"));
        assertTrue(config.getArrayValue("key2").contains("value2"));
        assertTrue(config.getArrayValue("key3").contains("value1"));
        assertTrue(config.getArrayValue("key3").contains("value2"));
        assertTrue(config.getArrayValue("key4").contains("value1|value2"));
        assertTrue(config.getArrayValue("key5").contains("value1"));
        assertTrue(config.getArrayValue("key5").contains("value2"));
        assertTrue(config.getArrayValue("key6").contains("value1"));
        assertTrue(config.getArrayValue("key6").contains("value2"));
    }

    @Test
    public void testArraysDefaultValue() throws IOException, ConfigException {
        assertEquals(0, config.getAllKeys().size());
        List<String> l = config.getArrayValue("key", "value");
        assertEquals(1, l.size());
        assertTrue(l.contains("value"));
        l = config.getArrayValue("key", "value1,value2");
        assertEquals(2, l.size());
        assertTrue(l.contains("value1"));
        assertTrue(l.contains("value2"));
        l = config.getArrayValue("key", "value1 value2");
        assertEquals(2, l.size());
        assertTrue(l.contains("value1"));
        assertTrue(l.contains("value2"));
        l = config.getArrayValue("key", "");
        assertEquals(0, l.size());
        l = config.getArrayValue("key", null);
        assertNull(l);
    }

    @Test
    public void testRemoveKey() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        Set<String> keys = config.getAllKeys();
        assertEquals(4, keys.size());
        assertTrue(keys.contains("key"));
        assertTrue(keys.contains("int"));
        assertTrue(keys.contains("float"));
        assertTrue(keys.contains("boolean"));

        config.removeKey("key");
        keys = config.getAllKeys();
        assertEquals(3, keys.size());
        assertFalse(keys.contains("key"));
        assertTrue(keys.contains("int"));
        assertTrue(keys.contains("float"));
        assertTrue(keys.contains("boolean"));
    }

    @Test
    public void testRemoveAllKeys() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        Set<String> keys = config.getAllKeys();
        assertEquals(4, keys.size());
        assertTrue(keys.contains("key"));
        assertTrue(keys.contains("int"));
        assertTrue(keys.contains("float"));
        assertTrue(keys.contains("boolean"));

        config.removeAllKeys();
        keys = config.getAllKeys();
        assertEquals(0, keys.size());
        // Check that keys are actually gone
        try {
            config.getValue("key");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        try {
            config.getValue("int");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        try {
            config.getValue("float");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        try {
            config.getValue("boolean");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadNullFile() throws IOException, ConfigException {
        config.read((File)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadNullString() throws IOException, ConfigException {
        config.read((String)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadNullReader() throws IOException, ConfigException {
        config.read((Reader)null, "null");
    }

    @Test(expected = ConfigException.class)
    public void testReadNonexistantFile() throws IOException, ConfigException {
        File f = new File(baseDir, "I-do-not-exist");
        config.read(f);
    }

    @Test(expected = ConfigException.class)
    public void testNonExistantInclude() throws IOException, ConfigException {
        read(NONEXISTANT_INCLUDE);
    }

    @Test(expected = ConfigException.class)
    public void testEmptyInclude() throws IOException, ConfigException {
        read(EMPTY_INCLUDE);
    }

    @Test
    public void testMalformedConfig() throws IOException, ConfigException {
        try {
            read(MALFORMED_1);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("Empty value"));
        }
        try {
            read(MALFORMED_2);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("Empty key"));
        }
        try {
            read(MALFORMED_3);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("Unrecognised config option"));
        }
        try {
            read(MALFORMED_4);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("Unrecognised config option"));
        }
        try {
            read(MALFORMED_5);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("Empty key"));
        }
        try {
            read(MALFORMED_6);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("Empty value"));
        }
    }

    @Test
    public void testWrite() throws IOException {
        config.setValue("key", "value");
        config.setIntValue("int", 2);
        config.setFloatValue("float", -9.1);
        config.setBooleanValue("boolean", false);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        config.write(pw);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("key : value" + System.getProperty("line.separator")));
        assertTrue(result.contains("int : 2" + System.getProperty("line.separator")));
        assertTrue(result.contains("float : -9.1" + System.getProperty("line.separator")));
        assertTrue(result.contains("boolean : false" + System.getProperty("line.separator")));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testWriteNullWriter() throws IOException {
        config.write(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetNullKey() throws IOException, ConfigException {
        config.getValue(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetNullIntKey() throws IOException, ConfigException {
        config.getIntValue(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetNullFloatKey() throws IOException, ConfigException {
        config.getFloatValue(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetNullBooleanKey() throws IOException, ConfigException {
        config.getBooleanValue(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNullKey() throws IOException, ConfigException {
        config.setValue(null, "value");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNullIntKey() throws IOException, ConfigException {
        config.setIntValue(null, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNullFloatKey() throws IOException, ConfigException {
        config.setFloatValue(null, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNullBooleanKey() throws IOException, ConfigException {
        config.setBooleanValue(null, false);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRemoveNullKey() throws IOException, ConfigException {
        config.removeKey(null);
    }

    @Test
    public void testSetNullValue() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        config.setValue("key", null);
        assertEquals(3, config.getAllKeys().size());
        assertFalse(config.getAllKeys().contains("key"));
    }

    @Test(expected=NoSuchConfigOptionException.class)
    public void testGetNonexistantKey() throws IOException, ConfigException {
        config.getValue("fake_key");
    }

    @Test
    public void testSetMethodsClearCache() {
        // Set an int value then set it again as a string and check that the int value is not cached
        config.setIntValue("key", 5);
        assertEquals(5, config.getIntValue("key"));
        assertEquals("5", config.getValue("key"));
        config.setValue("key", "banana");
        try {
            config.getIntValue("key");
            fail("Expected a NumberFormatException");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("banana", config.getValue("key"));

        // Same again for float values
        config.removeAllKeys();
        config.setFloatValue("key", 7.3);
        assertEquals(7.3, config.getFloatValue("key"), TOLERANCE);
        assertEquals("7.3", config.getValue("key"));
        config.setValue("key", "orange");
        try {
            config.getFloatValue("key");
            fail("Expected a NumberFormatException");
        }
        catch (NumberFormatException e) {
            // Expected
        }
        assertEquals("orange", config.getValue("key"));

        // And boolean values
        config.removeAllKeys();
        config.setBooleanValue("key", true);
        assertTrue(config.getBooleanValue("key"));
        assertEquals("true", config.getValue("key"));
        config.setValue("key", "apple");
        assertFalse(config.getBooleanValue("key"));
        assertEquals("apple", config.getValue("key"));
    }

    @Test
    public void testRemoveKeyClearsCache() {
        config.setIntValue("int", 5);
        assertEquals(5, config.getIntValue("int"));
        config.setFloatValue("float", 2.5);
        assertEquals(2.5, config.getFloatValue("float"), TOLERANCE);
        config.setBooleanValue("boolean", true);
        assertTrue(config.getBooleanValue("boolean"));
        config.removeKey("int");
        try {
            config.getIntValue("int");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        config.removeKey("float");
        try {
            config.getFloatValue("float");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        config.removeKey("boolean");
        try {
            config.getIntValue("boolean");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
    }

    @Test
    public void testRemoveAllKeysClearsCache() {
        config.setIntValue("int", 5);
        assertEquals(5, config.getIntValue("int"));
        config.setFloatValue("float", 2.5);
        assertEquals(2.5, config.getFloatValue("float"), TOLERANCE);
        config.setBooleanValue("boolean", true);
        assertTrue(config.getBooleanValue("boolean"));
        config.removeAllKeys();
        try {
            config.getIntValue("int");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        try {
            config.getFloatValue("float");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
        try {
            config.getIntValue("boolean");
            fail("Expected NoSuchConfigOptionException");
        }
        catch (NoSuchConfigOptionException e) {
            // Expected
        }
    }

    @Test
    public void testGetValueWithDefault() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals("defaultvalue", config.getValue("newkey", "defaultvalue"));
        assertEquals("value", config.getValue("key", "defaultvalue"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetValueWithNullKeyAndDefault() throws ConfigException {
        config.getValue(null, "defaultvalue");
    }

    @Test
    public void testGetIntValueWithDefault() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(10, config.getIntValue("newkey", 10));
        assertEquals(5, config.getIntValue("int", 10));
        // Look up a real key again to check that cached values are handled correctly
        assertEquals(5, config.getIntValue("int", 10));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIntValueWithNullKeyAndDefault() throws ConfigException {
        config.getIntValue(null, 10);
    }

    @Test
    public void testGetFloatValueWithDefault() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(10.0, config.getFloatValue("newkey", 10.0), TOLERANCE);
        assertEquals(3.4, config.getFloatValue("float", 10.0), TOLERANCE);
        // Look up a real key again to check that cached values are handled correctly
        assertEquals(3.4, config.getFloatValue("float", 10.0), TOLERANCE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetFloatValueWithNullKeyAndDefault() throws ConfigException {
        config.getFloatValue(null, 10.0);
    }

    @Test
    public void testGetBooleanValueWithDefault() throws IOException, ConfigException {
        read(BASIC_CONFIG);
        assertEquals(false, config.getBooleanValue("newkey", false));
        assertEquals(true, config.getBooleanValue("boolean", false));
        config.setBooleanValue("boolean2", false);
        assertEquals(false, config.getBooleanValue("boolean2", true));
        // Look up a real key again to check that cached values are handled correctly
        assertEquals(true, config.getBooleanValue("boolean", false));
        // Cover all versions of 'true'
        config.setValue("boolean3", "t");
        config.setValue("boolean4", "y");
        config.setValue("boolean5", "yes");
        config.setValue("boolean6", "1");
        assertEquals(true, config.getBooleanValue("boolean3", false));
        assertEquals(true, config.getBooleanValue("boolean4", false));
        assertEquals(true, config.getBooleanValue("boolean5", false));
        assertEquals(true, config.getBooleanValue("boolean6", false));
        // Cover a version of false
        config.setValue("boolean7", "not true");
        assertEquals(false, config.getBooleanValue("boolean7", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetBooleanValueWithNullKeyAndDefault() throws ConfigException {
        config.getBooleanValue(null, false);
    }

    @Test
    public void testDollarNotation() throws IOException, ConfigException {
        read(DOLLAR_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("apples", config.getValue("dollar.first"));
        assertEquals("apples", config.getValue("dollar.fourth"));
        assertEquals("oranges and apples", config.getValue("dollar.second"));
        assertEquals("apples and oranges", config.getValue("dollar.third"));
    }

    @Test(expected=NoSuchConfigOptionException.class)
    public void testBrokenDollarNotation() throws IOException, ConfigException {
        read(DOLLAR_CONFIG_BAD);
        assertEquals(2, config.getAllKeys().size());
        assertEquals("bananas", config.getValue("dollar.first"));
        // This should throw an exception
        config.getValue("dollar.second");
    }

    @Test
    public void testDollarNotationSetValue() throws IOException, ConfigException {
        read(DOLLAR_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("apples", config.getValue("dollar.first"));
        assertEquals("oranges and apples", config.getValue("dollar.second"));
        assertEquals("apples and oranges", config.getValue("dollar.third"));
        config.setValue("dollar.first", "pears");
        assertEquals(4, config.getAllKeys().size());
        assertEquals("pears", config.getValue("dollar.first"));
        assertEquals("pears", config.getValue("dollar.fourth"));
        assertEquals("oranges and pears", config.getValue("dollar.second"));
        assertEquals("pears and oranges", config.getValue("dollar.third"));
    }


    @Test
    public void testDollarNotationCache() throws IOException, ConfigException {
        read(DOLLAR_CONFIG);
        assertEquals(4, config.getAllKeys().size());
        assertEquals("apples", config.getValue("dollar.first"));
        assertEquals("apples", config.getValue("dollar.fourth"));
        assertEquals("oranges and apples", config.getValue("dollar.second"));
        assertEquals("apples and oranges", config.getValue("dollar.third"));
        config.setValue("dollar.first", "10");
        assertEquals(10, config.getIntValue("dollar.first"));
        assertEquals(10, config.getIntValue("dollar.fourth"));
        // Now check that dollar.fourth is not cached
        config.setValue("dollar.first", "20");
        assertEquals(20, config.getIntValue("dollar.first"));
        assertEquals(20, config.getIntValue("dollar.fourth"));

        // Same again for floats
        config.setValue("dollar.first", "10.0");
        assertEquals(10.0, config.getFloatValue("dollar.first"), TOLERANCE);
        assertEquals(10.0, config.getFloatValue("dollar.fourth"), TOLERANCE);
        config.setValue("dollar.first", "20.0");
        assertEquals(20.0, config.getFloatValue("dollar.first"), TOLERANCE);
        assertEquals(20.0, config.getFloatValue("dollar.fourth"), TOLERANCE);

        // Again for booleans
        config.setValue("dollar.first", "false");
        assertFalse(config.getBooleanValue("dollar.first"));
        assertFalse(config.getBooleanValue("dollar.fourth"));
        config.setValue("dollar.first", "true");
        assertTrue(config.getBooleanValue("dollar.first"));
        assertTrue(config.getBooleanValue("dollar.fourth"));

        // And again for arrays
        config.setValue("dollar.first", "foo bar");
        assertArrayEquals(new String[] {"foo", "bar"}, config.getArrayValue("dollar.first").toArray());
        assertArrayEquals(new String[] {"foo", "bar"}, config.getArrayValue("dollar.fourth").toArray());
        config.setValue("dollar.first", "foo bar baz");
        assertArrayEquals(new String[] {"foo", "bar", "baz"}, config.getArrayValue("dollar.first").toArray());
        assertArrayEquals(new String[] {"foo", "bar", "baz"}, config.getArrayValue("dollar.fourth").toArray());
    }

    @Test
    public void testDollarNotationTypes() throws IOException, ConfigException {
        read(DOLLAR_CONFIG_2);
        assertEquals(9, config.getAllKeys().size());
        assertEquals(10, config.getIntValue("int.first"));
        assertEquals(10, config.getIntValue("int.second"));
        assertEquals(0.4, config.getFloatValue("float.first"), TOLERANCE);
        assertEquals(0.4, config.getFloatValue("float.second"), TOLERANCE);
        assertFalse(config.getBooleanValue("boolean.first"));
        assertFalse(config.getBooleanValue("boolean.second"));
        assertArrayEquals(new String[] {"foo", "bar"}, config.getArrayValue("array.first").toArray());
        assertArrayEquals(new String[] {"foo", "bar"}, config.getArrayValue("array.second").toArray());
        assertArrayEquals(new String[] {"foo", "bar", "baz"}, config.getArrayValue("array.third").toArray());
    }

    @Test
    public void testWriteWithReferences() throws IOException {
        config.setValue("key", "value");
        config.setValue("duplicate", "${key}");
        assertEquals("value", config.getValue("duplicate"));
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        config.write(pw);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("key : value" + System.getProperty("line.separator")));
        assertTrue(result.contains("duplicate : ${key}" + System.getProperty("line.separator")));
        assertFalse(result.contains("duplicate : value"));
    }

    @Test
    public void testReferenceWithDefault() throws IOException, ConfigException {
        read(DOLLAR_CONFIG_DEFAULT);
        assertEquals(2, config.getAllKeys().size());
        assertEquals("apples", config.getValue("dollar.first"));
        assertEquals("apples and oranges", config.getValue("dollar.second"));
        config.setValue("nonexistant", "pears");
        assertEquals("apples and pears", config.getValue("dollar.second"));
        config.removeKey("nonexistant");
        assertEquals("apples and oranges", config.getValue("dollar.second"));
    }

    private void read(String name) throws IOException, ConfigException {
        config.read(new File(baseDir, name));
    }
}

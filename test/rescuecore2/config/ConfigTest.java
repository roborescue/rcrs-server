package rescuecore2.config;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Set;

public class ConfigTest {
    private Config config;
    private File baseDir;

    private final static String BASIC_CONFIG = "rescuecore2/config/basic.cfg";
    private final static String INCLUDE_CONFIG = "rescuecore2/config/include.cfg";
    private final static String INCLUDE_CONFIG_2 = "rescuecore2/config/include2.cfg";
    private final static String MORE_CONFIG = "rescuecore2/config/more.cfg";
    private final static String WHITESPACE_CONFIG = "rescuecore2/config/whitespace.cfg";
    private final static String CONFIG_DIR = "rescuecore2/config/configdir";
    private final static String CONFIG_TREE = "rescuecore2/config/configtree";
    private final static String CONFIG_INCLUDE_SUBDIR_FILE = "rescuecore2/config/include_subdir_file.cfg";
    private final static String CONFIG_INCLUDE_SUBDIR = "rescuecore2/config/include_subdir.cfg";
    private final static String NONEXISTANT_INCLUDE = "rescuecore2/config/nonexistant_include.cfg";
    private final static String EMPTY_INCLUDE = "rescuecore2/config/empty_include.cfg";
    private final static String MALFORMED_1 = "rescuecore2/config/malformed1.cfg";
    private final static String MALFORMED_2 = "rescuecore2/config/malformed2.cfg";
    private final static String MALFORMED_3 = "rescuecore2/config/malformed3.cfg";
    private final static String MALFORMED_4 = "rescuecore2/config/malformed4.cfg";
    private final static String MALFORMED_5 = "rescuecore2/config/malformed5.cfg";
    private final static String MALFORMED_6 = "rescuecore2/config/malformed6.cfg";
    private final static String BOOLEAN_CONFIG = "rescuecore2/config/boolean.cfg";

    @Before
    public void setup() {
        baseDir = new File(System.getProperty("rescuecore2.tests.basedir"));
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
        assertEquals(5, config.getFloatValue("int"), 0.001);
        assertEquals("5", config.getValue("int"));
        assertEquals(3.4, config.getFloatValue("float"), 0.001);
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
        assertEquals(-9.3, config.getFloatValue("newkey"), 0.001);
        assertEquals("-9.3", config.getValue("newkey"));
        assertEquals(3.4, config.getFloatValue("float"), 0.001);
        assertEquals("3.4", config.getValue("float"));
        config.setFloatValue("float", 127.02);
        assertEquals(5, config.getAllKeys().size());
        assertEquals(-9.3, config.getFloatValue("newkey"), 0.001);
        assertEquals("-9.3", config.getValue("newkey"));
        assertEquals(127.02, config.getFloatValue("float"), 0.001);
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
        config.read(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadNonexistantFile() throws IOException, ConfigException {
        File f = new File(baseDir, "I-do-not-exist");
        config.read(f);
    }

    @Test(expected = IllegalArgumentException.class)
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
            assertTrue(e.getMessage().contains("No value found"));
        }
        try {
            read(MALFORMED_2);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("No key found"));
        }
        try {
            read(MALFORMED_3);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("No ':' found"));
        }
        try {
            read(MALFORMED_4);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("No ':' found"));
        }
        try {
            read(MALFORMED_5);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("No key found"));
        }
        try {
            read(MALFORMED_6);
            fail("Expected a ConfigException");
        }
        catch (ConfigException e) {
            // Expected
            assertTrue(e.getMessage().contains("No value found"));
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
    public void testSetNullKey() throws IOException, ConfigException {
        config.setValue(null, "value");
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
        assertEquals(7.3, config.getFloatValue("key"), 0.001);
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
        assertEquals(2.5, config.getFloatValue("float"), 0.001);
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
        assertEquals(2.5, config.getFloatValue("float"), 0.001);
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

    private void read(String name) throws IOException, ConfigException {
        config.read(new File(baseDir, name));
    }
}
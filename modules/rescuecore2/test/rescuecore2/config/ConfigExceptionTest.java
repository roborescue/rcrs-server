package rescuecore2.config;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ConfigExceptionTest {
    @Test
    public void testFilenameConstructor() {
        ConfigException e = new ConfigException("filename");
        assertEquals("filename: Unknown error", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void testFilenameAndStringConstructor() {
        ConfigException e = new ConfigException("filename", "reason");
        assertEquals("filename: reason", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void testFilenameAndCauseConstructor() {
        Exception cause = new RuntimeException("cause");
        ConfigException e = new ConfigException("filename", cause);
        assertEquals("filename: java.lang.RuntimeException: cause", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    public void testFilenameAndLineNumberConstructor() {
        ConfigException e = new ConfigException("filename", 10);
        assertEquals("filename: Line 10: Unknown error", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void testFilenameLineNumberAndStringConstructor() {
        ConfigException e = new ConfigException("filename", 15, "reason");
        assertEquals("filename: Line 15: reason", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void testFilenameLineNumberAndCauseConstructor() {
        Exception cause = new RuntimeException("cause");
        ConfigException e = new ConfigException("filename", 20, cause);
        assertEquals("filename: Line 20: java.lang.RuntimeException: cause", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    public void testFilenameStringAndCauseConstructor() {
        Exception cause = new RuntimeException("cause");
        ConfigException e = new ConfigException("filename", "reason", cause);
        assertEquals("filename: reason", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    public void testFilenameLineNumberStringAndCauseConstructor() {
        Exception cause = new RuntimeException("cause");
        ConfigException e = new ConfigException("filename", 25, "reason", cause);
        assertEquals("filename: Line 25: reason", e.getMessage());
        assertSame(cause, e.getCause());
    }
}

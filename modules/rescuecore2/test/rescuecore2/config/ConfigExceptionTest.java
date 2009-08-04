package rescuecore2.config;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ConfigExceptionTest {
    @Test
    public void testFilenameConstructor() {
        ConfigException e = new ConfigException("filename");
        assertEquals("filename: unknown error", e.getMessage());
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
    public void testFilenameStringAndCauseConstructor() {
        Exception cause = new RuntimeException("cause");
        ConfigException e = new ConfigException("filename", "reason", cause);
        assertEquals("filename: reason", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
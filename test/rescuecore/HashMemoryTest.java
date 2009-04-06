package rescuecore;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HashMemoryTest {
    private HashMemory memory;

    @Before
    public void setup() {
        memory = new HashMemory();
    }

    @Test
    public void testEmptyInitially() {
        assertEquals(0,memory.getAllObjects().size());
    }
}
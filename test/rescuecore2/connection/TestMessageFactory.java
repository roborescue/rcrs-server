package rescuecore2.connection;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

public class TestMessageFactory implements MessageFactory {
    private String description;
    private int[] ids;

    public TestMessageFactory(String description, int... ids) {
        this.description = description;
        this.ids = ids;
    }

    @Override
    public int[] getKnownMessageTypeIDs() {
        return ids;
    }

    @Override
    public Message createMessage(int id, InputStream in) throws IOException {
        boolean found = false;
        for (int i = 0; i < ids.length && !found; ++i) {
            if (ids[i] == id) {
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        Message result = new TestMessage(id, description);
        result.read(in);
        return result;
    }
}
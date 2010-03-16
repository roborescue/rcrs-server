package rescuecore2.connection;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.registry.MessageFactory;

public class TestMessageFactory implements MessageFactory {
    private String description;
    private String[] urns;

    public TestMessageFactory(String description, String... urns) {
        this.description = description;
        this.urns = urns;
    }

    @Override
    public String[] getKnownMessageURNs() {
        return urns;
    }

    @Override
    public Message makeMessage(String urn, InputStream in) throws IOException {
        boolean found = false;
        for (int i = 0; i < urns.length && !found; ++i) {
            if (urns[i].equals(urn)) {
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        Message result = new TestMessage(urn, description);
        result.read(in);
        return result;
    }
}

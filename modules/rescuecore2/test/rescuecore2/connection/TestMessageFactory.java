package rescuecore2.connection;

import java.io.InputStream;
import java.util.Map;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.registry.MessageFactory;

public class TestMessageFactory implements MessageFactory {
    private String description;
    private int[] urns;

    public TestMessageFactory(String description, int... urns) {
        this.description = description;
        this.urns = urns;
    }

    @Override
    public int[] getKnownURNs() {
        return urns;
    }

    @Override
    public Message makeMessage(int urn, InputStream in) throws IOException {
        boolean found = false;
        for (int i = 0; i < urns.length && !found; ++i) {
            if (urns[i]==(urn)) {
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

	@Override
	public Message makeMessage(int urn, MessageProto proto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getV1Equiv(int urnId) {
		return null;
	}
	@Override
	public String getPrettyName(int urn) {
		return null;
	}
}

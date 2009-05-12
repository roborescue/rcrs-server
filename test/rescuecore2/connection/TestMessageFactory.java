package rescuecore2.connection;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

public class TestMessageFactory implements MessageFactory {
    public final static int MESSAGE_1 = 1;
    public final static int MESSAGE_2 = 2;

    @Override
    public Message createMessage(int id, InputStream in) throws IOException {
        Message result = null;
	switch (id) {
	case MESSAGE_1:
	    result = new TestMessage(MESSAGE_1);
            break;
	case MESSAGE_2:
	    result =  new TestMessage(MESSAGE_2);
            break;
        default:
            throw new IllegalArgumentException("Unrecognised ID: " + id);
	}
        if (in != null) {
            result.read(in);
        }
        return result;
    }
}
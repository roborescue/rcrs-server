package rescuecore2.connection;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

public class TestMessageFactory implements MessageFactory {
    public final static int MESSAGE_1 = 1;
    public final static int MESSAGE_2 = 2;

    public Message createMessage(int id) {
	switch (id) {
	case MESSAGE_1:
	    return new TestMessage(MESSAGE_1);
	case MESSAGE_2:
	    return new TestMessage(MESSAGE_2);
	}
	throw new IllegalArgumentException("Unrecognised ID: " + id);
    }
}
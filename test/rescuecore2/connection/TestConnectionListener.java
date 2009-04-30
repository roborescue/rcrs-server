package rescuecore2.connection;

import java.util.List;
import java.util.ArrayList;

import rescuecore2.messages.Message;

public class TestConnectionListener implements ConnectionListener {
    private List<Message> messages;

    public TestConnectionListener() {
	messages = new ArrayList<Message>();
    }

    @Override
    public void messageReceived(Message m) {
	messages.add(m);
    }

    public int getMessageCount() {
	return messages.size();
    }

    public Message getMessage(int index) {
	return messages.get(index);
    }
}
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
    public void messageReceived(Connection c, Message m) {
        synchronized (this) {
            messages.add(m);
            this.notifyAll();
        }
    }

    public int getMessageCount() {
        synchronized (this) {
            return messages.size();
        }
    }

    public Message getMessage(int index) {
        synchronized (this) {
            return messages.get(index);
        }
    }

    /**
       Wait until at least n messages have arrived or timeout ms have elapsed. If the thread is interrupted then this method will return immediately.
       @param n The number of messages to wait for.
       @param timeout The maximum time to wait in ms.
     */
    public void waitForMessages(int n, int timeout) {
        long end = System.currentTimeMillis() + timeout;
        synchronized (this) {
            while (messages.size() < n) {
                long now = System.currentTimeMillis();
                if (now >= end) {
                    return;
                }
                try {
                    this.wait(end - now);
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}

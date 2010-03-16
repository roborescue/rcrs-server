package rescuecore2.connection;

import rescuecore2.messages.Message;
import java.util.Deque;
import java.util.ArrayDeque;

/**
   ConnectionListener implementation that maintains a queue of messages.
 */
public class MessageQueueConnectionListener implements ConnectionListener {
    private Deque<Message> messages;

    /**
       Construct a MessageQueueConnectionListener.
    */
    public MessageQueueConnectionListener() {
        messages = new ArrayDeque<Message>();
    }

    @Override
    public void messageReceived(Connection c, Message msg) {
        synchronized (messages) {
            messages.addLast(msg);
            messages.notifyAll();
        }
    }

    /**
       Wait for a message.
       @return The next message.
       @throws InterruptedException If this thread is interrupted.
    */
    public Message waitForMessage() throws InterruptedException {
        synchronized (messages) {
            while (messages.isEmpty()) {
                messages.wait();
            }
            return messages.removeFirst();
        }
    }

    /**
       Wait for a message.
       @param timeout The maximum amount of time to wait in milliseconds.
       @return The next message or null if the timeout is reached.
       @throws InterruptedException If this thread is interrupted.
    */
    public Message waitForMessage(long timeout) throws InterruptedException {
        synchronized (messages) {
            long end = System.currentTimeMillis() + timeout;
            while (messages.isEmpty()) {
                long now = System.currentTimeMillis();
                if (now >= end) {
                    return null;
                }
                messages.wait(end - now);
            }
            return messages.removeFirst();
        }
    }
}

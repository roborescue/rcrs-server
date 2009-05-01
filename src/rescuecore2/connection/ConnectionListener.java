package rescuecore2.connection;

import rescuecore2.messages.Message;

/**
   Interface for classes interested in hearing about events on Connection objects.
 */
public interface ConnectionListener {
    /**
       Notification that a message was received.
       @param msg The Message that arrived.
     */
    void messageReceived(Message msg);
}
package rescuecore2.connection;

import rescuecore2.messages.Message;

/**
   Interface for classes interested in hearing about events on Connection objects.
 */
public interface ConnectionListener {
    /**
       Notification that a message was received.
       @param c The Connection that the message arrived on.
       @param msg The Message that arrived.
     */
    void messageReceived(Connection c, Message msg);
}

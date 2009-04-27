package rescuecore2.connection;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

import java.io.IOException;
import java.util.Collection;

/**
   Top-level interface for a communication interface between simulator components.
 */
public interface Connection {
    /**
       Send a message across this connection.
       @param msg The message to send.
       @throws IOException If the message cannot be sent.
     */
    void sendMessage(Message msg) throws IOException;

    /**
       Send a set of messages across this connection.
       @param messages The messages to send.
       @throws IOException If any of the messages cannot be sent.
     */
    void sendMessages(Collection<Message> messages) throws IOException;

    /**
       Add a ConnectionListener. This listener will be notified when messages arrive on this connection.
       @param l The listener to add.
     */
    void addConnectionListener(ConnectionListener l);

    /**
       Remove a ConnectionListener.
       @param l The listener to remove.
     */
    void removeConnectionListener(ConnectionListener l);

    /**
       Start this connection up. Connections are not available for use until started.
     */
    void startup();

    /**
       Shut this connection down.
     */
    void shutdown();

    /**
       Set the factory for interpreting messages that come in on this connection.
       @param factory The new factory to use.
    */
    void setMessageFactory(MessageFactory factory);

    /**
       Turn byte-level logging on or off.
       @param enable Whether to enable byte-level logging.
    */
    void setLogBytes(boolean enabled);
}
package rescuecore2.connection;

import rescuecore2.messages.Message;
import rescuecore2.registry.Registry;

import java.util.Collection;

/**
   Top-level interface for a communication interface between simulator components.
 */
public interface Connection {
    /**
       Send a message across this connection.
       @param msg The message to send.
       @throws ConnectionException If the connection has not been started up or has been shut down, or if there is an error sending the message.
     */
    void sendMessage(Message msg) throws ConnectionException;

    /**
       Send a set of messages across this connection.
       @param messages The messages to send.
       @throws ConnectionException If the connection has not been started up or has been shut down, or if there is an error sending the message.
     */
    void sendMessages(Collection<? extends Message> messages) throws ConnectionException;

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
       Find out if this connection is still alive. The connection is alive if it has been started, not stopped, and no fatal errors have occurred.
       @return True if this connection is alive and is still able to send/receive messages, false otherwise.
     */
    boolean isAlive();

    /**
       Shut this connection down.
     */
    void shutdown();

    /**
       Turn byte-level logging on or off.
       @param enabled Whether to enable byte-level logging.
    */
    void setLogBytes(boolean enabled);

    /**
       Set the name for this connection.
       @param name The new name.
    */
    void setName(String name);

    /**
       Get the name for this connection.
       @return The name.
    */
    String getName();

    /**
       Set the Registry that this connection should use for decoding messages and entities.
       @param registry The new Registry to use.
    */
    void setRegistry(Registry registry);

    /**
       Get the Registry that this connection is using for decoding messages and entities.
       @return The Registry in use.
    */
    Registry getRegistry();
}

package rescuecore2.connection;

/**
   Interface for objects interested in hearing about new connections to a ConnectionManager.
 */
public interface ConnectionManagerListener {
    /**
       Notification that a new connection has been made. This connection will be initialised but not started.
       @param c The new connection.
     */
    void newConnection(Connection c);
}

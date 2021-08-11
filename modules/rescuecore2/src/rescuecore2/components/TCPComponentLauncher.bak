package rescuecore2.components;

import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.TCPConnection;
import rescuecore2.connection.ConnectionException;

import java.io.IOException;

/**
   A class that knows how to connect components to the kernel via TCP.
 */
public class TCPComponentLauncher extends ComponentLauncher {
    private String host;
    private int port;

    /**
       Construct a new TCPComponentLauncher.
       @param host The host name.
       @param port The host port.
       @param config The system configuration.
    */
    public TCPComponentLauncher(String host, int port, Config config) {
        super(config);
        this.host = host;
        this.port = port;
    }

    @Override
    protected Connection makeConnection() throws ConnectionException {
        try {
            return new TCPConnection(host, port);
        }
        catch (IOException e) {
            throw new ConnectionException(e);
        }
    }
}

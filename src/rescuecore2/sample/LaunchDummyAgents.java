package rescuecore2.sample;

import java.io.IOException;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.TCPConnection;
import rescuecore2.worldmodel.EntityRegistry;

import rescuecore2.standard.entities.StandardEntityFactory;

/**
   Launcher for dummy agents.
 */
public final class LaunchDummyAgents {
    private static final int KERNEL_PORT = 7000;

    private LaunchDummyAgents() {}

    /**
       Launch 'em!
       @param args The first argument will be interpreted as the number of agents to launch. -1 means launch as many as possible.
     */
    public static void main(String[] args) {
        EntityRegistry.register(StandardEntityFactory.INSTANCE);
        int count = -1; // Launch as many as possible by default
        if (args.length != 0) {
            count = Integer.parseInt(args[0]);
        }
        try {
            Connection c = new TCPConnection("localhost", KERNEL_PORT);
            c.startup();
            connect(c, count);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ConnectionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void connect(Connection c, int count) throws InterruptedException, ConnectionException {
        int i = 1;
        while (count-- != 0) {
            System.out.println("Connecting agent " + i);
            DummyAgent agent = new DummyAgent();
            if (!agent.connect(c, i++)) {
                return;
            }
        }
    }
}
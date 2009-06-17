package rescuecore2.sample;

import java.io.IOException;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.TCPConnection;
import rescuecore2.worldmodel.EntityRegistry;

import rescuecore2.standard.entities.StandardEntityFactory;

/**
   Launcher for sample agents. This will launch as many instances of each of the sample agents as possible, all using one connction.
 */
public final class LaunchSampleAgents {
    private static final int DEFAULT_KERNEL_PORT = 7000;
    private static final String DEFAULT_KERNEL_HOST = "localhost";

    private static final String PORT_FLAG = "-p";
    private static final String HOST_FLAG = "-h";

    private LaunchSampleAgents() {}

    /**
       Launch 'em!
       @param args The following arguments are understood: -p <port>, -h <hostname>.
     */
    public static void main(String[] args) {
        EntityRegistry.register(StandardEntityFactory.INSTANCE);
        int port = DEFAULT_KERNEL_PORT;
        String host = DEFAULT_KERNEL_HOST;
        // CHECKSTYLE:OFF:ModifiedControlVariable
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals(PORT_FLAG)) {
                port = Integer.parseInt(args[++i]);
            }
            else if (args[i].equals(HOST_FLAG)) {
                host = args[++i];
            }
            else {
                System.err.println("Unrecognised option: " + args[i]);
            }
        }
        // CHECKSTYLE:ON:ModifiedControlVariable
        try {
            Connection c = new TCPConnection(host, port);
            c.startup();
            connect(c);
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

    private static void connect(Connection c) throws InterruptedException, ConnectionException {
        int i = 0;
        String reason = null;
        while (reason == null) {
            System.out.print("Connecting fire brigade " + i + "...");
            reason = new SampleFireBrigade().connect(c, i++);
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null) {
            System.out.print("Connecting police force " + i + "...");
            reason = new SamplePoliceForce().connect(c, i++);
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null) {
            System.out.print("Connecting ambulance team " + i + "...");
            reason = new SampleAmbulanceTeam().connect(c, i++);
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null) {
            System.out.print("Connecting centre " + i + "...");
            reason = new SampleCentre().connect(c, i++);
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
    }
}
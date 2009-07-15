package rescuecore2.sample;

import java.io.IOException;

import rescuecore2.components.ComponentLauncher;
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
    private static final String FIRE_BRIGADE_FLAG = "-fb";
    private static final String POLICE_FORCE_FLAG = "-pf";
    private static final String AMBULANCE_TEAM_FLAG = "-at";

    private LaunchSampleAgents() {}

    /**
       Launch 'em!
       @param args The following arguments are understood: -p <port>, -h <hostname>, -fb <fire brigades>, -pf <police forces>, -at <ambulance teams>
     */
    public static void main(String[] args) {
        EntityRegistry.register(StandardEntityFactory.INSTANCE);
        int port = DEFAULT_KERNEL_PORT;
        String host = DEFAULT_KERNEL_HOST;
        int fb = -1;
        int pf = -1;
        int at = -1;
        // CHECKSTYLE:OFF:ModifiedControlVariable
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals(PORT_FLAG)) {
                port = Integer.parseInt(args[++i]);
            }
            else if (args[i].equals(HOST_FLAG)) {
                host = args[++i];
            }
            else if (args[i].equals(FIRE_BRIGADE_FLAG)) {
                fb = Integer.parseInt(args[++i]);
            }
            else if (args[i].equals(POLICE_FORCE_FLAG)) {
                pf = Integer.parseInt(args[++i]);
            }
            else if (args[i].equals(AMBULANCE_TEAM_FLAG)) {
                at = Integer.parseInt(args[++i]);
            }
            else {
                System.err.println("Unrecognised option: " + args[i]);
            }
        }
        // CHECKSTYLE:ON:ModifiedControlVariable
        try {
            Connection c = new TCPConnection(host, port);
            c.startup();
            connect(c, fb, pf, at);
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

    private static void connect(Connection c, int fb, int pf, int at) throws InterruptedException, ConnectionException {
        ComponentLauncher launcher = new ComponentLauncher(c);
        int i = 0;
        String reason = null;
        while (reason == null && fb-- != 0) {
            System.out.print("Connecting fire brigade " + (i++) + "...");
            reason = launcher.connect(new SampleFireBrigade());
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null && pf-- != 0) {
            System.out.print("Connecting police force " + (i++) + "...");
            reason = launcher.connect(new SamplePoliceForce());
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null && at-- != 0) {
            System.out.print("Connecting ambulance team " + (i++) + "...");
            reason = launcher.connect(new SampleAmbulanceTeam());
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null) {
            System.out.print("Connecting centre " + (i++) + "...");
            reason = launcher.connect(new SampleCentre());
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
        reason = null;
        while (reason == null) {
            System.out.print("Connecting dummy agent " + (i++) + "...");
            reason = launcher.connect(new DummyAgent());
            if (reason == null) {
                System.out.println("success");
            }
            else {
                System.out.println("failed: " + reason);
            }
        }
    }
}
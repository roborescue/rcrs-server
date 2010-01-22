package rescuecore2;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.io.IOException;
import javax.swing.JFrame;

import rescuecore2.components.Component;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentInitialisationException;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.TCPConnection;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.registry.Registry;

/**
   General launcher for components.
 */
public final class LaunchComponents {
    private LaunchComponents() {}

    /**
       Launch 'em!
       @param args The arguments should be thus: [-p <port>]? [-h <hostname>]? [-c <config file>]* (fully.qualified.classname[*multiplier])+
     */
    public static void main(String[] args) {
        Config config = new Config();
        try {
            args = CommandLineOptions.processArgs(args, config);
            int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY, Constants.DEFAULT_KERNEL_PORT_NUMBER);
            String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY, Constants.DEFAULT_KERNEL_HOST_NAME);
            processJarFiles(config);
            Connection c = new TCPConnection(host, port);
            c.startup();
            ComponentLauncher launcher = new ComponentLauncher(c, config);
            for (String next : args) {
                connect(launcher, next);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ConfigException e) {
            e.printStackTrace();
        }
        catch (ConnectionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processJarFiles(Config config) throws IOException {
        LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
        processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
        processor.process();
    }

    private static void connect(ComponentLauncher launcher, String argLine) throws InterruptedException, ConnectionException {
        // Check if this class name has a multiplier
        int index = argLine.indexOf("*");
        int count = 1;
        String className = argLine;
        if (index != -1) {
            String mult = argLine.substring(index + 1);
            if ("n".equals(mult)) {
                count = Integer.MAX_VALUE;
            }
            else {
                count = Integer.parseInt(mult);
            }
            className = argLine.substring(0, index);
        }
        System.out.println("Launching " + (count == Integer.MAX_VALUE ? "many" : count) + " instances of component '" + className + "'...");
        for (int i = 0; i < count; ++i) {
            Component c = instantiate(className, Component.class);
            if (c == null) {
                break;
            }
            System.out.println("Launching instance " + (i + 1) + "...");
            try {
                c.initialise();
                launcher.connect(c);
                if (c instanceof GUIComponent) {
                    GUIComponent g = (GUIComponent)c;
                    JFrame frame = new JFrame(g.getGUIComponentName());
                    frame.setContentPane(g.getGUIComponent());
                    frame.pack();
                    frame.setVisible(true);
                }
                System.out.println("success");
            }
            catch (ComponentConnectionException e) {
                System.out.println("failed: " + e.getMessage());
                break;
            }
            catch (ComponentInitialisationException e) {
                System.out.println("failed: " + e);
                e.printStackTrace();
            }
            catch (ConnectionException e) {
                System.out.println("failed: " + e);
                e.printStackTrace();
            }
        }
    }
}
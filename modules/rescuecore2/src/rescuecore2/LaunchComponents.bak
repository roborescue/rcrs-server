package rescuecore2;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.io.IOException;
import javax.swing.JFrame;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.components.Component;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentInitialisationException;
import rescuecore2.connection.ConnectionException;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.registry.Registry;
import rescuecore2.log.Logger;

/**
   General launcher for components.
 */
public final class LaunchComponents {
    private static final String NO_GUI_FLAG = "--nogui";

    private LaunchComponents() {}

    /**
       Launch 'em!
       @param args The arguments should be thus: [-p <port>]? [-h <hostname>]? [-c <config file>]* (fully.qualified.classname[*multiplier])+
     */
    public static void main(String[] args) {
        Logger.setLogContext("launcher");
        Config config = new Config();
        boolean gui = true;
        try {
            args = CommandLineOptions.processArgs(args, config);
            List<String> toLaunch = new ArrayList<String>();
            for (String next : args) {
                if (NO_GUI_FLAG.equals(next)) {
                    gui = false;
                }
                else {
                    toLaunch.add(next);
                }
            }
            int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY, Constants.DEFAULT_KERNEL_PORT_NUMBER);
            String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY, Constants.DEFAULT_KERNEL_HOST_NAME);
            processJarFiles(config);
            ComponentLauncher launcher = new TCPComponentLauncher(host, port, config);
            for (String next : toLaunch) {
                connect(launcher, next, gui);
            }
        }
        catch (IOException e) {
            Logger.error("Error connecting components", e);
        }
        catch (ConfigException e) {
            Logger.error("Configuration error", e);
        }
        catch (ConnectionException e) {
            Logger.error("Error connecting components", e);
        }
        catch (InterruptedException e) {
            Logger.error("Error connecting components", e);
        }
    }

    private static void processJarFiles(Config config) throws IOException {
        LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
        processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
        processor.process();
    }

    private static void connect(ComponentLauncher launcher, String argLine, boolean gui) throws InterruptedException, ConnectionException {
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
        Logger.info("Launching " + (count == Integer.MAX_VALUE ? "many" : count) + " instances of component '" + className + "'...");
        for (int i = 0; i < count; ++i) {
            Component c = instantiate(className, Component.class);
            if (c == null) {
                break;
            }
            Logger.info("Launching instance " + (i + 1) + "...");
            try {
                c.initialise();
                launcher.connect(c);
                if (gui && c instanceof GUIComponent) {
                    GUIComponent g = (GUIComponent)c;
                    JFrame frame = new JFrame(g.getGUIComponentName());
                    frame.setContentPane(g.getGUIComponent());
                    frame.pack();
                    frame.setVisible(true);
                }
                Logger.info("success");
            }
            catch (ComponentConnectionException e) {
                Logger.info("failed: " + e.getMessage());
                break;
            }
            catch (ComponentInitialisationException e) {
                Logger.info("failed: " + e);
            }
            catch (ConnectionException e) {
                Logger.info("failed: " + e);
                break;
            }
        }
    }
}

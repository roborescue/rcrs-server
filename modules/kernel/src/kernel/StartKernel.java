package kernel;

import static rescuecore2.misc.java.JavaTools.instantiate;
import static rescuecore2.misc.java.JavaTools.instantiateFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.concurrent.CountDownLatch;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.StreamConnection;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.Component;
import rescuecore2.components.ComponentInitialisationException;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.Agent;
import rescuecore2.components.Simulator;
import rescuecore2.components.Viewer;
import rescuecore2.messages.MessageRegistry;
import rescuecore2.messages.MessageFactory;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityRegistry;
import rescuecore2.worldmodel.EntityFactory;
import rescuecore2.misc.Pair;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.misc.java.LoadableType;
import rescuecore2.Constants;

import kernel.ui.KernelGUI;
import kernel.ui.KernelGUIComponent;

/**
   A class for launching the kernel.
 */
public final class StartKernel {
    private static final String CONFIG_FLAG = "-c";
    private static final String CONFIG_LONG_FLAG = "--config";
    private static final String NO_GUI = "--nogui";
    private static final String JUST_RUN = "--just-run";

    private static final String GIS_MANIFEST_KEY = "Gis";
    private static final String PERCEPTION_MANIFEST_KEY = "Perception";
    private static final String COMMUNICATION_MANIFEST_KEY = "CommunicationModel";

    private static final String GIS_KEY = "kernel.gis";
    private static final String PERCEPTION_KEY = "kernel.perception";
    private static final String COMMUNICATION_KEY = "kernel.communication";
    private static final String AGENT_KEY = "kernel.agents";
    private static final String SIMULATOR_KEY = "kernel.simulators";
    private static final String VIEWER_KEY = "kernel.viewers";

    private static final String GIS_REGEX = "(.+WorldModelCreator).class";
    private static final String PERCEPTION_REGEX = "(.+Perception).class";
    private static final String COMMUNICATION_REGEX = "(.+CommunicationModel).class";

    private static final LoadableType GIS_LOADABLE_TYPE = new LoadableType(GIS_MANIFEST_KEY, GIS_REGEX, WorldModelCreator.class);
    private static final LoadableType PERCEPTION_LOADABLE_TYPE = new LoadableType(PERCEPTION_MANIFEST_KEY, PERCEPTION_REGEX, Perception.class);
    private static final LoadableType COMMUNICATION_LOADABLE_TYPE = new LoadableType(COMMUNICATION_MANIFEST_KEY, COMMUNICATION_REGEX, CommunicationModel.class);

    private static final String AUTOSTART_SUFFIX = ".auto";

    private static final String KERNEL_PORT_KEY = "kernel.io.port";
    private static final String KERNEL_STARTUP_TIME_KEY = "kernel.startup.connect-time";

    private static final String COMMAND_FILTERS_KEY = "kernel.commandfilters";
    private static final String AGENT_REGISTRAR_KEY = "kernel.agents.registrar";
    private static final String GUI_COMPONENTS_KEY = "kernel.ui.components";

    /** Utility class: private constructor. */
    private StartKernel() {}

    /**
       Start a kernel.
       @param args Command line arguments.
    */
    public static void main(String[] args) {
        Config config = new Config();
        boolean showGUI = true;
        boolean justRun = false;
        try {
            int i = 0;
            while (i < args.length) {
                if (args[i].equalsIgnoreCase(CONFIG_FLAG) || args[i].equalsIgnoreCase(CONFIG_LONG_FLAG)) {
                    config.read(new File(args[++i]));
                }
                else if (args[i].equalsIgnoreCase(NO_GUI)) {
                    showGUI = false;
                }
                else if (args[i].equalsIgnoreCase(JUST_RUN)) {
                    justRun = true;
                }
                else if (args[i].startsWith("--") && args[i].indexOf("=") != -1) {
                    int index = args[i].indexOf("=");
                    String key = args[i].substring(2, index);
                    String value = args[i].substring(index + 1);
                    config.setValue(key, value);
                }
                else {
                    System.out.println("Unrecognised option: " + args[i]);
                }
                ++i;
            }
            // Process jar files
            processJarFiles(config.getValue(Constants.JAR_DIR_KEY, Constants.DEFAULT_JAR_DIR), config);
            // Register messages and entities
            for (String next : config.getArrayValue(Constants.MESSAGE_FACTORY_KEY)) {
                MessageFactory factory = instantiateFactory(next, MessageFactory.class);
                if (factory != null) {
                    MessageRegistry.register(factory);
                    System.out.println("Registered message factory: " + next);
                }
            }
            for (String next : config.getArrayValue(Constants.ENTITY_FACTORY_KEY)) {
                EntityFactory factory = instantiateFactory(next, EntityFactory.class);
                if (factory != null) {
                    EntityRegistry.register(factory);
                    System.out.println("Registered entity factory: " + next);
                }
            }
            final KernelInfo kernelInfo = createKernel(config);
            autostartComponents(kernelInfo, config);
            if (showGUI) {
                KernelGUI gui = new KernelGUI(kernelInfo.kernel, kernelInfo.componentManager, config, !justRun);
                for (KernelGUIComponent next : kernelInfo.guiComponents) {
                    gui.addKernelGUIComponent(next);
                }
                JFrame frame = new JFrame("Kernel GUI");
                frame.getContentPane().add(gui);
                frame.pack();
                frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            kernelInfo.kernel.shutdown();
                            System.exit(0);
                        }
                    });
                frame.setVisible(true);
            }
            initialiseKernel(kernelInfo, config);
            if (!showGUI || justRun) {
                waitForComponentManager(kernelInfo, config);
                int maxTime = config.getIntValue(Kernel.TIMESTEPS_KEY);
                while (kernelInfo.kernel.getTime() < maxTime) {
                    kernelInfo.kernel.timestep();
                }
                kernelInfo.kernel.shutdown();
            }
        }
        catch (ConfigException e) {
            System.err.println("Couldn't start kernel");
            e.printStackTrace();
        }
        catch (KernelException e) {
            System.err.println("Couldn't start kernel");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.err.println("Couldn't start kernel");
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void initialiseKernel(KernelInfo kernel, Config config) throws KernelException {
        // Start the connection manager
        ConnectionManager connectionManager = new ConnectionManager();
        try {
            connectionManager.listen(config.getIntValue(KERNEL_PORT_KEY), kernel.componentManager);
        }
        catch (IOException e) {
            throw new KernelException("Couldn't open kernel port", e);
        }
    }

    private static void waitForComponentManager(final KernelInfo kernel, Config config) throws KernelException {
        // Wait for all connections
        // Set up a CountDownLatch
        final CountDownLatch latch = new CountDownLatch(1);
        final long timeout = config.getIntValue(KERNEL_STARTUP_TIME_KEY, -1);
        Thread timeoutThread = null;
        if (timeout != -1) {
            timeoutThread = new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(timeout);
                            latch.countDown();
                        }
                        // CHECKSTYLE:OFF:EmptyStatement is OK here
                        catch (InterruptedException e) {
                            // Ignore
                        }
                        // CHECKSTYLE:ON:EmptyStatement
                    }
                };
        }
        Thread waitThread = new Thread() {
                public void run() {
                    try {
                        kernel.componentManager.waitForAllAgents();
                        kernel.componentManager.waitForAllSimulators();
                        kernel.componentManager.waitForAllViewers();
                    }
                    // CHECKSTYLE:OFF:EmptyStatement is OK here
                    catch (InterruptedException e) {
                        // Ignore
                    }
                    // CHECKSTYLE:ON:EmptyStatement
                    latch.countDown();
                }
            };
        waitThread.start();
        if (timeoutThread != null) {
            timeoutThread.start();
        }
        // Wait at the latch until either everything is connected or the connection timeout expires
        System.out.println("Waiting for all agents, simulators and viewers to connect.");
        if (timeout > -1) {
            System.out.println("Connection timeout is " + timeout + "ms");
        }
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            waitThread.interrupt();
            if (timeoutThread != null) {
                timeoutThread.interrupt();
            }
            throw new KernelException("Interrupted");
        }
    }

    private static void autostartComponents(KernelInfo info, Config config) throws InterruptedException {
        Pair<Connection, Connection> connections = StreamConnection.createConnectionPair();
        info.componentManager.newConnection(connections.first());
        ComponentLauncher launcher = new ComponentLauncher(connections.second());
        // Simulators
        try {
            autostartComponents(Simulator.class, SIMULATOR_KEY, config, launcher);
        }
        catch (NoSuchConfigOptionException e) {
            // Ignore
            System.out.println("Not starting any simulators");
        }
        // Viewers
        try {
            autostartComponents(Viewer.class, VIEWER_KEY, config, launcher);
        }
        catch (NoSuchConfigOptionException e) {
            // Ignore
            System.out.println("Not starting any viewers");
        }
        // Agents
        try {
            autostartComponents(Agent.class, AGENT_KEY, config, launcher);
        }
        catch (NoSuchConfigOptionException e) {
            // Ignore
            System.out.println("Not starting any agents");
        }
    }

    private static <T extends Component> void autostartComponents(Class<T> clazz, String key, Config config, ComponentLauncher launcher) throws InterruptedException {
        for (String next : config.getArrayValue(key + AUTOSTART_SUFFIX)) {
            // Check if this class name has a multiplier
            int index = next.indexOf("*");
            int count = 1;
            String className = next;
            if (index != -1) {
                count = Integer.parseInt(next.substring(index + 1));
                className = next.substring(0, index);
            }
            System.out.println("Launching " + count + " instances of component '" + className + "'...");
            for (int i = 0; i < count; ++i) {
                Component c = instantiate(className, clazz);
                if (c == null) {
                    break;
                }
                System.out.println("Launching instance " + (i + 1) + "...");
                try {
                    c.initialise(config);
                    launcher.connect(c);
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

    private static KernelInfo createKernel(Config config) throws KernelException {
        // Show the chooser GUI
        List<WorldModelCreator> gisChoices = createChoices(config, GIS_KEY, WorldModelCreator.class);
        List<Perception> perceptionChoices = createChoices(config, PERCEPTION_KEY, Perception.class);
        List<CommunicationModel> commsChoices = createChoices(config, COMMUNICATION_KEY, CommunicationModel.class);
        KernelChooserDialog dialog = new KernelChooserDialog(gisChoices.toArray(new WorldModelCreator[0]), perceptionChoices.toArray(new Perception[0]), commsChoices.toArray(new CommunicationModel[0]));
        if (gisChoices.size() > 1 || perceptionChoices.size() > 1 || commsChoices.size() > 1) {
            dialog.setVisible(true);
        }
        WorldModelCreator gis = dialog.getWorldModelCreator();
        Perception perception = dialog.getPerception();
        CommunicationModel comms = dialog.getCommunicationModel();
        CommandFilter filter = makeCommandFilter(config);
        // Get the world model
        WorldModel<? extends Entity> worldModel = gis.buildWorldModel(config);
        // Initialise
        perception.initialise(config, worldModel);
        comms.initialise(config, worldModel);
        // Create the kernel
        Kernel kernel = new Kernel(config, perception, comms, worldModel, filter);
        // Create the component manager
        ComponentManager componentManager = new ComponentManager(kernel, worldModel, config);
        registerInitialAgents(config, componentManager, worldModel);
        return new KernelInfo(kernel, perception, comms, componentManager, makeKernelGUIComponents(config));
    }

    private static void registerInitialAgents(Config config, ComponentManager c, WorldModel<? extends Entity> model) throws KernelException {
        AgentRegistrar ar = instantiate(config.getValue(AGENT_REGISTRAR_KEY), AgentRegistrar.class);
        if (ar == null) {
            throw new KernelException("Couldn't instantiate agent registrar");
        }
        ar.registerAgents(model, config, c);
    }

    private static <T> List<T> createChoices(Config config, String key, Class<T> expectedClass) {
        List<T> instances = new ArrayList<T>();
        String auto = config.getValue(key + AUTOSTART_SUFFIX, null);
        if (auto != null) {
            System.out.println("Attempting to auto-start " + key + ": '" + auto + "'");
            T t = instantiate(auto, expectedClass);
            if (t != null) {
                instances.add(t);
                return instances;
            }
            System.out.println("Auto-start '" + auto + "' failed. Falling back to option list.");
        }
        System.out.println("Loading options: " + key);
        List<String> classNames = config.getArrayValue(key);
        for (String next : classNames) {
            System.out.println("Option found: '" + next + "'");
            T t = instantiate(next, expectedClass);
            if (t != null) {
                instances.add(t);
            }
        }
        return instances;
    }

    private static CommandFilter makeCommandFilter(Config config) {
        ChainedCommandFilter result = new ChainedCommandFilter();
        List<String> classNames = config.getArrayValue(COMMAND_FILTERS_KEY);
        for (String next : classNames) {
            System.out.println("Command filter found: '" + next + "'");
            CommandFilter f = instantiate(next, CommandFilter.class);
            if (f != null) {
                result.addFilter(f);
            }
        }
        return result;
    }

    private static List<KernelGUIComponent> makeKernelGUIComponents(Config config) {
        List<KernelGUIComponent> result = new ArrayList<KernelGUIComponent>();
        try {
            List<String> classNames = config.getArrayValue(GUI_COMPONENTS_KEY);
            for (String next : classNames) {
                System.out.println("GUI component found: '" + next + "'");
                KernelGUIComponent c = instantiate(next, KernelGUIComponent.class);
                if (c != null) {
                    result.add(c);
                }
            }
        }
        catch (NoSuchConfigOptionException e) {
            System.out.println("No extra GUI components found");
        }
        return result;
    }

    private static void processJarFiles(String base, Config config) throws IOException {
        LoadableTypeProcessor processor = new LoadableTypeProcessor();
        processor.setDeepInspection(config.getBooleanValue(Constants.DEEP_JAR_INSPECTION_KEY, true));
        processor.addConfigUpdater(LoadableType.MESSAGE_FACTORY, config, Constants.MESSAGE_FACTORY_KEY);
        processor.addConfigUpdater(LoadableType.ENTITY_FACTORY, config, Constants.ENTITY_FACTORY_KEY);
        processor.addConfigUpdater(LoadableType.AGENT, config, AGENT_KEY);
        processor.addConfigUpdater(LoadableType.SIMULATOR, config, SIMULATOR_KEY);
        processor.addConfigUpdater(LoadableType.VIEWER, config, VIEWER_KEY);
        processor.addConfigUpdater(GIS_LOADABLE_TYPE, config, GIS_KEY);
        processor.addConfigUpdater(PERCEPTION_LOADABLE_TYPE, config, PERCEPTION_KEY);
        processor.addConfigUpdater(COMMUNICATION_LOADABLE_TYPE, config, COMMUNICATION_KEY);
        processor.processJarDirectory(base, LoadableType.MESSAGE_FACTORY, LoadableType.ENTITY_FACTORY, LoadableType.AGENT, LoadableType.SIMULATOR, LoadableType.VIEWER, GIS_LOADABLE_TYPE, PERCEPTION_LOADABLE_TYPE, COMMUNICATION_LOADABLE_TYPE);
    }

    private static class KernelChooserDialog extends JDialog {
        private JComboBox gisChooser;
        private JComboBox perceptionChooser;
        private JComboBox commsChooser;

        public KernelChooserDialog(WorldModelCreator[] gisChoices, Perception[] perceptionChoices, CommunicationModel[] commsChoices) {
            super((Frame)null, "Choose kernel options");
            gisChooser = new JComboBox(gisChoices);
            perceptionChooser = new JComboBox(perceptionChoices);
            commsChooser = new JComboBox(commsChoices);
            // CHECKSTYLE:OFF:MagicNumber
            JPanel main = new JPanel(new GridLayout(3, 2));
            // CHECKSTYLE:ON:MagicNumber
            main.add(new JLabel("GIS: "));
            main.add(gisChooser);
            main.add(new JLabel("Perception: "));
            main.add(perceptionChooser);
            main.add(new JLabel("Communication model: "));
            main.add(commsChooser);
            JButton ok = new JButton("OK");
            add(main, BorderLayout.CENTER);
            add(ok, BorderLayout.SOUTH);
            ok.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                });
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            pack();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            gisChooser.setEnabled(gisChoices.length > 1);
            perceptionChooser.setEnabled(perceptionChoices.length > 1);
            commsChooser.setEnabled(commsChoices.length > 1);
        }

        public WorldModelCreator getWorldModelCreator() {
            return (WorldModelCreator)gisChooser.getSelectedItem();
        }

        public Perception getPerception() {
            return (Perception)perceptionChooser.getSelectedItem();
        }

        public CommunicationModel getCommunicationModel() {
            return (CommunicationModel)commsChooser.getSelectedItem();
        }
    }

    private static class KernelInfo {
        Kernel kernel;
        ComponentManager componentManager;
        List<KernelGUIComponent> guiComponents;

        public KernelInfo(Kernel kernel, Perception perception, CommunicationModel comms, ComponentManager componentManager, List<KernelGUIComponent> otherComponents) {
            this.kernel = kernel;
            this.componentManager = componentManager;
            guiComponents = new ArrayList<KernelGUIComponent>(otherComponents);
            if (perception instanceof KernelGUIComponent) {
                guiComponents.add((KernelGUIComponent)perception);
            }
            if (comms instanceof KernelGUIComponent) {
                guiComponents.add((KernelGUIComponent)comms);
            }
        }
    }
}
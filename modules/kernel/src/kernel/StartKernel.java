package kernel;

import static rescuecore2.misc.java.JavaTools.instantiate;
import static rescuecore2.misc.java.JavaTools.instantiateFactory;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.WindowConstants;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.StreamConnection;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.config.IntegerConstrainedConfigValue;
import rescuecore2.config.ClassNameSetConstrainedConfigValue;
import rescuecore2.config.ClassNameConstrainedConfigValue;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.Component;
import rescuecore2.components.ComponentInitialisationException;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.registry.Registry;
import rescuecore2.registry.MessageFactory;
import rescuecore2.registry.EntityFactory;
import rescuecore2.registry.PropertyFactory;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.misc.Pair;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.misc.java.LoadableType;
import rescuecore2.Constants;
import rescuecore2.GUIComponent;
import rescuecore2.log.LogException;
import rescuecore2.score.ScoreFunction;

import kernel.ui.KernelGUI;
import kernel.ui.ScoreTable;
import kernel.ui.ScoreGraph;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A class for launching the kernel.
 */
public final class StartKernel {
    private static final String NO_GUI = "--nogui";
    private static final String JUST_RUN = "--just-run";

    private static final String GIS_MANIFEST_KEY = "Gis";
    private static final String PERCEPTION_MANIFEST_KEY = "Perception";
    private static final String COMMUNICATION_MANIFEST_KEY = "CommunicationModel";

    private static final String COMMAND_COLLECTOR_KEY = "kernel.commandcollectors";

    private static final String TERMINATION_KEY = "kernel.termination";

    private static final String GIS_REGEX = "(.+WorldModelCreator).class";
    private static final String PERCEPTION_REGEX = "(.+Perception).class";
    private static final String COMMUNICATION_REGEX = "(.+CommunicationModel).class";

    private static final LoadableType GIS_LOADABLE_TYPE = new LoadableType(GIS_MANIFEST_KEY, GIS_REGEX, WorldModelCreator.class);
    private static final LoadableType PERCEPTION_LOADABLE_TYPE = new LoadableType(PERCEPTION_MANIFEST_KEY, PERCEPTION_REGEX, Perception.class);
    private static final LoadableType COMMUNICATION_LOADABLE_TYPE = new LoadableType(COMMUNICATION_MANIFEST_KEY, COMMUNICATION_REGEX, CommunicationModel.class);

    private static final String KERNEL_STARTUP_TIME_KEY = "kernel.startup.connect-time";

    private static final String COMMAND_FILTERS_KEY = "kernel.commandfilters";
    private static final String AGENT_REGISTRAR_KEY = "kernel.agents.registrar";
    private static final String GUI_COMPONENTS_KEY = "kernel.ui.components";

    private static final Log LOG = LogFactory.getLog(StartKernel.class);

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
        // CHECKSTYLE:OFF:MagicNumber
        config.addConstraint(new IntegerConstrainedConfigValue(Constants.KERNEL_PORT_NUMBER_KEY, 1, 65535));
        // CHECKSTYLE:ON:MagicNumber
        config.addConstraint(new IntegerConstrainedConfigValue(KERNEL_STARTUP_TIME_KEY, 0, Integer.MAX_VALUE));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(Constants.MESSAGE_FACTORY_KEY, MessageFactory.class));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(Constants.ENTITY_FACTORY_KEY, EntityFactory.class));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(Constants.PROPERTY_FACTORY_KEY, PropertyFactory.class));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(COMMAND_FILTERS_KEY, CommandFilter.class));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(TERMINATION_KEY, TerminationCondition.class));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(COMMAND_COLLECTOR_KEY, CommandCollector.class));
        config.addConstraint(new ClassNameSetConstrainedConfigValue(GUI_COMPONENTS_KEY, GUIComponent.class));
        config.addConstraint(new ClassNameConstrainedConfigValue(AGENT_REGISTRAR_KEY, AgentRegistrar.class));
        config.addConstraint(new ClassNameConstrainedConfigValue(Constants.SCORE_FUNCTION_KEY, ScoreFunction.class));
        try {
            args = CommandLineOptions.processArgs(args, config);
            int i = 0;
            for (String arg : args) {
                if (arg.equalsIgnoreCase(NO_GUI)) {
                    showGUI = false;
                }
                else if (arg.equalsIgnoreCase(JUST_RUN)) {
                    justRun = true;
                }
                else {
                    LOG.warn("Unrecognised option: " + arg);
                }
            }
            // Process jar files
            processJarFiles(config);
            Registry localRegistry = new Registry("Kernel local registry");
            // Register preferred message, entity and property factories
            for (String next : config.getArrayValue(Constants.MESSAGE_FACTORY_KEY, null)) {
                MessageFactory factory = instantiateFactory(next, MessageFactory.class);
                if (factory != null) {
                    localRegistry.registerMessageFactory(factory);
                    LOG.info("Registered local message factory: " + next);
                }
            }
            for (String next : config.getArrayValue(Constants.ENTITY_FACTORY_KEY, null)) {
                EntityFactory factory = instantiateFactory(next, EntityFactory.class);
                if (factory != null) {
                    localRegistry.registerEntityFactory(factory);
                    LOG.info("Registered local entity factory: " + next);
                }
            }
            for (String next : config.getArrayValue(Constants.PROPERTY_FACTORY_KEY, null)) {
                PropertyFactory factory = instantiateFactory(next, PropertyFactory.class);
                if (factory != null) {
                    localRegistry.registerPropertyFactory(factory);
                    LOG.info("Registered local property factory: " + next);
                }
            }
            final KernelInfo kernelInfo = createKernel(config);
            KernelGUI gui = null;
            if (showGUI) {
                gui = new KernelGUI(kernelInfo.kernel, kernelInfo.componentManager, config, !justRun, localRegistry);
                for (GUIComponent next : kernelInfo.guiComponents) {
                    gui.addGUIComponent(next);
                    if (next instanceof KernelListener) {
                        kernelInfo.kernel.addKernelListener((KernelListener)next);
                    }
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
            initialiseKernel(kernelInfo, config, localRegistry);
            autostartComponents(kernelInfo, localRegistry, gui);
            if (!showGUI || justRun) {
                waitForComponentManager(kernelInfo, config);
                Kernel kernel = kernelInfo.kernel;
                while (!kernel.hasTerminated()) {
                    kernel.timestep();
                }
                kernel.shutdown();
            }
        }
        catch (ConfigException e) {
            LOG.fatal("Couldn't start kernel", e);
        }
        catch (KernelException e) {
            LOG.fatal("Couldn't start kernel", e);
        }
        catch (IOException e) {
            LOG.fatal("Couldn't start kernel", e);
        }
        catch (LogException e) {
            LOG.fatal("Couldn't write log", e);
        }
        catch (InterruptedException e) {
            LOG.fatal("Kernel interrupted");
        }
    }

    private static void initialiseKernel(KernelInfo kernel, Config config, Registry registry) throws KernelException {
        // Start the connection manager
        ConnectionManager connectionManager = new ConnectionManager();
        try {
            connectionManager.listen(config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY), registry, kernel.componentManager);
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
        if (timeout > 0) {
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
        LOG.info("Waiting for all agents, simulators and viewers to connect.");
        if (timeout > -1) {
            LOG.info("Connection timeout is " + timeout + "ms");
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

    private static void autostartComponents(KernelInfo info, Registry registry, KernelGUI gui) throws InterruptedException {
        KernelChooserDialog chooser = info.choices;
        Collection<Callable<Void>> all = new ArrayList<Callable<Void>>();
        for (Pair<String, Integer> next : chooser.getAllComponents()) {
            all.add(new ComponentStarter(next.first(), info.componentManager, next.second(), registry, gui));
        }
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        service.invokeAll(all);
    }

    private static KernelInfo createKernel(Config config) throws KernelException {
        // Show the chooser GUI
        KernelChooserDialog dialog = new KernelChooserDialog(config);
        dialog.setVisible(true);
        WorldModelCreator gis = dialog.getWorldModelCreator();
        Perception perception = dialog.getPerception();
        CommunicationModel comms = dialog.getCommunicationModel();
        CommandFilter filter = makeCommandFilter(config);
        TerminationCondition termination = makeTerminationCondition(config);
        ScoreFunction score = makeScoreFunction(config);
        CommandCollector collector = makeCommandCollector(config);

        // Get the world model
        WorldModel<? extends Entity> worldModel = gis.buildWorldModel(config);
        // Initialise
        perception.initialise(config, worldModel);
        comms.initialise(config, worldModel);
        termination.initialise(config);
        // Create the kernel
        ScoreGraph graph = new ScoreGraph(score);
        Kernel kernel = new Kernel(config, perception, comms, worldModel, gis, filter, termination, graph, collector);
        // Create the component manager
        ComponentManager componentManager = new ComponentManager(kernel, worldModel, config);
        registerInitialAgents(config, componentManager, worldModel);
        KernelInfo result = new KernelInfo(kernel, perception, comms, componentManager, makeGUIComponents(config, perception, comms, termination, filter, graph, collector, score), dialog);
        return result;
    }

    private static void registerInitialAgents(Config config, ComponentManager c, WorldModel<? extends Entity> model) throws KernelException {
        AgentRegistrar ar = instantiate(config.getValue(AGENT_REGISTRAR_KEY), AgentRegistrar.class);
        if (ar == null) {
            throw new KernelException("Couldn't instantiate agent registrar");
        }
        ar.registerAgents(model, config, c);
    }

    private static CommandFilter makeCommandFilter(Config config) {
        ChainedCommandFilter result = new ChainedCommandFilter();
        List<String> classNames = config.getArrayValue(COMMAND_FILTERS_KEY, null);
        for (String next : classNames) {
            LOG.debug("Command filter found: '" + next + "'");
            CommandFilter f = instantiate(next, CommandFilter.class);
            if (f != null) {
                result.addFilter(f);
            }
        }
        return result;
    }

    private static TerminationCondition makeTerminationCondition(Config config) {
        List<TerminationCondition> result = new ArrayList<TerminationCondition>();
        for (String next : config.getArrayValue(TERMINATION_KEY, null)) {
            TerminationCondition t = instantiate(next, TerminationCondition.class);
            if (t != null) {
                result.add(t);
            }
        }
        return new OrTerminationCondition(result);
    }

    private static ScoreFunction makeScoreFunction(Config config) {
        String className = config.getValue(Constants.SCORE_FUNCTION_KEY);
        ScoreFunction result = instantiate(className, ScoreFunction.class);
        return new ScoreTable(result);
    }

    private static CommandCollector makeCommandCollector(Config config) {
        List<String> classNames = config.getArrayValue(COMMAND_COLLECTOR_KEY);
        CompositeCommandCollector result = new CompositeCommandCollector();
        for (String next : classNames) {
            CommandCollector c = instantiate(next, CommandCollector.class);
            if (c != null) {
                result.addCommandCollector(c);
            }
        }
        return result;
    }

    private static List<GUIComponent> makeGUIComponents(Config config, Object... objectsToTest) {
        List<GUIComponent> result = new ArrayList<GUIComponent>();
        List<String> classNames = config.getArrayValue(GUI_COMPONENTS_KEY, null);
        for (String next : classNames) {
            LOG.debug("GUI component found: '" + next + "'");
            GUIComponent c = instantiate(next, GUIComponent.class);
            if (c != null) {
                result.add(c);
            }
        }
        for (Object next : objectsToTest) {
            if (next instanceof GUIComponent) {
                result.add((GUIComponent)next);
            }
        }
        return result;
    }

    private static void processJarFiles(Config config) throws IOException {
        LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
        processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
        processor.addConfigUpdater(LoadableType.AGENT, config, KernelConstants.AGENTS_KEY);
        processor.addConfigUpdater(LoadableType.SIMULATOR, config, KernelConstants.SIMULATORS_KEY);
        processor.addConfigUpdater(LoadableType.VIEWER, config, KernelConstants.VIEWERS_KEY);
        processor.addConfigUpdater(LoadableType.COMPONENT, config, KernelConstants.COMPONENTS_KEY);
        processor.addConfigUpdater(GIS_LOADABLE_TYPE, config, KernelConstants.GIS_KEY);
        processor.addConfigUpdater(PERCEPTION_LOADABLE_TYPE, config, KernelConstants.PERCEPTION_KEY);
        processor.addConfigUpdater(COMMUNICATION_LOADABLE_TYPE, config, KernelConstants.COMMUNICATION_MODEL_KEY);
        LOG.info("Looking for gis, perception, communication, agent, simulator and viewer implementations");
        processor.process();
    }

    private static class ComponentStarter implements Callable<Void> {
        private String className;
        private ComponentManager componentManager;
        private int count;
        private Registry registry;
        private KernelGUI gui;

        public ComponentStarter(String className, ComponentManager componentManager, int count, Registry registry, KernelGUI gui) {
            this.className = className;
            this.componentManager = componentManager;
            this.count = count;
            this.registry = registry;
            this.gui = gui;
            LOG.debug("New ComponentStarter: " + className + " * " + count);
        }

        public Void call() throws InterruptedException {
            LOG.debug("ComponentStarter running: " + className + " * " + count);
            Pair<Connection, Connection> connections = StreamConnection.createConnectionPair(registry);
            componentManager.newConnection(connections.first());
            ComponentLauncher launcher = new ComponentLauncher(connections.second(), new Config());
            LOG.info("Launching " + count + " instances of component '" + className + "'...");
            for (int i = 0; i < count; ++i) {
                Component c = instantiate(className, Component.class);
                if (c == null) {
                    break;
                }
                LOG.info("Launching " + className + " instance " + (i + 1) + "...");
                try {
                    c.initialise();
                    launcher.connect(c);
                    if (gui != null && c instanceof GUIComponent) {
                        gui.addGUIComponent((GUIComponent)c);
                    }
                    LOG.info(className + "instance " + (i + 1) + " launched successfully");
                }
                catch (ComponentConnectionException e) {
                    LOG.info(className + "instance " + (i + 1) + " failed: " + e.getMessage());
                    break;
                }
                catch (ComponentInitialisationException e) {
                    LOG.info(className + "instance " + (i + 1) + " failed", e);
                }
                catch (ConnectionException e) {
                    LOG.info(className + "instance " + (i + 1) + " failed", e);
                }
            }
            return null;
        }
    }

    private static class KernelChooserDialog extends JDialog {
        private KernelLaunchGUI components;

        public KernelChooserDialog(Config config) {
            super((Frame)null, "Choose kernel options");
            components = new KernelLaunchGUI(config);
            JButton ok = new JButton("OK");
            add(components, BorderLayout.CENTER);
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
        }

        public WorldModelCreator getWorldModelCreator() {
            return components.getGIS();
        }

        public Perception getPerception() {
            return components.getPerception();
        }

        public CommunicationModel getCommunicationModel() {
            return components.getCommunicationModel();
        }

        /*
        public Collection<String> getSimulators() {
            return components.getSimulators();
        }

        public Collection<String> getViewers() {
            return components.getViewers();
        }

        public Collection<Pair<String, Integer>> getAgents() {
            return components.getAgents();
        }
        */

        public Collection<Pair<String, Integer>> getAllComponents() {
            return components.getAllComponents();
        }
    }

    private static class KernelInfo {
        Kernel kernel;
        ComponentManager componentManager;
        List<GUIComponent> guiComponents;
        KernelChooserDialog choices;

        public KernelInfo(Kernel kernel, Perception perception, CommunicationModel comms, ComponentManager componentManager, List<GUIComponent> otherComponents, KernelChooserDialog choices) {
            this.kernel = kernel;
            this.componentManager = componentManager;
            this.choices = choices;
            guiComponents = new ArrayList<GUIComponent>(otherComponents);
        }
    }
}
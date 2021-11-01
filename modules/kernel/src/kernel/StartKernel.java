package kernel;

import static rescuecore2.misc.java.JavaTools.instantiate;
import static rescuecore2.misc.java.JavaTools.instantiateFactory;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import kernel.ui.KernelGUI;
import kernel.ui.KernelStartupPanel;
import kernel.ui.ScoreGraph;
import kernel.ui.ScoreTable;

import org.dom4j.DocumentException;

import rescuecore2.Constants;
import rescuecore2.GUIComponent;
import rescuecore2.components.Component;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentInitialisationException;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.config.ClassNameSetValueConstraint;
import rescuecore2.config.ClassNameValueConstraint;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.config.IntegerValueConstraint;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.log.LogException;
import rescuecore2.log.Logger;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.MutableBoolean;
import rescuecore2.misc.Pair;
import rescuecore2.misc.java.LoadableType;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.registry.Factory;
import rescuecore2.registry.Registry;
import rescuecore2.scenario.Scenario;
import rescuecore2.score.ScoreFunction;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
 * A class for launching the kernel.
 */
public final class StartKernel {
  private static final String NO_STARTUP_MENU = "--nomenu";
  private static final String NO_GUI = "--nogui";
  private static final String AUTORUN = "--autorun";

  private static final String GIS_MANIFEST_KEY = "Gis";
  private static final String PERCEPTION_MANIFEST_KEY = "Perception";
  private static final String COMMUNICATION_MANIFEST_KEY = "CommunicationModel";

  private static final String COMMAND_COLLECTOR_KEY = "kernel.commandcollectors";

  private static final String TERMINATION_KEY = "kernel.termination";

  private static final String GIS_REGEX = "(.+WorldModelCreator).class";
  private static final String PERCEPTION_REGEX = "(.+Perception).class";
  private static final String COMMUNICATION_REGEX = "(.+CommunicationModel).class";

  private static final LoadableType GIS_LOADABLE_TYPE = new LoadableType(GIS_MANIFEST_KEY, GIS_REGEX,
      WorldModelCreator.class);
  private static final LoadableType PERCEPTION_LOADABLE_TYPE = new LoadableType(PERCEPTION_MANIFEST_KEY,
      PERCEPTION_REGEX, Perception.class);
  private static final LoadableType COMMUNICATION_LOADABLE_TYPE = new LoadableType(COMMUNICATION_MANIFEST_KEY,
      COMMUNICATION_REGEX, CommunicationModel.class);

  private static final String KERNEL_STARTUP_TIME_KEY = "kernel.startup.connect-time";

  private static final String COMMAND_FILTERS_KEY = "kernel.commandfilters";
  private static final String AGENT_REGISTRAR_KEY = "kernel.agents.registrar";
  private static final String GUI_COMPONENTS_KEY = "kernel.ui.components";

  /** Utility class: private constructor. */
  private StartKernel() {
  }

  /**
   * Start a kernel.
   *
   * @param args Command line arguments.
   * @throws DocumentException
   */
  public static void main(String[] args) throws DocumentException {
    Config config = new Config();
    boolean showStartupMenu = true;
    boolean showGUI = true;
    boolean autorun = false;
    Logger.setLogContext("startup");
    try {
      args = CommandLineOptions.processArgs(args, config);
      for (String arg : args) {
        if (arg.equalsIgnoreCase(NO_GUI)) {
          showGUI = false;
        } else if (arg.equalsIgnoreCase(NO_STARTUP_MENU)) {
          showStartupMenu = false;
        } else if (arg.equalsIgnoreCase(AUTORUN)) {
          autorun = true;
        } else {
          Logger.warn("Unrecognised option: " + arg);
        }
      }
      // Process jar files
      processJarFiles(config);
      Registry localRegistry = new Registry("Kernel local registry");
      // Register preferred message, entity and property factories
      for (String next : config.getArrayValue(Constants.FACTORY_KEY, "")) {
        Factory factory = instantiateFactory(next, Factory.class);
        if (factory != null) {
          localRegistry.registerFactory(factory);
          Logger.info("Registered local factory: " + next);
        }
      }

      config.addConstraint(new IntegerValueConstraint(Constants.KERNEL_PORT_NUMBER_KEY, 1, 65535));
      config.addConstraint(new IntegerValueConstraint(KERNEL_STARTUP_TIME_KEY, 0, Integer.MAX_VALUE));
      config.addConstraint(new ClassNameSetValueConstraint(Constants.FACTORY_KEY, Factory.class));
      config.addConstraint(new ClassNameSetValueConstraint(COMMAND_FILTERS_KEY, CommandFilter.class));
      config.addConstraint(new ClassNameSetValueConstraint(TERMINATION_KEY, TerminationCondition.class));
      config.addConstraint(new ClassNameSetValueConstraint(COMMAND_COLLECTOR_KEY, CommandCollector.class));
      config.addConstraint(new ClassNameSetValueConstraint(GUI_COMPONENTS_KEY, GUIComponent.class));
      config.addConstraint(new ClassNameValueConstraint(AGENT_REGISTRAR_KEY, AgentRegistrar.class));
      config.addConstraint(new ClassNameValueConstraint(Constants.SCORE_FUNCTION_KEY, ScoreFunction.class));

      Logger.setLogContext("kernel");
      final KernelInfo kernelInfo = createKernel(config, showStartupMenu);
      if (kernelInfo == null) {
        System.exit(0);
      }
      KernelGUI gui = null;
      if (showGUI) {
        gui = new KernelGUI(kernelInfo.kernel, kernelInfo.componentManager, config, localRegistry, !autorun);
        for (GUIComponent next : kernelInfo.guiComponents) {
          gui.addGUIComponent(next);
          if (next instanceof KernelListener) {
            kernelInfo.kernel.addKernelListener((KernelListener) next);
          }
        }
        JFrame frame = new JFrame("Kernel GUI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
      autostartComponents(kernelInfo, localRegistry, gui, config);
      if (!showGUI || autorun) {
        waitForComponentManager(kernelInfo, config);
        Kernel kernel = kernelInfo.kernel;
        while (!kernel.hasTerminated()) {
          kernel.timestep();
        }
        kernel.shutdown();
      }
    } catch (ConfigException e) {
      Logger.fatal("Couldn't start kernel", e);
    } catch (KernelException e) {
      Logger.fatal("Couldn't start kernel", e);
    } catch (IOException e) {
      Logger.fatal("Couldn't start kernel", e);
    } catch (LogException e) {
      Logger.fatal("Couldn't write log", e);
    } catch (InterruptedException e) {
      Logger.fatal("Kernel interrupted");
    } catch (DocumentException e) {
      Logger.fatal("Document Exception ", e);
    }
  }

  private static KernelInfo createKernel(Config config, boolean showMenu) throws KernelException, DocumentException {
    KernelStartupOptions options = new KernelStartupOptions(config);
    // Show the chooser GUI
    if (showMenu) {
      JFrame frame = new JFrame() {
        private static final long serialVersionUID = 1L;
        {
          setUndecorated(true);
          setVisible(true);
          setLocationRelativeTo(null);
          setTitle("RCRS Start options");
          setVisible(true);
          java.awt.Toolkit.getDefaultToolkit().beep();
          setAlwaysOnTop(true);
          setAlwaysOnTop(false);
        }
      };
      final JDialog dialog = new JDialog(frame, "Setup kernel options", true) {
        private static final long serialVersionUID = 1L;

        @Override
        public void setVisible(boolean b) {
          super.setVisible(b);
          if (!isVisible())
            frame.dispose();
        }
      };
      KernelStartupPanel panel = new KernelStartupPanel(config, options);
      JButton okButton = new JButton("OK");
      JButton cancelButton = new JButton("Cancel");
      JPanel buttons = new JPanel(new FlowLayout());
      buttons.add(okButton);
      buttons.add(cancelButton);
      dialog.getContentPane().add(panel, BorderLayout.CENTER);
      dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
      final MutableBoolean ok = new MutableBoolean(true);
      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          ok.set(true);
          dialog.setVisible(false);
          dialog.dispose();
        }
      });
      cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          ok.set(false);
          dialog.setVisible(false);
          dialog.dispose();
        }
      });
      dialog.pack();
      dialog.setAlwaysOnTop(true);
      dialog.setAlwaysOnTop(false);
      dialog.setVisible(true);
      if (!ok.get()) {
        return null;
      }
    }
    WorldModelCreator gis = options.getWorldModelCreator();
    Perception perception = options.getPerception();
    CommunicationModel comms = options.getCommunicationModel();
    CommandFilter filter = makeCommandFilter(config);
    TerminationCondition termination = makeTerminationCondition(config);
    ScoreFunction score = makeScoreFunction(config);
    CommandCollector collector = makeCommandCollector(config);

    // Get the world model
    WorldModel<? extends Entity> worldModel = gis.buildWorldModel(config);
    Scenario scenario = gis.getScenario(config);
    // Create the kernel
    ScoreGraph graph = new ScoreGraph(score);
    Kernel kernel = new Kernel(config, perception, comms, worldModel, gis, filter, termination, graph, collector);
    // Create the component manager
    ComponentManager componentManager = new ComponentManager(kernel, worldModel, config, scenario);
    KernelInfo result = new KernelInfo(kernel, options, componentManager,
        makeGUIComponents(config, componentManager, perception, comms, termination, filter, graph, collector, score));
    return result;
  }

  private static void initialiseKernel(KernelInfo kernel, Config config, Registry registry) throws KernelException {
    registerInitialAgents(config, kernel.componentManager, kernel.kernel.getWorldModel());
    if (!config.getBooleanValue(KernelConstants.INLINE_ONLY_KEY, false)) {
      // Start the connection manager
      ConnectionManager connectionManager = new ConnectionManager();
      try {
        connectionManager.listen(config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY), registry,
            kernel.componentManager);
      } catch (IOException e) {
        throw new KernelException("Couldn't open kernel port", e);
      }
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
          } catch (InterruptedException e) {
          }
        }
      };
    }
    Thread waitThread = new Thread() {
      public void run() {
        try {
          kernel.componentManager.waitForAllAgents();
          kernel.componentManager.waitForAllSimulators();
          kernel.componentManager.waitForAllViewers();
        } catch (InterruptedException e) {
        }
        latch.countDown();
      }
    };
    waitThread.start();
    if (timeoutThread != null) {
      timeoutThread.start();
    }
    // Wait at the latch until either everything is connected or the
    // connection timeout expires
    Logger.info("Waiting for all agents, simulators and viewers to connect.");
    if (timeout > -1) {
      Logger.info("Connection timeout is " + timeout + "ms");
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
      waitThread.interrupt();
      if (timeoutThread != null) {
        timeoutThread.interrupt();
      }
      throw new KernelException("Interrupted");
    }
  }

  private static void autostartComponents(KernelInfo info, Registry registry, KernelGUI gui, Config config)
      throws InterruptedException {
    KernelStartupOptions options = info.options;
    Collection<Callable<Void>> all = new ArrayList<Callable<Void>>();
    Config launchConfig = new Config(config);
    launchConfig.removeExcept(Constants.RANDOM_SEED_KEY, Constants.RANDOM_CLASS_KEY);
    for (Pair<String, Integer> next : options.getInlineComponents()) {
      if (next.second() > 0) {
        all.add(new ComponentStarter(next.first(), info.componentManager, next.second(), registry, gui, launchConfig));
      }
    }
    ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    service.invokeAll(all);
  }

  private static void registerInitialAgents(Config config, ComponentManager c, WorldModel<? extends Entity> model)
      throws KernelException {
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
      Logger.debug("Command filter found: '" + next + "'");
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

  private static List<GUIComponent> makeGUIComponents(Config config, ComponentManager componentManager,
      Object... objectsToTest) {
    List<GUIComponent> result = new ArrayList<GUIComponent>();
    result.add(componentManager);
    List<String> classNames = config.getArrayValue(GUI_COMPONENTS_KEY, null);
    for (String next : classNames) {
      Logger.debug("GUI component found: '" + next + "'");
      GUIComponent c = instantiate(next, GUIComponent.class);
      if (c != null) {
        result.add(c);
      }
    }
    for (Object next : objectsToTest) {
      if (next instanceof GUIComponent) {
        result.add((GUIComponent) next);
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
    Logger.info("Looking for gis, perception, communication, agent, simulator and viewer implementations");
    processor.process();
  }

  private static class ComponentStarter implements Callable<Void> {
    private String className;
    private ComponentManager componentManager;
    private int count;
    private Registry registry;
    private KernelGUI gui;
    private Config config;

    public ComponentStarter(String className, ComponentManager componentManager, int count, Registry registry,
        KernelGUI gui, Config config) {
      this.className = className;
      this.componentManager = componentManager;
      this.count = count;
      this.registry = registry;
      this.gui = gui;
      this.config = config;
      Logger.debug("New ComponentStarter: " + className + " * " + count);
    }

    public Void call() throws InterruptedException {
      Logger.debug("ComponentStarter running: " + className + " * " + count);
      ComponentLauncher launcher = new InlineComponentLauncher(componentManager, config);
      launcher.setDefaultRegistry(registry);
      Logger.info("Launching " + count + " instances of component '" + className + "'...");
      for (int i = 0; i < count; ++i) {
        Component c = instantiate(className, Component.class);
        if (c == null) {
          break;
        }
        Logger.info("Launching " + className + " instance " + (i + 1) + "...");
        try {
          c.initialise();
          launcher.connect(c);
          if (gui != null && c instanceof GUIComponent) {
            gui.addGUIComponent((GUIComponent) c);
          }
          Logger.info(className + "instance " + (i + 1) + " launched successfully");
        } catch (ComponentConnectionException e) {
          Logger.info(className + "instance " + (i + 1) + " failed: " + e.getMessage());
          break;
        } catch (ComponentInitialisationException e) {
          Logger.info(className + "instance " + (i + 1) + " failed", e);
        } catch (ConnectionException e) {
          Logger.info(className + "instance " + (i + 1) + " failed", e);
        }
      }
      return null;
    }
  }

  private static class KernelInfo {
    Kernel kernel;
    KernelStartupOptions options;
    ComponentManager componentManager;
    List<GUIComponent> guiComponents;

    public KernelInfo(Kernel kernel, KernelStartupOptions options, ComponentManager componentManager,
        List<GUIComponent> otherComponents) {
      this.kernel = kernel;
      this.options = options;
      this.componentManager = componentManager;
      guiComponents = new ArrayList<GUIComponent>(otherComponents);
    }
  }

  public class DummyFrame extends JFrame {
    public DummyFrame(String title) {
      super(title);
      setUndecorated(true);
      setVisible(true);
      setLocationRelativeTo(null);
    }
  }
}
package rescuecore2.log;

import static rescuecore2.misc.java.JavaTools.instantiate;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.gui.ListModelList;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.view.ViewComponent;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.io.IOException;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

/**
   A class for viewing log files.
 */
public class LogViewer extends JPanel {
    private static final String VIEWERS_KEY = "log.viewers";

    private static final int TICK_STEP_SIZE = 10;

    private static final int VIEWER_SIZE = 500;

    private LogReader log;
    private JLabel timestep;
    private JSlider slider;
    private JList commandsList;
    private JList updatesList;
    private ListModelList<Command> commands;
    private ListModelList<Entity> updates;
    private List<ViewComponent> viewers;
    private JButton down;
    private JButton up;
    private int maxTime;

    /**
       Construct a LogViewer.
       @param reader The LogReader to read.
       @param config The system configuration.
       @throws LogException If there is a problem reading the log.
    */
    public LogViewer(LogReader reader, Config config) throws LogException {
        super(new BorderLayout());
        this.log = reader;
        registerViewers(config);
        maxTime = log.getMaxTimestep();
        slider = new JSlider(0, maxTime);
        down = new JButton("<-");
        up = new JButton("->");
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMinorTickSpacing(1);
        Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
        labels.put(maxTime, new JLabel(String.valueOf(maxTime)));
        for (int i = 0; i < maxTime; i += TICK_STEP_SIZE) {
            labels.put(i, new JLabel(String.valueOf(i)));
        }
        slider.setLabelTable(labels);
        slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    showTimestep(slider.getValue());
                }
            });
        down.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int value = slider.getValue() - 1;
                    if (value >= 0) {
                        slider.setValue(value);
                    }
                }
            });
        up.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int value = slider.getValue() + 1;
                    if (value <= maxTime) {
                        slider.setValue(value);
                    }
                }
            });
        JPanel lists = new JPanel(new GridLayout(0, 1));
        commands = new ListModelList<Command>();
        commandsList = new JList(commands);
        updates = new ListModelList<Entity>();
        updatesList = new JList(updates);
        JScrollPane s = new JScrollPane(commandsList);
        s.setBorder(BorderFactory.createTitledBorder("Commands"));
        s.setPreferredSize(commandsList.getPreferredScrollableViewportSize());
        lists.add(s);
        s = new JScrollPane(updatesList);
        s.setBorder(BorderFactory.createTitledBorder("Updates"));
        s.setPreferredSize(updatesList.getPreferredScrollableViewportSize());
        lists.add(s);
        timestep = new JLabel("Timestep: 0");
        JTabbedPane tabs = new JTabbedPane();
        for (ViewComponent next : viewers) {
            tabs.addTab(next.getViewerName(), next);
        }
        add(tabs, BorderLayout.CENTER);
        add(lists, BorderLayout.EAST);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(down, BorderLayout.WEST);
        bottom.add(slider, BorderLayout.CENTER);
        bottom.add(up, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        add(timestep, BorderLayout.NORTH);
        slider.setValue(0);
    }

    /**
       Show a particular timestep in the viewer.
       @param time The timestep to show. If this value is out of range then this method will silently return.
    */
    public void showTimestep(int time) {
        try {
            if (time < 0 || time > maxTime) {
                return;
            }
            timestep.setText("Timestep: " + time);
            commands.clear();
            updates.clear();
            CommandsRecord commandsRecord = log.getCommands(time);
            if (commandsRecord != null) {
                commands.addAll(commandsRecord.getCommands());
            }
            UpdatesRecord updatesRecord = log.getUpdates(time);
            if (updatesRecord != null) {
                updates.addAll(updatesRecord.getEntities());
            }
            WorldModel<? extends Entity> model = log.getWorldModel(time);
            for (ViewComponent next : viewers) {
                next.view(model, commandsRecord == null ? null : commandsRecord.getCommands(), updatesRecord == null ? null : updatesRecord.getEntities());
                next.repaint();
            }
            down.setEnabled(time != 0);
            up.setEnabled(time != maxTime);
            System.out.println("Showing time " + time);
            System.out.println(model.getAllEntities().size() + " entities");
            System.out.println(commands.size() + " commands");
            System.out.println(updates.size() + " updates");
        }
        catch (LogException e) {
            JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerViewers(Config config) {
        viewers = new ArrayList<ViewComponent>();
        for (String next : config.getArrayValue(VIEWERS_KEY, null)) {
            ViewComponent viewer = instantiate(next, ViewComponent.class);
            if (viewer != null) {
                viewer.initialise(config);
                viewers.add(viewer);
                System.out.println("Added viewer: " + next);
            }
        }
    }


    /**
       Launch a new LogViewer.
       @param args Command line arguments. Accepts only one argument: the name of a log file.
    */
    public static void main(String[] args) {
        Config config = new Config();
        try {
            args = CommandLineOptions.processArgs(args, config);
            if (args.length != 1) {
                printUsage();
                return;
            }
            String name = args[0];
            processJarFiles(config);
            LogReader reader = new FileLogReader(name);
            LogViewer viewer = new LogViewer(reader, config);
            viewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
            JFrame frame = new JFrame("Log viewer: " + name);
            frame.getContentPane().add(viewer, BorderLayout.CENTER);
            frame.pack();
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
            frame.setVisible(true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ConfigException e) {
            e.printStackTrace();
        }
        catch (LogException e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: LogViewer <filename>");
    }

    private static void processJarFiles(Config config) throws IOException {
        LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
        processor.addFactoryRegisterCallbacks();
        processor.process();
    }
}
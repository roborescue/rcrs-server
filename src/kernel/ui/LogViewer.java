package kernel.ui;

import kernel.log.KernelLogException;
import kernel.log.LogReader;
import kernel.log.FileLogReader;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;
import rescuecore2.view.WorldModelViewer;

import rescuecore2.standard.view.StandardViewLayer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

/**
   A class for viewing log files.
 */
public class LogViewer extends JPanel {
    private static final int TICK_STEP_SIZE = 10;

    private LogReader log;
    private JLabel timestep;
    private JSlider slider;
    private JList commandsList;
    private JList updatesList;
    private ListModelList<Command> commandsModel;
    private ListModelList<Entity> updatesModel;
    private List<Command> commands;
    private List<Entity> updates;
    private WorldModel<? extends Entity> world;
    private WorldModelViewer worldViewer;

    /**
       Launch a new LogViewer.
       @param args Command line arguments. Accepts only one argument: the name of a log file.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
            return;
        }
        String name = args[0];
        LogReader reader = new FileLogReader(name);
        LogViewer viewer = new LogViewer(reader);
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

    private static void printUsage() {
        System.out.println("Usage: LogViewer <filename>");
    }

    public LogViewer(LogReader reader) {
        super(new BorderLayout());
        this.log = reader;
        int max = log.getMaxTimestep();
        slider = new JSlider(1, max);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMinorTickSpacing(1);
        Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
        labels.put(1, new JLabel("1"));
        labels.put(max, new JLabel(String.valueOf(max)));
        for (int i = TICK_STEP_SIZE; i < max; i += TICK_STEP_SIZE) {
            labels.put(i, new JLabel(String.valueOf(i)));
        }
        slider.setLabelTable(labels);
        slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    selectTimestep(slider.getValue());
                }
            });
        JPanel lists = new JPanel(new GridLayout(0, 1));
        commands = new ArrayList<Command>();
        commandsModel = new ListModelList<Command>(commands);
        commandsList = new JList(commandsModel);
        updates = new ArrayList<Entity>();
        updatesModel = new ListModelList<Entity>(updates);
        updatesList = new JList(updatesModel);
        JScrollPane s = new JScrollPane(commandsList);
        s.setBorder(BorderFactory.createTitledBorder("Commands"));
        lists.add(s);
        s = new JScrollPane(updatesList);
        s.setBorder(BorderFactory.createTitledBorder("Updates"));
        lists.add(s);
        timestep = new JLabel("Timestep: 0");
        worldViewer = new WorldModelViewer();
        worldViewer.setBorder(BorderFactory.createTitledBorder("World model"));
        add(worldViewer, BorderLayout.CENTER);
        add(lists, BorderLayout.EAST);
        add(slider, BorderLayout.SOUTH);
        add(timestep, BorderLayout.NORTH);
    }

    public void selectTimestep(int time) {
        if (time < 0 || time > log.getMaxTimestep()) {
            return;
        }
        timestep.setText("Timestep: " + time);
        commands.clear();
        updates.clear();
        commands.addAll(log.getCommands(time));
        updates.addAll(log.getUpdates(time));
        world = log.getWorldModel(time);
        worldViewer.removeAllLayers();
        worldViewer.addLayer(new StandardViewLayer(world));
        worldViewer.repaint();
    }
}
package kernel.ui;

import kernel.log.KernelLogException;
import kernel.log.LogReader;
import kernel.log.FileLogReader;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityRegistry;
import rescuecore2.messages.Command;
import rescuecore2.messages.MessageRegistry;
import rescuecore2.view.WorldModelViewer;

import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.messages.StandardMessageFactory;

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

    private static final int VIEWER_SIZE = 500;

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
    private JButton down;
    private JButton up;
    private int maxTime;

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
        try {
            MessageRegistry.register(StandardMessageFactory.INSTANCE);
            EntityRegistry.register(StandardEntityFactory.INSTANCE);
            LogReader reader = new FileLogReader(name);
            LogViewer viewer = new LogViewer(reader);
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: LogViewer <filename>");
    }

    public LogViewer(LogReader reader) throws KernelLogException {
        super(new BorderLayout());
        this.log = reader;
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
                    selectTimestep(slider.getValue());
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
        worldViewer.addLayer(new StandardViewLayer());
        worldViewer.setBorder(BorderFactory.createTitledBorder("World model"));
        add(worldViewer, BorderLayout.CENTER);
        add(lists, BorderLayout.EAST);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(down, BorderLayout.WEST);
        bottom.add(slider, BorderLayout.CENTER);
        bottom.add(up, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        add(timestep, BorderLayout.NORTH);
        slider.setValue(0);
    }

    public void selectTimestep(int time) {
        try {
            if (time < 0 || time > maxTime) {
                return;
            }
            timestep.setText("Timestep: " + time);
            commands.clear();
            updates.clear();
            commands.addAll(log.getCommands(time));
            updates.addAll(log.getUpdates(time));
            world = log.getWorldModel(time);
            worldViewer.setWorldModel(world);
            worldViewer.repaint();
            down.setEnabled(time != 0);
            up.setEnabled(time != maxTime);
        }
        catch (KernelLogException e) {
            JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
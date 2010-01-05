package kernel;

import static rescuecore2.misc.java.JavaTools.instantiate;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JSplitPane;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.config.Config;
import rescuecore2.components.Simulator;
import rescuecore2.components.Viewer;
import rescuecore2.components.Agent;
import rescuecore2.components.Component;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ConfigTree;

/**
   A GUI for setting up kernel options.
*/
public class KernelLaunchGUI extends JPanel {
    private static final String AUTO_SUFFIX = ".auto";

    private SimulatorsPanel simulators;
    private ViewersPanel viewers;
    private AgentsPanel agents;
    private ComponentsPanel otherComponents;
    private JComboBox gis;
    private JComboBox perception;
    private JComboBox comms;
    private ConfigTree configTree;

    /**
       Create a kernel launch GUI.
       @param config The system configuration.
    */
    public KernelLaunchGUI(Config config) {
        super(new BorderLayout());
        gis = createComboBox(config, KernelConstants.GIS_KEY, WorldModelCreator.class);
        perception = createComboBox(config, KernelConstants.PERCEPTION_KEY, Perception.class);
        comms = createComboBox(config, KernelConstants.COMMUNICATION_MODEL_KEY, CommunicationModel.class);
        simulators = new SimulatorsPanel(config);
        viewers = new ViewersPanel(config);
        agents = new AgentsPanel(config);
        otherComponents = new ComponentsPanel(config);
        JScrollPane simulatorsScroll = new JScrollPane(simulators);
        simulatorsScroll.setBorder(BorderFactory.createTitledBorder("Simulators"));
        JScrollPane viewersScroll = new JScrollPane(viewers);
        viewersScroll.setBorder(BorderFactory.createTitledBorder("Viewers"));
        JScrollPane agentsScroll = new JScrollPane(agents);
        agentsScroll.setBorder(BorderFactory.createTitledBorder("Agents"));
        JScrollPane componentsScroll = new JScrollPane(otherComponents);
        componentsScroll.setBorder(BorderFactory.createTitledBorder("Other components"));
        configTree = new ConfigTree(config);
        JScrollPane configTreeScroll = new JScrollPane(configTree);
        configTreeScroll.setBorder(BorderFactory.createTitledBorder("Config"));

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel options = new JPanel(layout);
        JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, options, configTreeScroll);
        add(top, BorderLayout.CENTER);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        JLabel l = new JLabel("GIS:");
        layout.setConstraints(l, c);
        options.add(l);
        c.gridy = 1;
        l = new JLabel("Perception:");
        layout.setConstraints(l, c);
        options.add(l);
        c.gridy = 2;
        l = new JLabel("Communication model:");
        layout.setConstraints(l, c);
        options.add(l);
        c.gridy = 0;
        c.gridx = 1;
        c.weightx = 1;
        layout.setConstraints(gis, c);
        options.add(gis);
        c.gridy = 1;
        layout.setConstraints(perception, c);
        options.add(perception);
        c.gridy = 2;
        layout.setConstraints(comms, c);
        options.add(comms);

        // Simulators, viewers, agents, other components
        c.gridx = 0;
        ++c.gridy;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        layout.setConstraints(simulatorsScroll, c);
        options.add(simulatorsScroll);
        ++c.gridy;
        layout.setConstraints(viewersScroll, c);
        options.add(viewersScroll);
        ++c.gridy;
        layout.setConstraints(agentsScroll, c);
        options.add(agentsScroll);
        ++c.gridy;
        layout.setConstraints(componentsScroll, c);
        options.add(componentsScroll);
    }

    /**
       Get the names of all selected simulators.
       @return All selected simulator class names.
    */
    private Collection<String> getSimulators() {
        return simulators.getSelectedSimulators();
    }

    /**
       Get the names of all selected viewers.
       @return All selected viewer class names.
    */
    private Collection<String> getViewers() {
        return viewers.getSelectedViewers();
    }

    /**
       Get the names and the required number of all selected agents.
       @return All selected agent class names and the requested number of each.
    */
    private Collection<Pair<String, Integer>> getAgents() {
        return agents.getSelectedAgents();
    }

    /**
       Get the names of all selected components.
       @return All selected component class names and the requested number of each.
    */
    public Collection<Pair<String, Integer>> getAllComponents() {
        Collection<Pair<String, Integer>> all = new ArrayList<Pair<String, Integer>>();
        for (String next : simulators.getSelectedSimulators()) {
            all.add(new Pair<String, Integer>(next, 1));
        }
        for (String next : viewers.getSelectedViewers()) {
            all.add(new Pair<String, Integer>(next, 1));
        }
        for (Pair<String, Integer> next : agents.getSelectedAgents()) {
            all.add(next);
        }
        for (String next : otherComponents.getSelectedComponents()) {
            all.add(new Pair<String, Integer>(next, 1));
        }
        return all;
    }

    /**
       Get the world model creator.
       @return The chosen WorldModelCreator implementation.
    */
    public WorldModelCreator getGIS() {
        return (WorldModelCreator)gis.getSelectedItem();
    }

    /**
       Get the perception module.
       @return The chosen Perception implementation.
    */
    public Perception getPerception() {
        return (Perception)perception.getSelectedItem();
    }

    /**
       Get the communication model.
       @return The chosen CommunicationModel implementation.
    */
    public CommunicationModel getCommunicationModel() {
        return (CommunicationModel)comms.getSelectedItem();
    }

    private <T> JComboBox createComboBox(Config config, String key, Class<T> clazz) {
        Pair<List<T>, Integer> options = createChoices(config, key, clazz);
        Object[] choices = options.first().toArray();
        JComboBox result = new JComboBox(choices);
        result.setSelectedIndex(options.second());
        result.setEnabled(choices.length > 1);
        return result;
    }

    private <T> Pair<List<T>, Integer> createChoices(Config config, String key, Class<T> expectedClass) {
        List<T> instances = new ArrayList<T>();
        int index = 0;
        int selectedIndex = 0;
        System.out.println("Loading options: " + key);
        List<String> classNames = config.getArrayValue(key);
        String auto = config.getValue(key + AUTO_SUFFIX, null);
        for (String next : classNames) {
            System.out.println("Option found: '" + next + "'");
            T t = instantiate(next, expectedClass);
            if (t != null) {
                instances.add(t);
                if (next.equals(auto)) {
                    selectedIndex = index;
                }
                ++index;
            }
        }
        return new Pair<List<T>, Integer>(instances, selectedIndex);
    }

    private static final class SimulatorsPanel extends JPanel {
        private List<Pair<String, JCheckBox>> simulators;

        private SimulatorsPanel(Config config) {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;
            setLayout(layout);
            simulators = new ArrayList<Pair<String, JCheckBox>>();
                List<String> classNames = config.getArrayValue(KernelConstants.SIMULATORS_KEY, null);
                List<String> autoClassNames = config.getArrayValue(KernelConstants.SIMULATORS_KEY + AUTO_SUFFIX, null);
                for (String next : classNames) {
                    Simulator s = instantiate(next, Simulator.class);
                    if (s != null) {
                        c.gridx = 0;
                        c.weightx = 1;
                        JLabel l = new JLabel(s.getName());
                        layout.setConstraints(l, c);
                        add(l);
                        c.gridx = 1;
                        c.weightx = 0;
                        JCheckBox check = new JCheckBox();
                        check.setSelected(autoClassNames.contains(next));
                        layout.setConstraints(check, c);
                        add(check);
                        simulators.add(new Pair<String, JCheckBox>(next, check));
                        ++c.gridy;
                    }
                }
        }

        private Collection<String> getSelectedSimulators() {
            Collection<String> result = new ArrayList<String>();
            for (Pair<String, JCheckBox> next : simulators) {
                if (next.second().isSelected()) {
                    result.add(next.first());
                }
            }
            return result;
        }
    }

    private static final class ViewersPanel extends JPanel {
        private List<Pair<String, JCheckBox>> viewers;

        private ViewersPanel(Config config) {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;
            setLayout(layout);
            viewers = new ArrayList<Pair<String, JCheckBox>>();
                List<String> classNames = config.getArrayValue(KernelConstants.VIEWERS_KEY, null);
                List<String> autoClassNames = config.getArrayValue(KernelConstants.VIEWERS_KEY + AUTO_SUFFIX, null);
                for (String next : classNames) {
                    Viewer v = instantiate(next, Viewer.class);
                    if (v != null) {
                        c.gridx = 0;
                        c.weightx = 1;
                        JLabel l = new JLabel(v.getName());
                        layout.setConstraints(l, c);
                        add(l);
                        c.gridx = 1;
                        c.weightx = 0;
                        JCheckBox check = new JCheckBox();
                        check.setSelected(autoClassNames.contains(next));
                        layout.setConstraints(check, c);
                        add(check);
                        viewers.add(new Pair<String, JCheckBox>(next, check));
                        ++c.gridy;
                    }
                }
        }

        private Collection<String> getSelectedViewers() {
            Collection<String> result = new ArrayList<String>();
            for (Pair<String, JCheckBox> next : viewers) {
                if (next.second().isSelected()) {
                    result.add(next.first());
                }
            }
            return result;
        }
    }

    private static final class AgentsPanel extends JPanel {
        private List<Pair<String, Pair<JSpinner, JCheckBox>>> agents;

        private AgentsPanel(Config config) {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;
            setLayout(layout);
            agents = new ArrayList<Pair<String, Pair<JSpinner, JCheckBox>>>();
            List<String> classNames = config.getArrayValue(KernelConstants.AGENTS_KEY, null);
            List<String> autoClassNames = config.getArrayValue(KernelConstants.AGENTS_KEY + AUTO_SUFFIX, null);
                for (String next : classNames) {
                    Agent a = instantiate(next, Agent.class);
                    if (a != null) {
                        c.gridx = 0;
                        c.weightx = 1;
                        JLabel l = new JLabel(a.getName());
                        layout.setConstraints(l, c);
                        add(l);
                        int count = 0;
                        String argLine = getArgLine(next, autoClassNames);
                        boolean all = false;
                        if (argLine != null) {
                            count = getMultiplier(argLine);
                            if (count == Integer.MAX_VALUE) {
                                count = 0;
                                all = true;
                            }
                        }
                        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(count, 0, Integer.MAX_VALUE, 1));
                        final JCheckBox check = new JCheckBox("Maximum");
                        check.setSelected(all);
                        spinner.setEnabled(!all);
                        check.addChangeListener(new ChangeListener() {
                                public void stateChanged(ChangeEvent e) {
                                    spinner.setEnabled(!check.isSelected());
                                }
                            });
                        c.gridx = 1;
                        c.weightx = 0;
                        layout.setConstraints(spinner, c);
                        add(spinner);
                        c.gridx = 2;
                        layout.setConstraints(check, c);
                        add(check);
                        agents.add(new Pair<String, Pair<JSpinner, JCheckBox>>(next, new Pair<JSpinner, JCheckBox>(spinner, check)));
                        ++c.gridy;
                    }
                }
        }

        private Collection<Pair<String, Integer>> getSelectedAgents() {
            Collection<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
            for (Pair<String, Pair<JSpinner, JCheckBox>> next : agents) {
                int count = ((Number)next.second().first().getValue()).intValue();
                if (next.second().second().isSelected()) {
                    count = Integer.MAX_VALUE;
                }
                if (count > 0) {
                    result.add(new Pair<String, Integer>(next.first(), count));
                }
            }
            return result;
        }

        private String getArgLine(String className, List<String> argLines) {
            for (String next : argLines) {
                if (next.startsWith(className)) {
                    return next;
                }
            }
            return null;
        }

        private int getMultiplier(String argLine) {
            int index = argLine.indexOf("*");
            if (index == -1) {
                return 1;
            }
            String arg = argLine.substring(index + 1);
            if ("n".equals(arg)) {
                return Integer.MAX_VALUE;
            }
            return Integer.parseInt(arg);
        }
    }

    private static final class ComponentsPanel extends JPanel {
        private List<Pair<String, JCheckBox>> components;

        private ComponentsPanel(Config config) {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;
            setLayout(layout);
            components = new ArrayList<Pair<String, JCheckBox>>();
                List<String> classNames = config.getArrayValue(KernelConstants.COMPONENTS_KEY, null);
                List<String> autoClassNames = config.getArrayValue(KernelConstants.COMPONENTS_KEY + AUTO_SUFFIX, null);
                for (String next : classNames) {
                    Component comp = instantiate(next, Component.class);
                    if (comp != null) {
                        c.gridx = 0;
                        c.weightx = 1;
                        JLabel l = new JLabel(comp.getName());
                        layout.setConstraints(l, c);
                        add(l);
                        c.gridx = 1;
                        c.weightx = 0;
                        JCheckBox check = new JCheckBox();
                        check.setSelected(autoClassNames.contains(next));
                        layout.setConstraints(check, c);
                        add(check);
                        components.add(new Pair<String, JCheckBox>(next, check));
                        ++c.gridy;
                    }
                }
        }

        private Collection<String> getSelectedComponents() {
            Collection<String> result = new ArrayList<String>();
            for (Pair<String, JCheckBox> next : components) {
                if (next.second().isSelected()) {
                    result.add(next.first());
                }
            }
            return result;
        }
    }
}
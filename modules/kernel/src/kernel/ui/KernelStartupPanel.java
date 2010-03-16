package kernel.ui;

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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Collection;
import java.util.List;

import kernel.WorldModelCreator;
import kernel.Perception;
import kernel.CommunicationModel;
import kernel.KernelStartupOptions;

import rescuecore2.config.Config;
import rescuecore2.components.Component;
import rescuecore2.misc.gui.ConfigTree;

/**
   A JPanel for displaying and editing kernel startup options.
*/
public class KernelStartupPanel extends JPanel {
    private static final String AUTO_SUFFIX = ".auto";

    /**
       Create a kernel launch GUI.
       @param config The system configuration.
       @param options The kernel startup options.
    */
    public KernelStartupPanel(Config config, final KernelStartupOptions options) {
        super(new BorderLayout());
        final JComboBox gis = createComboBox(options.getAvailableWorldModelCreators(), options.getWorldModelCreator());
        final JComboBox perception = createComboBox(options.getAvailablePerceptions(), options.getPerception());
        final JComboBox comms = createComboBox(options.getAvailableCommunicationModels(), options.getCommunicationModel());
        CheckboxPanel simulators = new CheckboxPanel(options.getAvailableSimulators(), options);
        CheckboxPanel viewers = new CheckboxPanel(options.getAvailableViewers(), options);
        SpinnerPanel agents = new SpinnerPanel(options.getAvailableAgents(), options);
        CheckboxPanel otherComponents = new CheckboxPanel(options.getAvailableComponents(), options);
        JScrollPane simulatorsScroll = new JScrollPane(simulators);
        simulatorsScroll.setBorder(BorderFactory.createTitledBorder("Simulators"));
        JScrollPane viewersScroll = new JScrollPane(viewers);
        viewersScroll.setBorder(BorderFactory.createTitledBorder("Viewers"));
        JScrollPane agentsScroll = new JScrollPane(agents);
        agentsScroll.setBorder(BorderFactory.createTitledBorder("Agents"));
        JScrollPane componentsScroll = new JScrollPane(otherComponents);
        componentsScroll.setBorder(BorderFactory.createTitledBorder("Other components"));
        ConfigTree configTree = new ConfigTree(config);
        JScrollPane configTreeScroll = new JScrollPane(configTree);
        configTreeScroll.setBorder(BorderFactory.createTitledBorder("Config"));

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel optionsPanel = new JPanel(layout);
        JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionsPanel, configTreeScroll);
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
        optionsPanel.add(l);
        c.gridy = 1;
        l = new JLabel("Perception:");
        layout.setConstraints(l, c);
        optionsPanel.add(l);
        c.gridy = 2;
        l = new JLabel("Communication model:");
        layout.setConstraints(l, c);
        optionsPanel.add(l);
        c.gridy = 0;
        c.gridx = 1;
        c.weightx = 1;
        layout.setConstraints(gis, c);
        optionsPanel.add(gis);
        c.gridy = 1;
        layout.setConstraints(perception, c);
        optionsPanel.add(perception);
        c.gridy = 2;
        layout.setConstraints(comms, c);
        optionsPanel.add(comms);

        // Simulators, viewers, agents, other components
        c.gridx = 0;
        ++c.gridy;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        layout.setConstraints(simulatorsScroll, c);
        optionsPanel.add(simulatorsScroll);
        ++c.gridy;
        layout.setConstraints(viewersScroll, c);
        optionsPanel.add(viewersScroll);
        ++c.gridy;
        layout.setConstraints(agentsScroll, c);
        optionsPanel.add(agentsScroll);
        ++c.gridy;
        layout.setConstraints(componentsScroll, c);
        optionsPanel.add(componentsScroll);

        // Event listeners
        gis.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.setWorldModelCreator((WorldModelCreator)gis.getSelectedItem());
                }
            });
        perception.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.setPerception((Perception)perception.getSelectedItem());
                }
            });
        comms.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.setCommunicationModel((CommunicationModel)comms.getSelectedItem());
                }
            });
    }

    private <T> JComboBox createComboBox(List<T> options, T selected) {
        Object[] choices = options.toArray();
        JComboBox result = new JComboBox(choices);
        result.setSelectedItem(selected);
        result.setEnabled(choices.length > 1);
        return result;
    }

    private static final class CheckboxPanel extends JPanel {
        private CheckboxPanel(Collection<? extends Component> available, final KernelStartupOptions options) {
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
            for (Component t : available) {
                c.gridx = 0;
                c.weightx = 1;
                JLabel l = new JLabel(t.getName());
                layout.setConstraints(l, c);
                add(l);
                c.gridx = 1;
                c.weightx = 0;
                final JCheckBox check = new JCheckBox();
                check.setSelected(options.getInstanceCount(t) > 0);
                options.setInstanceCount(t, check.isSelected() ? 1 : 0);
                layout.setConstraints(check, c);
                add(check);
                ++c.gridy;
                final Component comp = t;
                check.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            options.setInstanceCount(comp, check.isSelected() ? 1 : 0);
                        }
                    });
            }
        }
    }

    private static final class SpinnerPanel extends JPanel {
        private SpinnerPanel(Collection<? extends Component> available, final KernelStartupOptions options) {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;
            setLayout(layout);
            for (Component t : available) {
                c.gridx = 0;
                c.weightx = 1;
                JLabel l = new JLabel(t.getName());
                layout.setConstraints(l, c);
                add(l);
                int count = options.getInstanceCount(t);
                boolean all = count == Integer.MAX_VALUE;
                final JSpinner spinner = new JSpinner(new SpinnerNumberModel(count == Integer.MAX_VALUE ? 0 : count, 0, Integer.MAX_VALUE, 1));
                final JCheckBox check = new JCheckBox("Maximum");
                check.setSelected(all);
                spinner.setEnabled(!all);
                final Component comp = t;
                check.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            spinner.setEnabled(!check.isSelected());
                            if (check.isSelected()) {
                                options.setInstanceCount(comp, Integer.MAX_VALUE);
                            }
                            else {
                                options.setInstanceCount(comp, ((Number)spinner.getValue()).intValue());
                            }
                        }
                    });
                spinner.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            options.setInstanceCount(comp, ((Number)spinner.getValue()).intValue());
                        }
                    });
                c.gridx = 1;
                c.weightx = 0;
                layout.setConstraints(spinner, c);
                add(spinner);
                c.gridx = 2;
                layout.setConstraints(check, c);
                add(check);
                ++c.gridy;
            }
        }
    }
}

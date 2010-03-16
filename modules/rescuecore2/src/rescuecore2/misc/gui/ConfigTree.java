package rescuecore2.misc.gui;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.AbstractCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.EventObject;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigConstraint;

/**
   A JTree that knows how to display and edit Config objects.
*/
public class ConfigTree extends JTree {
    private Config config;

    /**
       Create a ConfigTree that will display a particular Config.
       @param config The Config to display.
    */
    public ConfigTree(Config config) {
        this.config = config;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Config");
        List<String> keys = new ArrayList<String>(config.getAllKeys());
        Collections.sort(keys);
        buildModel(root, keys);
        setModel(new DefaultTreeModel(root));
        setEditable(true);
        setCellRenderer(new ConfigEntryCellRenderer());
        setCellEditor(new ConfigEntryCellEditor());
        setInvokesStopCellEditing(true);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    private void buildModel(DefaultMutableTreeNode root, Collection<String> keys) {
        Map<String, DefaultMutableTreeNode> branches = new HashMap<String, DefaultMutableTreeNode>();
        for (String next : keys) {
            String[] branchNames = next.split("\\.");
            // Create all parent branches if required
            DefaultMutableTreeNode parent = root;
            StringBuilder branchName = new StringBuilder();
            for (int i = 0; i < branchNames.length - 1; ++i) {
                branchName.append(branchNames[i]);
                String name = branchName.toString();
                DefaultMutableTreeNode nextParent = branches.get(name);
                if (nextParent == null) {
                    nextParent = new DefaultMutableTreeNode(new ConfigCategoryNode(name));
                    branches.put(name, nextParent);
                    parent.add(nextParent);
                }
                branchName.append(".");
                parent = nextParent;
            }
            // Create the leaf node
            DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(new ConfigEntryNode(next, config.getValue(next)));
            parent.add(leaf);
        }
    }

    private static final class ConfigCategoryNode {
        private String prefix;

        private ConfigCategoryNode(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return prefix;
        }
    }

    private static final class ConfigEntryNode {
        private String key;
        private String value;

        private ConfigEntryNode(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String getKey() {
            return key;
        }

        String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + ": " + value;
        }
    }

    private final class ConfigEntryCellRenderer extends JLabel implements TreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode)value);
            Object o = node.getUserObject();
            setText(o.toString());
            setOpaque(false);
            setToolTipText(null);
            if (o instanceof ConfigEntryNode) {
                ConfigEntryNode entry = (ConfigEntryNode)o;
                String key = entry.getKey();
                StringBuilder problems = new StringBuilder();
                for (ConfigConstraint constraint : config.getViolatedConstraints()) {
                    if (constraint.getKeys().contains(key)) {
                        if (problems.length() != 0) {
                            problems.append("\n");
                        }
                        problems.append(constraint.getDescription());
                    }
                }
                if (problems.length() != 0) {
                    setBackground(Color.RED);
                    setOpaque(true);
                    setToolTipText(problems.toString());
                }
            }
            return this;
        }
    }

    private final class ConfigEntryCellEditor extends AbstractCellEditor implements TreeCellEditor, ActionListener {
        private JPanel panel;
        private JLabel label;
        private JTextField field;
        private DefaultMutableTreeNode node;
        private String key;

        private ConfigEntryCellEditor() {
            panel = new JPanel(new BorderLayout());
            label = new JLabel();
            field = new JTextField();
            panel.add(label, BorderLayout.CENTER);
            panel.add(field, BorderLayout.EAST);
            field.addActionListener(this);
            node = null;
        }

        @Override
        public boolean isCellEditable(EventObject o) {
            if (o instanceof MouseEvent && o.getSource() instanceof JTree) {
                JTree tree = (JTree)o.getSource();
                MouseEvent e = (MouseEvent)o;
                if (e.getClickCount() > 1) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    Object leaf = path.getLastPathComponent();
                    if (leaf instanceof DefaultMutableTreeNode) {
                        Object content = ((DefaultMutableTreeNode)leaf).getUserObject();
                        return content instanceof ConfigEntryNode;
                    }
                }
            }
            return false;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object data, boolean selected, boolean expanded, boolean leaf, int row) {
            node = ((DefaultMutableTreeNode)data);
            ConfigEntryNode entry = (ConfigEntryNode)node.getUserObject();
            key = entry.getKey();
            label.setText(key + ": ");
            field.setText(entry.getValue());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return new ConfigEntryNode(key, field.getText());
        }

        @Override
        public boolean stopCellEditing() {
            if (node != null) {
                String value = field.getText();
                node.setUserObject(new ConfigEntryNode(key, value));
                config.setValue(key, value);
            }
            fireEditingStopped();
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stopCellEditing();
        }
    }
}

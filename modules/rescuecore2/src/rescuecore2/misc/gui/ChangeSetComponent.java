package rescuecore2.misc.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.AbstractListModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.ChangeSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class ChangeSetComponent extends JPanel {
    private PropertyTableModel propertiesModel;
    private EntityListModel changedModel;
    private EntityListModel deletedModel;
    private ChangeSet changes;

    public ChangeSetComponent() {
        super(new BorderLayout());
        propertiesModel = new PropertyTableModel();
        changedModel = new EntityListModel();
        deletedModel = new EntityListModel();
        JTable propsTable = new JTable(propertiesModel);
        JList changedList = new JList(changedModel);
        JList deletedList = new JList(deletedModel);

        JScrollPane scroll = new JScrollPane(propsTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Properties"));
        add(scroll, BorderLayout.EAST);
        scroll = new JScrollPane(changedList);
        scroll.setBorder(BorderFactory.createTitledBorder("Changed entities"));
        add(scroll, BorderLayout.CENTRE);
        scroll = new JScrollPane(deletedList);
        scroll.setBorder(BorderFactory.createTitledBorder("Deleted entities"));
        add(scroll, BorderLayout.WEST);

        changedList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    EntityID id = changedList.getSelectedItem();
                    if (id != null) {
                        Set<Property> changes = getChangedProperties(id);
                        propertiesModel.show(changes);
                    }
                }
            });
    }

    public void show(ChangeSet newChanges) {
        changes = newChanges;
        propertiesModel.show(new HashSet<Property>());
        changedModel.show(changes.getChangedEntities());
        deletedModel.show(changes.getDeletedEntities());
    }

    private class EntityListModel extends AbstractListModel {
        private List<EntityID> ids;

        public EntityListModel() {
            ids = new ArrayList<EntityID>();
        }

        public void show(Set<EntityID> newIDs) {
            ids.clear();
            ids.addAll(newIDs);
        }

        public int getSize() {
            return ids.size();
        }

        public Object getElementAt(int index) {
            return ids.get(index);
        }
    }

    private class PropertyTableModel extends AbstractTableModel {
        private List<Property> properties;

        public PropertyTableModel() {
            properties = new ArrayList<Property>();
        }

        public void show(Set<Property> p) {
            properties.clear();
            properties.addAll(p);
        }

        public int getRowCount() {
            return properties.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int row, int col) {
            if (row < 0 || row >= properties.size()) {
                throw new IllegalArgumentException("Illegal row: " + row);
            }
            Property p = properties.get(row);
            switch (col) {
            case 0:
                return p.getURN();
            case 1:
                return p.isDefined() ? "undefined" : p.getValue().toString();
            }
            throw new IllegalArgumentException("Illegal column: " + col);            
        }

        public String getColumnName(int col) {
            switch (col) {
            case 0:
                return "URN";
            case 1:
                return "Value";
            }
            throw new IllegalArgumentException("Illegal column: " + col);
        }
    }
}
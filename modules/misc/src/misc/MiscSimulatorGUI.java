package misc;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.Collection;

/**
   A GUI component for viewing the misc simulator state.
 */
public class MiscSimulatorGUI extends JPanel {
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_HP = 1;
    private static final int COLUMN_DAMAGE = 2;
    private static final int COLUMN_BURY = 3;
    private static final int COLUMN_COLLAPSE = 4;
    private static final int COLUMN_FIRE = 5;
    private static final int COLUMN_BURIEDNESS = 6;
    private static final int COLUMNS = 7;

    private MiscTableModel model;

    /**
       Create a MiscSimulatorGUI.
     */
    public MiscSimulatorGUI() {
        super(new BorderLayout());
        model = new MiscTableModel();
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
    }

    /**
       Refresh the UI.
       @param data The set of human data to show.
     */
    public void refresh(Collection<HumanAttributes> data) {
        model.setData(data.toArray(new HumanAttributes[data.size()]));
    }

    private static class MiscTableModel extends AbstractTableModel {
        private HumanAttributes[] data;

        void setData(HumanAttributes[] data) {
            this.data = data;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.length;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS;
        }

        @Override
        public Object getValueAt(int row, int col) {
            HumanAttributes att = data[row];
            switch (col) {
            case COLUMN_ID:
                return att.getID();
            case COLUMN_HP:
                return att.getHuman().isHPDefined() ? String.valueOf(att.getHuman().getHP()) : "undefined";
            case COLUMN_DAMAGE:
                return att.getHuman().isDamageDefined() ? String.valueOf(att.getHuman().getDamage()) : "undefined";
            case COLUMN_BURY:
                return att.getBuriednessDamage();
            case COLUMN_COLLAPSE:
                return att.getCollapseDamage();
            case COLUMN_FIRE:
                return att.getFireDamage();
            case COLUMN_BURIEDNESS:
                return att.getHuman().isBuriednessDefined() ? String.valueOf(att.getHuman().getBuriedness()) : "undefined";
            default:
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
            case COLUMN_ID:
                return "ID";
            case COLUMN_HP:
                return "HP";
            case COLUMN_DAMAGE:
                return "Total damage";
            case COLUMN_BURY:
                return "Buriedness damage";
            case COLUMN_COLLAPSE:
                return "Collapse damage";
            case COLUMN_FIRE:
                return "Fire damage";
            case COLUMN_BURIEDNESS:
                return "Buriedness";
            default:
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }
}

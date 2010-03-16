package kernel.ui;

import rescuecore2.score.ScoreFunction;
import rescuecore2.score.CompositeScoreFunction;
import rescuecore2.score.DelegatingScoreFunction;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.config.Config;
import rescuecore2.Timestep;
import rescuecore2.GUIComponent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.AbstractListModel;
import javax.swing.UIManager;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
   A ScoreFunction that also provides a JTable for viewing the components of the score.
 */
public class ScoreTable extends DelegatingScoreFunction implements GUIComponent {
    private ScoreModel model;

    /**
       Construct a ScoreTable that wraps a child score function.
       @param child The child score function.
    */
    public ScoreTable(ScoreFunction child) {
        super("Score Table", child);
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        super.initialise(world, config);
        model = new ScoreModel(child);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        model.update(world, timestep);
        return super.score(world, timestep);
    }

    @Override
    public JComponent getGUIComponent() {
        JTable table = new JTable(model.table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scroll = new JScrollPane(table);
        JList rowHeader = new JList(model.list);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));
        rowHeader.setBackground(table.getBackground());
        rowHeader.setOpaque(true);
        scroll.setRowHeaderView(rowHeader);
        return scroll;
    }

    @Override
    public String getGUIComponentName() {
        return "Score";
    }

    private static class RowHeaderRenderer extends JLabel implements ListCellRenderer {
        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(LEFT);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        @Override
        public JLabel getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private static class ScoreModel {
        ScoreTableModel table;
        ScoreListModel list;
        private int steps;
        private List<ScoreFunctionEntry> entries;

        ScoreModel(ScoreFunction root) {
            steps = 0;
            entries = new ArrayList<ScoreFunctionEntry>();
            populateEntries(root, "");
            table = new ScoreTableModel();
            list = new ScoreListModel();
        }

        private void populateEntries(ScoreFunction root, String prefix) {
            String suffix = "";
            if (!(root instanceof ScoreTable || root instanceof ScoreGraph)) {
                entries.add(new ScoreFunctionEntry(root, prefix));
                suffix = "--";
            }
            if (root instanceof DelegatingScoreFunction) {
                populateEntries(((DelegatingScoreFunction)root).getChildFunction(), prefix + suffix);
            }
            if (root instanceof CompositeScoreFunction) {
                Set<ScoreFunction> children = ((CompositeScoreFunction)root).getChildFunctions();
                for (ScoreFunction next : children) {
                    populateEntries(next, prefix + suffix);
                }
            }
        }

        void update(WorldModel<? extends Entity> world, Timestep timestep) {
            for (ScoreFunctionEntry next : entries) {
                next.update(world, timestep);
            }
            steps = timestep.getTime();
            table.fireTableStructureChanged();
        }

        private class ScoreTableModel extends AbstractTableModel {
            @Override
            public String getColumnName(int col) {
                return String.valueOf(col + 1);
            }

            @Override
            public int getRowCount() {
                return entries.size();
            }

            @Override
            public int getColumnCount() {
                return steps;
            }

            @Override
            public Object getValueAt(int row, int column) {
                return entries.get(row).getScore(column + 1);
            }
        }

        private class ScoreListModel extends AbstractListModel {
            @Override
            public int getSize() {
                return entries.size();
            }

            @Override
            public Object getElementAt(int row) {
                return entries.get(row).getScoreFunctionName();
            }
        }

        private static class ScoreFunctionEntry {
            private ScoreFunction function;
            private String prefix;
            private Map<Integer, Double> scores;

            public ScoreFunctionEntry(ScoreFunction f, String prefix) {
                function = f;
                this.prefix = prefix;
                scores = new HashMap<Integer, Double>();
            }

            public String getScoreFunctionName() {
                return prefix + function.getName();
            }

            public double getScore(int step) {
                return scores.containsKey(step) ? scores.get(step) : Double.NaN;
            }

            void update(WorldModel<? extends Entity> world, Timestep timestep) {
                double d = function.score(world, timestep);
                scores.put(timestep.getTime(), d);
            }
        }
    }
}

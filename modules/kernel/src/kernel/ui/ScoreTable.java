package kernel.ui;

import rescuecore2.score.ScoreFunction;
import rescuecore2.score.CompositeScoreFunction;
import rescuecore2.score.DelegatingScoreFunction;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.config.Config;
import rescuecore2.Timestep;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import kernel.Kernel;

/**
   A ScoreFunction that also provides a JTable for viewing the components of the score.
 */
public class ScoreTable implements KernelGUIComponent, ScoreFunction {
    private ScoreFunction child;
    private JTable table;
    private ScoreTableModel model;

    /**
       Construct a ScoreTable that wraps a child score function.
       @param child The child score function.
    */
    public ScoreTable(ScoreFunction child) {
        this.child = child;
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        child.initialise(world, config);
        model = new ScoreTableModel(child);
        table = new JTable(model);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        model.update(world, timestep);
        return child.score(world, timestep);
    }

    @Override
    public JComponent getGUIComponent(Kernel kernel, Config config) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return new JScrollPane(table);
    }

    @Override
    public String getGUIComponentName() {
        return "Score";
    }

    private static class ScoreTableModel extends AbstractTableModel {
        private int steps;
        private List<ScoreFunctionEntry> entries;

        ScoreTableModel(ScoreFunction root) {
            steps = 0;
            entries = new ArrayList<ScoreFunctionEntry>();
            populateEntries(root, "");
        }

        private void populateEntries(ScoreFunction root, String prefix) {
            entries.add(new ScoreFunctionEntry(root, prefix));
            if (root instanceof DelegatingScoreFunction) {
                populateEntries(((DelegatingScoreFunction)root).getChildFunction(), prefix + "--");
            }
            if (root instanceof CompositeScoreFunction) {
                Set<ScoreFunction> children = ((CompositeScoreFunction)root).getChildFunctions();
                for (ScoreFunction next : children) {
                    populateEntries(next, prefix + "--");
                }
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return "Score function";
            }
            return String.valueOf(col);
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return steps + 1;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                return entries.get(row).getScoreFunctionName();
            }
            else {
                return entries.get(row).getScore(column);
            }
        }

        void update(WorldModel<? extends Entity> world, Timestep timestep) {
            for (ScoreFunctionEntry next : entries) {
                next.update(world, timestep);
            }
            steps = timestep.getTime();
            fireTableStructureChanged();
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
                return prefix + function.toString();
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
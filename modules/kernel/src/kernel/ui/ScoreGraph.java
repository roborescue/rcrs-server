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
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.BorderLayout;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

/**
   A ScoreFunction that also provides a components for graphing the components of the score.
 */
public class ScoreGraph extends DelegatingScoreFunction implements GUIComponent {
    private JFreeChart chart;
    private List<SeriesInfo> allSeries;

    /**
       Construct a ScoreGraph that wraps a child score function.
       @param child The child score function.
    */
    public ScoreGraph(ScoreFunction child) {
        super("Score graph", child);
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        super.initialise(world, config);
        allSeries = new ArrayList<SeriesInfo>();
        XYSeriesCollection data = new XYSeriesCollection();
        createSeries(child, data);
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        chart = ChartFactory.createXYLineChart("Score", "Time", "Score", data, orientation, true, false, false);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        update(world, timestep);
        return super.score(world, timestep);
    }

    @Override
    public JComponent getGUIComponent() {
        JComponent selectionPanel = Box.createVerticalBox();
        final XYItemRenderer renderer = ((XYPlot)chart.getPlot()).getRenderer();
        for (SeriesInfo next : allSeries) {
            final ScoreFunction f = next.function;
            final int index = next.index;
            final JCheckBox checkBox = new JCheckBox(f.getName(), true);
            selectionPanel.add(checkBox);
            checkBox.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        boolean selected = checkBox.isSelected();
                        renderer.setSeriesVisible(index, selected);
                    }
                });
        }
        JPanel result = new JPanel();
        result.add(new ChartPanel(chart), BorderLayout.CENTER);
        result.add(selectionPanel, BorderLayout.EAST);
        return result;
    }

    @Override
    public String getGUIComponentName() {
        return "Score chart";
    }

    private void createSeries(ScoreFunction root, XYSeriesCollection data) {
        if (!(root instanceof ScoreTable || root instanceof ScoreGraph)) {
            XYSeries next = new XYSeries(root.getName());
            allSeries.add(new SeriesInfo(root, next, allSeries.size()));
            data.addSeries(next);
        }
        if (root instanceof DelegatingScoreFunction) {
            createSeries(((DelegatingScoreFunction)root).getChildFunction(), data);
        }
        if (root instanceof CompositeScoreFunction) {
            Set<ScoreFunction> children = ((CompositeScoreFunction)root).getChildFunctions();
            for (ScoreFunction f : children) {
                createSeries(f, data);
            }
        }
    }

    private void update(WorldModel<? extends Entity> world, Timestep timestep) {
        for (SeriesInfo next : allSeries) {
            ScoreFunction f = next.function;
            XYSeries data = next.series;
            double d = f.score(world, timestep);
            data.add(timestep.getTime(), d);
        }
    }

    private static class SeriesInfo {
        ScoreFunction function;
        XYSeries series;
        int index;

        public SeriesInfo(ScoreFunction function, XYSeries series, int index) {
            this.function = function;
            this.series = series;
            this.index = index;
        }
    }
}

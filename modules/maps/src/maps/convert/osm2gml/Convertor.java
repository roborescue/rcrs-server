package maps.convert.osm2gml;

import maps.osm.OSMMap;
import maps.gml.GMLMap;
import maps.convert.ConvertStep;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;

import java.util.List;
import java.util.ArrayList;

/**
   This class converts OSMMaps to GMLMaps.
*/
public class Convertor {
    private static final int PROGRESS_WIDTH  = 200;
    private static final int PROGRESS_HEIGHT = 10;
    private static final int STATUS_WIDTH    = 500;
    private static final int STATUS_HEIGHT   = 10;
    private static final int MARGIN          = 4;

    /**
       Convert an OSMMap to a GMLMap.
       @param map The OSMMap to convert.
       @return A new GMLMap.
    */
    public GMLMap convert(final OSMMap map) {
        final GMLMap gmlMap = new GMLMap();
        final TemporaryMap temp = new TemporaryMap(map);

        final JFrame frame = new JFrame("OSM to GML converter");
        final JPanel main  = new JPanel(new BorderLayout());

        // Build the header labels describing the source map.
        final JComponent top = Box.createVerticalBox();
        top.add(new JLabel("Converting OSM map with "
                + map.getRoads().size() + " roads and "
                + map.getBuildings().size() + " buildings"));
        top.add(new JLabel("Map size: "
                + (map.getMaxLongitude() - map.getMinLongitude()) +
                " x "
                + (map.getMaxLatitude() - map.getMinLatitude())));

        // Build the step rows and collect the ordered step list.
        final StepPanelBuilder builder = new StepPanelBuilder();
        final List<ConvertStep> steps = builder.addSteps(
                new CleanOSMStep                      (temp),
                new ScanOSMStep                       (temp),
                new RemovePseudoNodesStep             (temp),
                new GenerateIntersectionAreaStep      (temp),
                new MakeTempObjectsStep               (temp),
                new RemoveSelfIntersectingStep        (temp),
                new CreateEntranceStep                (temp),
                new SplitIntersectingEdgesStep        (temp),
                new CleanBuildingOverlapsStep         (temp),
                new MergePassableShapesStep           (temp),
                new FixConnectivityStep               (temp),
                new SplitNonTraversableObjectsStep    (temp),
                new SplitShapesStep                   (temp),
                new RemoveShapesStep                  (temp),
                new ComputePassableEdgesStep          (temp),
                new MakeObjectsStep                   (temp, gmlMap)
        );

        main.add(top, BorderLayout.NORTH);
        main.add(builder.getPanel(), BorderLayout.CENTER);

        frame.setContentPane(main);
        frame.pack();
        frame.setVisible(true);

        // Execute steps in order.
        for (final ConvertStep next : steps) {
            next.doStep();
        }

        return gmlMap;
    }

    /**
     * Builds a GridBag panel where each {@code ConvertStep} occupies one row:
     * [description label] [progress bar] [status component]
     */
    private static class StepPanelBuilder {
        private final JPanel             panel = new JPanel(new GridBagLayout());
        private final GridBagConstraints c     = new GridBagConstraints();

        StepPanelBuilder() {
            c.gridwidth  = 1;
            c.gridheight = 1;
            c.weightx    = 1;
            c.fill        = GridBagConstraints.BOTH;
            c.anchor      = GridBagConstraints.CENTER;
            c.insets      = new Insets(MARGIN, MARGIN, MARGIN, MARGIN);
            c.gridy       = 0;
        }

        // Append one row per step and return the steps in insertion order.
        List<ConvertStep> addSteps(final ConvertStep... steps) {
            final List<ConvertStep> ordered = new ArrayList<>();
            for (final ConvertStep step : steps) {
                addRow(step);
                ordered.add(step);
            }
            return ordered;
        }

        private void addRow(final ConvertStep step) {
            final JProgressBar progress = step.getProgressBar();
            final JComponent  status   = step.getStatusComponent();
            progress.setPreferredSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
            status  .setPreferredSize(new Dimension(STATUS_WIDTH  , STATUS_HEIGHT  ));

            addCell(new JLabel(step.getDescription()), 0, 1.0);
            addCell(progress                         , 1, 0.0);
            addCell(status                           , 2, 1.0);
            c.gridy++;
        }

        private void addCell(final JComponent component, final int gridX, final double weightX) {
            c.gridx   = gridX;
            c.weightx = weightX;
            panel.add(component, c);
        }

        JPanel getPanel() { return panel; }
    }
}

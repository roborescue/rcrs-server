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
    private static final int PROGRESS_WIDTH = 200;
    private static final int PROGRESS_HEIGHT = 10;
    private static final int STATUS_WIDTH = 500;
    private static final int STATUS_HEIGHT = 10;
    private static final int MARGIN = 4;

    /**
       Convert an OSMMap to a GMLMap.
       @param map The OSMMap to convert.
       @return A new GMLMap.
    */
    public GMLMap convert(OSMMap map) {
        GMLMap gmlMap = new GMLMap();

        JFrame frame = new JFrame("OSM to GML converter");
        JPanel main = new JPanel(new BorderLayout());
        JComponent top = Box.createVerticalBox();
        top.add(new JLabel("Converting OSM map with " + map.getRoads().size() + " roads and " + map.getBuildings().size() + " buildings"));
        top.add(new JLabel("Map size: " + (map.getMaxLongitude() - map.getMinLongitude()) + " x " + (map.getMaxLatitude() - map.getMinLatitude())));
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(MARGIN, MARGIN, MARGIN, MARGIN);
        JPanel progress = new JPanel(layout);

        //        Random random = new Random();

        TemporaryMap temp = new TemporaryMap(map);

        List<ConvertStep> steps = new ArrayList<ConvertStep>();
        addStep(new CleanOSMStep(temp), steps, progress, layout, c);
        addStep(new ScanOSMStep(temp), steps, progress, layout, c);
        addStep(new MakeTempObjectsStep(temp), steps, progress, layout, c);
        addStep(new SplitIntersectingEdgesStep(temp), steps, progress, layout, c);
        addStep(new SplitShapesStep(temp), steps, progress, layout, c);
        addStep(new RemoveShapesStep(temp), steps, progress, layout, c);
        addStep(new MergeShapesStep(temp), steps, progress, layout, c);
        addStep(new ComputePassableEdgesStep(temp), steps, progress, layout, c);
        /*
        addStep(new CreateBuildingsStep(temp, ConvertTools.sizeOf1Metre(osmMap), random), steps, progress, layout, c);
        addStep(new CreateEntrancesStep(temp), steps, progress, layout, c);
        addStep(new PruneStep(temp), steps, progress, layout, c);
        */
        addStep(new MakeObjectsStep(temp, gmlMap), steps, progress, layout, c);

        main.add(top);
        main.add(progress);

        frame.setContentPane(main);
        frame.pack();
        frame.setVisible(true);

        for (ConvertStep next : steps) {
            next.doStep();
        }

        return gmlMap;
    }

    private void addStep(ConvertStep step, List<ConvertStep> steps, JComponent panel, GridBagLayout layout, GridBagConstraints c) {
        JLabel title = new JLabel(step.getDescription());
        JProgressBar progress = step.getProgressBar();
        JComponent status = step.getStatusComponent();

        c.gridx = 0;
        c.weightx = 1;
        layout.setConstraints(title, c);
        panel.add(title);
        c.gridx = 1;
        c.weightx = 0;
        layout.setConstraints(progress, c);
        panel.add(progress);
        c.gridx = 2;
        c.weightx = 1;
        layout.setConstraints(status, c);
        panel.add(status);
        ++c.gridy;
        progress.setPreferredSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
        status.setPreferredSize(new Dimension(STATUS_WIDTH, STATUS_HEIGHT));

        steps.add(step);
    }
}

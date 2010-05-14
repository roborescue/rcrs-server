package maps.convert;

import maps.MapWriter;
import maps.osm.OSMMap;
import maps.osm.OSMMapViewer;
import maps.osm.OSMException;
import maps.gml.GMLMap;
import maps.gml.view.GMLMapViewer;

import maps.convert.osm2gml.Convertor;
import maps.gml.formats.RobocupFormat;

import org.dom4j.DocumentException;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.io.File;
import java.io.IOException;

/**
   This class converts maps from one format to another.
*/
public final class Convert {
    // Nodes that are close are deemed to be co-located.
    private static final double NEARBY_NODE_THRESHOLD = 0.000001;

    private static final int PROGRESS_WIDTH = 200;
    private static final int PROGRESS_HEIGHT = 10;
    private static final int VIEWER_SIZE = 500;
    private static final int STATUS_WIDTH = 500;
    private static final int STATUS_HEIGHT = 10;
    private static final int MARGIN = 4;

    //    private ShapeDebugFrame debug;
    //    private List<ShapeDebugFrame.ShapeInfo> allOSMNodes;
    //    private List<ShapeDebugFrame.ShapeInfo> allGMLNodes;

    private Convert() {
    }

    /**
       Run the map convertor.
       @param args Command line arguments: osm-mapname gml-mapname.
    */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Convert <osm-mapname> <gml-mapname>");
            return;
        }
        try {
            OSMMap osmMap = readOSMMap(args[0]);
            OSMMapViewer osmViewer = new OSMMapViewer(osmMap);
            Convertor convert = new Convertor();
            GMLMap gmlMap = convert.convert(osmMap);
            MapWriter.writeMap(gmlMap, args[1], RobocupFormat.INSTANCE);
            GMLMapViewer gmlViewer = new GMLMapViewer(gmlMap);
            JFrame frame = new JFrame("Convertor");
            JPanel main = new JPanel(new GridLayout(1, 2));
            osmViewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
            gmlViewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
            osmViewer.setBorder(BorderFactory.createTitledBorder("OSM map"));
            gmlViewer.setBorder(BorderFactory.createTitledBorder("GML map"));
            main.add(osmViewer);
            main.add(gmlViewer);
            frame.setContentPane(main);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        }
        // CHECKSTYLE:OFF:IllegalCatch
        catch (Exception e) {
            e.printStackTrace();
        }
        // CHECKSTYLE:ON:IllegalCatch
    }

    private static OSMMap readOSMMap(String file) throws OSMException, IOException, DocumentException {
        File f = new File(file);
        return new OSMMap(f);
    }
}

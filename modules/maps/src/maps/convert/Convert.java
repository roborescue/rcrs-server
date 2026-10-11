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
    private static final int VIEWER_SIZE = 500;

    private Convert() {
    }

    /**
       Run the map convertor.
       @param args Command line arguments: osm-mapname gml-mapname.
    */
    public static void main(String[] args) {
        String osmFile = null;
        String gmlFile = null;
        boolean showGui = false;

        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        for (String arg : args) {
            if ("--gui".equals(arg)) {
                showGui = true;
            }
            else if (osmFile == null) {
                osmFile = arg;
            }
            else if (gmlFile == null) {
                gmlFile = arg;
            }
            else {
                System.out.println("unrecognized argument: " + arg);
                printUsage();
                System.exit(1);
            }
        }

        if (osmFile == null || gmlFile == null) {
            printUsage();
            System.exit(1);
        }

        ConvertStep.setGuiEnabled(showGui);

        try {
            OSMMap osmMap = readOSMMap(osmFile);
            Convertor convert = new Convertor();
            GMLMap gmlMap = convert.convert(osmMap);
            MapWriter.writeMap(gmlMap, gmlFile, RobocupFormat.INSTANCE);
            System.out.println("Wrote " + gmlFile);
            if (showGui) {
                showViewer(osmMap, gmlMap);
            } else {
                System.exit(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: ./gradlew osm2gml --args=\"[--gui] '<osm map path>' '<gml map path>'\"");
        System.out.println("  --gui    Show a window comparing the OSM and GML maps after conversion.");
        System.out.println("           Without this flag the tool runs headless (CUI) and exits when done.");
    }

    private static void showViewer(OSMMap osmMap, GMLMap gmlMap) {
        OSMMapViewer osmViewer = new OSMMapViewer(osmMap);
        osmViewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
        osmViewer.setBorder(BorderFactory.createTitledBorder("OSM map"));

        GMLMapViewer gmlViewer = new GMLMapViewer(gmlMap);
        gmlViewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
        gmlViewer.setBorder(BorderFactory.createTitledBorder("GML map"));

        JPanel main = new JPanel(new GridLayout(1, 2));
        main.add(osmViewer);
        main.add(gmlViewer);

        JFrame frame = new JFrame("Convertor");
        frame.setContentPane(main);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private static OSMMap readOSMMap(String file) throws OSMException, IOException, DocumentException {
        File f = new File(file);
        return new OSMMap(f);
    }
}

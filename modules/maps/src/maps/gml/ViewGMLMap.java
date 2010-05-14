package maps.gml;

import maps.MapReader;
import maps.gml.view.GMLMapViewer;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
   A GML map viewer.
*/
public final class ViewGMLMap {
    private static final int VIEWER_SIZE = 500;

    private ViewGMLMap() {
    }

    /**
       Start the viewer.
       @param args Command-line arguments: mapname.
    */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ViewGMLMap <mapname>");
            return;
        }
        try {
            GMLMap map = (GMLMap)MapReader.readMap(args[0]);
            GMLMapViewer gmlViewer = new GMLMapViewer(map);
            JFrame frame = new JFrame("GML Map");
            gmlViewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
            frame.setContentPane(gmlViewer);
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
}

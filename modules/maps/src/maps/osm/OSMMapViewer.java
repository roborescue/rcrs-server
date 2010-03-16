package maps.osm;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.PanZoomListener;

/**
   A component for viewing OSM maps.
*/
public class OSMMapViewer extends JComponent {
    private OSMMap map;
    private ScreenTransform transform;
    private PanZoomListener panZoom;

    /**
       Create an OSMMapViewer.
    */
    public OSMMapViewer() {
        this(null);
    }

    /**
       Create an OSMMapViewer.
       @param map The map to view.
    */
    public OSMMapViewer(final OSMMap map) {
        panZoom = new PanZoomListener(this);
        setMap(map);
        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Point p = e.getPoint();
                        double lon = transform.screenToX(p.x);
                        double lat = transform.screenToY(p.y);
                        OSMNode node = map.getNearestNode(lat, lon);
                        System.out.println("Click at " + lat + ", " + lon);
                        System.out.println("Nearest node: " + node);
                    }
                }
            });
    }

    /**
       Set the map.
       @param map The new map to view.
    */
    public void setMap(OSMMap map) {
        this.map = map;
        transform = null;
        if (map != null) {
            transform = new ScreenTransform(map.getMinLongitude(), map.getMinLatitude(), map.getMaxLongitude(), map.getMaxLatitude());
        }
        panZoom.setScreenTransform(transform);
    }

    /**
       Get the latitude of a screen coordinate.
       @param y The screen coordinate.
       @return The latitude at that coordinate.
    */
    public double getLatitude(int y) {
        return transform.screenToY(y);
    }

    /**
       Get the longitude of a screen coordinate.
       @param x The screen coordinate.
       @return The longitude at that coordinate.
    */
    public double getLongitude(int x) {
        return transform.screenToX(x);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (map == null) {
            return;
        }
        Insets insets = getInsets();
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        Graphics2D g = (Graphics2D)graphics.create(insets.left, insets.top, width + 1 , height + 1);
        transform.rescale(width, height);
        g.setColor(Color.black);
        for (OSMNode next : map.getNodes()) {
            int x = transform.xToScreen(next.getLongitude());
            int y = transform.yToScreen(next.getLatitude());
            g.drawLine(x - 1, y - 1, x + 1, y + 1);
            g.drawLine(x + 1, y - 1, x - 1, y + 1);
        }
        for (OSMRoad next : map.getRoads()) {
            int lastX = -1;
            int lastY = -1;
            for (Long nodeID : next.getNodeIDs()) {
                OSMNode node = map.getNode(nodeID);
                int x = transform.xToScreen(node.getLongitude());
                int y = transform.yToScreen(node.getLatitude());
                if (lastX != -1) {
                    g.drawLine(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
            }
        }
        g.setColor(Color.blue);
        for (OSMBuilding next : map.getBuildings()) {
            int lastX = -1;
            int lastY = -1;
            for (Long nodeID : next.getNodeIDs()) {
                OSMNode node = map.getNode(nodeID);
                int x = transform.xToScreen(node.getLongitude());
                int y = transform.yToScreen(node.getLatitude());
                if (lastX != -1) {
                    g.drawLine(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
            }
        }
    }
}

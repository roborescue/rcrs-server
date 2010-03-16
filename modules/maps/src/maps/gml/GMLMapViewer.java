package maps.gml;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Polygon;
import javax.swing.JComponent;

import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.PanZoomListener;

/**
   A component for viewing GML maps.
*/
public class GMLMapViewer extends JComponent {
    // CHECKSTYLE:OFF:JavadocVariable
    public static final Color BUILDING_COLOUR = new Color(0, 255, 0, 128); // Transparent lime
    public static final Color INTERSECTION_COLOUR = new Color(192, 192, 192, 128); // Transparent silver
    public static final Color ROAD_COLOUR = new Color(128, 128, 128, 128); // Transparent gray
    public static final Color SPACE_COLOUR = new Color(0, 128, 0, 128); // Transparent green
    public static final Color OUTLINE_COLOUR = Color.BLACK;
    // CHECKSTYLE:ON:JavadocVariable

    //    private static final Stroke PASSABLE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {1, 1}, 0);
    //    private static final Stroke PASSABLE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    //    private static final Stroke IMPASSABLE_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    //    private static final Color PASSABLE_COLOUR = new Color(0, 0, 255); // Blue
    //    private static final Color IMPASSABLE_COLOUR = new Color(0, 0, 128); // Navy

    private GMLMap map;
    private ScreenTransform transform;
    private PanZoomListener panZoom;

    /**
       Create a GMLMapViewer.
    */
    public GMLMapViewer() {
        this(null);
    }

    /**
       Create a GMLMapViewer.
       @param map The map to view.
    */
    public GMLMapViewer(GMLMap map) {
        panZoom = new PanZoomListener(this);
        setMap(map);
    }

    /**
       Set the map.
       @param map The map to view.
    */
    public void setMap(GMLMap map) {
        this.map = map;
        transform = null;
        if (map != null) {
            transform = new ScreenTransform(map.getMinX(), map.getMinY(), map.getMaxX(), map.getMaxY());
        }
        panZoom.setScreenTransform(transform);
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
        for (GMLRoad next : map.getRoads()) {
            paint(next, g, ROAD_COLOUR);
        }
        for (GMLBuilding next : map.getBuildings()) {
            paint(next, g, BUILDING_COLOUR);
        }
        for (GMLSpace next : map.getSpaces()) {
            paint(next, g, SPACE_COLOUR);
        }
    }

    private void paint(GMLShape shape, Graphics2D g, Color fill) {
        Polygon p = makePolygon(shape);
        g.setColor(OUTLINE_COLOUR);
        g.draw(p);
        g.setColor(fill);
        g.fill(p);
    }

    private Polygon makePolygon(GMLShape shape) {
        List<GMLCoordinates> c = shape.getCoordinates();
        int[] xs = new int[c.size()];
        int[] ys = new int[c.size()];
        int i = 0;
        for (GMLCoordinates next : c) {
            xs[i] = transform.xToScreen(next.getX());
            ys[i] = transform.yToScreen(next.getY());
            ++i;
        }
        return new Polygon(xs, ys, c.size());
    }
}

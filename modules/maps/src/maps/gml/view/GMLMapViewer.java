package maps.gml.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.PanZoomListener;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLSpace;
import maps.gml.GMLObject;
import maps.gml.GMLCoordinates;
import maps.gml.GMLTools;

/**
   A component for viewing GML maps.
*/
public class GMLMapViewer extends JComponent {
    private static final Color BUILDING_COLOUR = new Color(67, 67, 67, 67); // Transparent dark gray
    private static final Color ROAD_COLOUR = new Color(192, 192, 192, 128); // Transparent light gray
    private static final Color SPACE_COLOUR = new Color(0, 128, 0, 128); // Transparent green

    private static final Color GRID_COLOUR = new Color(0, 255, 0, 128); // Transparent lime

    private static final Color NODE_COLOUR = Color.BLACK;
    private static final int NODE_SIZE = 3;

    private static final Color EDGE_COLOUR = Color.BLACK;

    private static final double MIN_ZOOM_BOUNDS_SIZE = 0.1;

    private GMLMap map;
    private ScreenTransform transform;
    private PanZoomListener panZoom;

    private transient NodeDecorator defaultNodeDecorator;
    private transient Map<GMLNode, NodeDecorator> nodeDecorators;

    private transient EdgeDecorator defaultEdgeDecorator;
    private transient Map<GMLEdge, EdgeDecorator> edgeDecorators;

    private transient BuildingDecorator defaultBuildingDecorator;
    private transient Map<GMLBuilding, BuildingDecorator> buildingDecorators;

    private transient RoadDecorator defaultRoadDecorator;
    private transient Map<GMLRoad, RoadDecorator> roadDecorators;

    private transient SpaceDecorator defaultSpaceDecorator;
    private transient Map<GMLSpace, SpaceDecorator> spaceDecorators;

    private transient List<Overlay> overlays;

    private boolean grid;
    private double gridResolution;
	private boolean paintNodes=true;

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
        defaultNodeDecorator = new CrossNodeDecorator(NODE_COLOUR, NODE_SIZE);
        defaultEdgeDecorator = new LineEdgeDecorator(EDGE_COLOUR);
        FilledShapeDecorator d = new FilledShapeDecorator(BUILDING_COLOUR, ROAD_COLOUR, SPACE_COLOUR);
        defaultBuildingDecorator = d;
        defaultRoadDecorator = d;
        defaultSpaceDecorator = d;
        nodeDecorators = new HashMap<GMLNode, NodeDecorator>();
        edgeDecorators = new HashMap<GMLEdge, EdgeDecorator>();
        buildingDecorators = new HashMap<GMLBuilding, BuildingDecorator>();
        roadDecorators = new HashMap<GMLRoad, RoadDecorator>();
        spaceDecorators = new HashMap<GMLSpace, SpaceDecorator>();
        grid = false;
        gridResolution = 1;
        overlays = new ArrayList<Overlay>();
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
            if (!map.hasSize()) {
                // CHECKSTYLE:OFF:MagicNumber
                transform = new ScreenTransform(0, 0, 100, 100);
                // CHECKSTYLE:ON:MagicNumber
            }
            else {
                transform = new ScreenTransform(map.getMinX(), map.getMinY(), map.getMaxX(), map.getMaxY());
            }
        }
        panZoom.setScreenTransform(transform);
    }

    /**
       View a particular set of objects.
       @param objects The objects to view.
    */
    public void view(GMLObject... objects) {
        view(Arrays.asList(objects));
    }

    /**
       View a particular set of objects.
       @param objects The objects to view.
    */
    public void view(List<? extends GMLObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }
        Rectangle2D bounds = GMLTools.getObjectBounds(objects);
        if (bounds == null) {
            return;
        }
        if (bounds.getWidth() < MIN_ZOOM_BOUNDS_SIZE) {
            bounds = new Rectangle2D.Double(bounds.getX() - MIN_ZOOM_BOUNDS_SIZE / 2,
                    bounds.getY(), MIN_ZOOM_BOUNDS_SIZE, bounds.getHeight());
        }
        if (bounds.getHeight() < MIN_ZOOM_BOUNDS_SIZE) {
            bounds = new Rectangle2D.Double(bounds.getX(),
                    bounds.getY() - MIN_ZOOM_BOUNDS_SIZE / 2,
                    bounds.getWidth(), MIN_ZOOM_BOUNDS_SIZE);
        }
        transform.show(bounds);
    }

    /**
       View all objects.
    */
    public void viewAll() {
        transform.resetZoom();
    }

    /**
       Get the PanZoomListener for this component.
       @return The PanZoomListener.
    */
    public PanZoomListener getPanZoomListener() {
        return panZoom;
    }

    /**
       Set the default node decorator.
       @param defaultDecorator The new default node decorator.
    */
    public void setDefaultNodeDecorator(NodeDecorator defaultDecorator) {
        defaultNodeDecorator = defaultDecorator;
    }

    /**
       Get the default node decorator.
       @return The default node decorator.
    */
    public NodeDecorator getDefaultNodeDecorator() {
        return defaultNodeDecorator;
    }

    /**
       Set the NodeDecorator for a set of GMLNodes.
       @param decorator The decorator to set.
       @param nodes The nodes to set the decorator for.
    */
    public void setNodeDecorator(NodeDecorator decorator, GMLNode... nodes) {
        setNodeDecorator(decorator, Arrays.asList(nodes));
    }

    /**
       Set the NodeDecorator for a set of GMLNodes.
       @param decorator The decorator to set.
       @param nodes The nodes to set the decorator for.
    */
    public void setNodeDecorator(NodeDecorator decorator, Collection<? extends GMLNode> nodes) {
        for (GMLNode next : nodes) {
            nodeDecorators.put(next, decorator);
        }
    }

    /**
       Get the NodeDecorator for a GMLNodes.
       @param node The node to look up.
       @return The NodeDecorator for that node. This will be the default decorator if no custom decorator has been set for that node.
    */
    public NodeDecorator getNodeDecorator(GMLNode node) {
        NodeDecorator result = nodeDecorators.get(node);
        if (result == null) {
            result = defaultNodeDecorator;
        }
        return result;
    }

    /**
       Remove any custom NodeDecorator for a set of GMLNodes.
       @param nodes The nodes to remove any custom decorator for.
    */
    public void clearNodeDecorator(GMLNode... nodes) {
        clearNodeDecorator(Arrays.asList(nodes));
    }

    /**
       Remove any custom NodeDecorator for a set of GMLNodes.
       @param nodes The nodes to remove any custom decorator for.
    */
    public void clearNodeDecorator(Collection<? extends GMLNode> nodes) {
        for (GMLNode next : nodes) {
            nodeDecorators.remove(next);
        }
    }

    /**
       Remove any custom NodeDecorators.
    */
    public void clearAllNodeDecorators() {
        nodeDecorators.clear();
    }

    /**
       Set the default edge decorator.
       @param defaultDecorator The new default edge decorator.
    */
    public void setDefaultEdgeDecorator(EdgeDecorator defaultDecorator) {
        defaultEdgeDecorator = defaultDecorator;
    }

    /**
       Get the default edge decorator.
       @return The default edge decorator.
    */
    public EdgeDecorator getDefaultEdgeDecorator() {
        return defaultEdgeDecorator;
    }

    /**
       Set the EdgeDecorator for a set of GMLEdges.
       @param decorator The decorator to set.
       @param edges The edges to set the decorator for.
    */
    public void setEdgeDecorator(EdgeDecorator decorator, GMLEdge... edges) {
        setEdgeDecorator(decorator, Arrays.asList(edges));
    }

    /**
       Set the EdgeDecorator for a set of GMLEdges.
       @param decorator The decorator to set.
       @param edges The edges to set the decorator for.
    */
    public void setEdgeDecorator(EdgeDecorator decorator, Collection<? extends GMLEdge> edges) {
        for (GMLEdge next : edges) {
            edgeDecorators.put(next, decorator);
        }
    }

    /**
       Get the EdgeDecorator for a GMLEdge.
       @param edge The edge to look up.
       @return The EdgeDecorator for that edge. This will be the default decorator if no custom decorator has been set for that edge.
    */
    public EdgeDecorator getEdgeDecorator(GMLEdge edge) {
        EdgeDecorator result = edgeDecorators.get(edge);
        if (result == null) {
            result = defaultEdgeDecorator;
        }
        return result;
    }

    /**
       Remove any custom EdgeDecorator for a set of GMLEdges.
       @param edges The edges to remove any custom decorator for.
    */
    public void clearEdgeDecorator(GMLEdge... edges) {
        clearEdgeDecorator(Arrays.asList(edges));
    }

    /**
       Remove any custom EdgeDecorator for a set of GMLEdges.
       @param edges The edges to remove any custom decorator for.
    */
    public void clearEdgeDecorator(Collection<? extends GMLEdge> edges) {
        for (GMLEdge next : edges) {
            edgeDecorators.remove(next);
        }
    }

    /**
       Remove any custom EdgeDecorators.
    */
    public void clearAllEdgeDecorators() {
        edgeDecorators.clear();
    }

    /**
       Set the default building decorator.
       @param defaultDecorator The new default building decorator.
    */
    public void setDefaultBuildingDecorator(BuildingDecorator defaultDecorator) {
        defaultBuildingDecorator = defaultDecorator;
    }

    /**
       Get the default building decorator.
       @return The default building decorator.
    */
    public BuildingDecorator getDefaultBuildingDecorator() {
        return defaultBuildingDecorator;
    }

    /**
       Set the BuildingDecorator for a set of GMLBuildings.
       @param decorator The decorator to set.
       @param buildings The buildings to set the decorator for.
    */
    public void setBuildingDecorator(BuildingDecorator decorator, GMLBuilding... buildings) {
        setBuildingDecorator(decorator, Arrays.asList(buildings));
    }

    /**
       Set the BuildingDecorator for a set of GMLBuildings.
       @param decorator The decorator to set.
       @param buildings The buildings to set the decorator for.
    */
    public void setBuildingDecorator(BuildingDecorator decorator, Collection<? extends GMLBuilding> buildings) {
        for (GMLBuilding next : buildings) {
            buildingDecorators.put(next, decorator);
        }
    }

    /**
       Get the BuildingDecorator for a GMLBuildings.
       @param building The building to look up.
       @return The BuildingDecorator for that building. This will be the default decorator if no custom decorator has been set for that building.
    */
    public BuildingDecorator getBuildingDecorator(GMLBuilding building) {
        BuildingDecorator result = buildingDecorators.get(building);
        if (result == null) {
            result = defaultBuildingDecorator;
        }
        return result;
    }

    /**
       Remove any custom BuildingDecorator for a set of GMLBuildings.
       @param buildings The buildings to remove any custom decorator for.
    */
    public void clearBuildingDecorator(GMLBuilding... buildings) {
        clearBuildingDecorator(Arrays.asList(buildings));
    }

    /**
       Remove any custom BuildingDecorator for a set of GMLBuildings.
       @param buildings The buildings to remove any custom decorator for.
    */
    public void clearBuildingDecorator(Collection<? extends GMLBuilding> buildings) {
        for (GMLBuilding next : buildings) {
            buildingDecorators.remove(next);
        }
    }

    /**
       Remove any custom BuildingDecorators.
    */
    public void clearAllBuildingDecorators() {
        buildingDecorators.clear();
    }

    /**
       Set the default road decorator.
       @param defaultDecorator The new default road decorator.
    */
    public void setDefaultRoadDecorator(RoadDecorator defaultDecorator) {
        defaultRoadDecorator = defaultDecorator;
    }

    /**
       Get the default road decorator.
       @return The default road decorator.
    */
    public RoadDecorator getDefaultRoadDecorator() {
        return defaultRoadDecorator;
    }

    /**
       Set the RoadDecorator for a set of GMLRoads.
       @param decorator The decorator to set.
       @param roads The roads to set the decorator for.
    */
    public void setRoadDecorator(RoadDecorator decorator, GMLRoad... roads) {
        setRoadDecorator(decorator, Arrays.asList(roads));
    }

    /**
       Set the RoadDecorator for a set of GMLRoads.
       @param decorator The decorator to set.
       @param roads The roads to set the decorator for.
    */
    public void setRoadDecorator(RoadDecorator decorator, Collection<? extends GMLRoad> roads) {
        for (GMLRoad next : roads) {
            roadDecorators.put(next, decorator);
        }
    }

    /**
       Get the RoadDecorator for a GMLRoads.
       @param road The road to look up.
       @return The RoadDecorator for that road. This will be the default decorator if no custom decorator has been set for that road.
    */
    public RoadDecorator getRoadDecorator(GMLRoad road) {
        RoadDecorator result = roadDecorators.get(road);
        if (result == null) {
            result = defaultRoadDecorator;
        }
        return result;
    }

    /**
       Remove any custom RoadDecorator for a set of GMLRoads.
       @param roads The roads to remove any custom decorator for.
    */
    public void clearRoadDecorator(GMLRoad... roads) {
        clearRoadDecorator(Arrays.asList(roads));
    }

    /**
       Remove any custom RoadDecorator for a set of GMLRoads.
       @param roads The roads to remove any custom decorator for.
    */
    public void clearRoadDecorator(Collection<? extends GMLRoad> roads) {
        for (GMLRoad next : roads) {
            roadDecorators.remove(next);
        }
    }

    /**
       Remove any custom RoadDecorators.
    */
    public void clearAllRoadDecorators() {
        roadDecorators.clear();
    }

    /**
       Set the default space decorator.
       @param defaultDecorator The new default space decorator.
    */
    public void setDefaultSpaceDecorator(SpaceDecorator defaultDecorator) {
        defaultSpaceDecorator = defaultDecorator;
    }

    /**
       Get the default space decorator.
       @return The default space decorator.
    */
    public SpaceDecorator getDefaultSpaceDecorator() {
        return defaultSpaceDecorator;
    }

    /**
       Set the SpaceDecorator for a set of GMLSpaces.
       @param decorator The decorator to set.
       @param spaces The spaces to set the decorator for.
    */
    public void setSpaceDecorator(SpaceDecorator decorator, GMLSpace... spaces) {
        setSpaceDecorator(decorator, Arrays.asList(spaces));
    }

    /**
       Set the SpaceDecorator for a set of GMLSpaces.
       @param decorator The decorator to set.
       @param spaces The spaces to set the decorator for.
    */
    public void setSpaceDecorator(SpaceDecorator decorator, Collection<? extends GMLSpace> spaces) {
        for (GMLSpace next : spaces) {
            spaceDecorators.put(next, decorator);
        }
    }

    /**
       Get the SpaceDecorator for a GMLSpaces.
       @param space The space to look up.
       @return The SpaceDecorator for that space. This will be the default decorator if no custom decorator has been set for that space.
    */
    public SpaceDecorator getSpaceDecorator(GMLSpace space) {
        SpaceDecorator result = spaceDecorators.get(space);
        if (result == null) {
            result = defaultSpaceDecorator;
        }
        return result;
    }

    /**
       Remove any custom SpaceDecorator for a set of GMLSpaces.
       @param spaces The spaces to remove any custom decorator for.
    */
    public void clearSpaceDecorator(GMLSpace... spaces) {
        clearSpaceDecorator(Arrays.asList(spaces));
    }

    /**
       Remove any custom SpaceDecorator for a set of GMLSpaces.
       @param spaces The spaces to remove any custom decorator for.
    */
    public void clearSpaceDecorator(Collection<? extends GMLSpace> spaces) {
        for (GMLSpace next : spaces) {
            spaceDecorators.remove(next);
        }
    }

    /**
       Remove any custom SpaceDecorators.
    */
    public void clearAllSpaceDecorators() {
        spaceDecorators.clear();
    }

    /**
       Set whether to draw the grid or not.
       @param b True to draw the grid.
    */
    public void setGridEnabled(boolean b) {
        grid = b;
    }

    /**
       Set the grid resolution.
       @param resolution The new grid resolution.
    */
    public void setGridResolution(double resolution) {
        gridResolution = resolution;
    }

    /**
       Add an overlay to the view.
       @param overlay The overlay to add.
    */
    public void addOverlay(Overlay overlay) {
        overlays.add(overlay);
    }

    /**
       Remove an overlay from the view.
       @param overlay The overlay to remove.
    */
    public void removeOverlay(Overlay overlay) {
        overlays.remove(overlay);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics copy = graphics.create();
        copy.setColor(getBackground());
        copy.fillRect(0, 0, getWidth(), getHeight());
        if (map == null) {
            return;
        }
        Insets insets = getInsets();
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        Graphics2D g = (Graphics2D)graphics.create(insets.left, insets.top, width + 1 , height + 1);
        transform.rescale(width, height);
        Collection<GMLRoad> roads;
        Collection<GMLBuilding> buildings;
        Collection<GMLSpace> spaces;
        Collection<GMLEdge> edges;
        Collection<GMLNode> nodes;
        synchronized (map) {
            roads = new HashSet<GMLRoad>(map.getRoads());
            buildings = new HashSet<GMLBuilding>(map.getBuildings());
            spaces = new HashSet<GMLSpace>(map.getSpaces());
            edges = new HashSet<GMLEdge>(map.getEdges());
            nodes = new HashSet<GMLNode>(map.getNodes());
        }
        for (GMLRoad next : roads) {
            RoadDecorator d = getRoadDecorator(next);
            if (d != null) {
                d.decorate(next, (Graphics2D)g.create(), transform);
            }
        }
        for (GMLBuilding next : buildings) {
            BuildingDecorator d = getBuildingDecorator(next);
            if (d != null) {
                d.decorate(next, (Graphics2D)g.create(), transform);
            }
        }
        for (GMLSpace next : spaces) {
            SpaceDecorator d = getSpaceDecorator(next);
            if (d != null) {
                d.decorate(next, (Graphics2D)g.create(), transform);
            }
        }
        for (GMLEdge next : edges) {
            EdgeDecorator e = getEdgeDecorator(next);
            if (e != null) {
                e.decorate(next, (Graphics2D)g.create(), transform);
            }
        }
        for (GMLNode next : nodes) {
            NodeDecorator n = getNodeDecorator(next);
            if (paintNodes&&n != null) {
                n.decorate(next, (Graphics2D)g.create(), transform);
            }
        }
        for (Overlay next : overlays) {
            next.render((Graphics2D)g.create(), transform);
        }
        if (grid) {
            double xMin = roundDownToGrid(transform.screenToX(0));
            double xMax = roundUpToGrid(transform.screenToX(width));
            double yMin = roundDownToGrid(transform.screenToY(height));
            double yMax = roundUpToGrid(transform.screenToY(0));
            g.setColor(GRID_COLOUR);
            for (double worldX = xMin; worldX <= xMax; worldX += gridResolution) {
                int x = transform.xToScreen(worldX);
                g.drawLine(x, 0, x, height);
            }
            for (double worldY = yMin; worldY <= yMax; worldY += gridResolution) {
                int y = transform.yToScreen(worldY);
                g.drawLine(0, y, width, y);
            }
        }
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    /**
       Enable or disable the pan/zoom feature.
       @param enabled Whether pan/zoom should be enabled or not.
    */
    public void setPanZoomEnabled(boolean enabled) {
        panZoom.setEnabled(enabled);
    }

    /**
       Get the coordinates of a point on screen.
       @param x The screen x coordinate.
       @param y The screen y coordinate.
       @return The coordinates in the GML map under the screen point.
    */
    public GMLCoordinates getCoordinatesAtPoint(int x, int y) {
        double cx = transform.screenToX(x);
        double cy = transform.screenToY(y);
        return new GMLCoordinates(cx, cy);
    }

    /**
       Get the on-screen coordinates for a point.
       @param c The GML coordinates to look up.
       @return The on-screen coordinates of the point.
    */
    public Point getScreenCoordinates(GMLCoordinates c) {
        int x = transform.xToScreen(c.getX());
        int y = transform.yToScreen(c.getY());
        return new Point(x, y);
    }

    private double roundDownToGrid(double d) {
        return Math.floor(d / gridResolution) * gridResolution;
    }

    private double roundUpToGrid(double d) {
        return Math.ceil(d / gridResolution) * gridResolution;
    }

    public void setPaintNodes(boolean paintNodes) {
		this.paintNodes = paintNodes;
		
	}
}

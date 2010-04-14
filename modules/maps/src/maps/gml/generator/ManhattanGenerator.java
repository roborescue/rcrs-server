package maps.gml.generator;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
//import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLBuilding;
//import maps.gml.GMLRoad;
import maps.gml.GMLCoordinates;

import rescuecore2.config.Config;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.log.Logger;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.maths.random.ContinuousUniformGenerator;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
   A MapGenerator that generates a grid world.
 */
public class ManhattanGenerator implements MapGenerator {
    private static final String GRID_WIDTH_KEY = "generator.manhattan.grid.width";
    private static final String GRID_HEIGHT_KEY = "generator.manhattan.grid.height";
    private static final String GRID_SIZE_KEY = "generator.manhattan.grid.size";
    private static final String ROAD_WIDTH_KEY = "generator.manhattan.road.width";
    private static final String BUILDING_WIDTH_MIN_KEY = "generator.manhattan.building.width.min";
    private static final String BUILDING_HEIGHT_MIN_KEY = "generator.manhattan.building.height.min";
    private static final String BUILDING_SEPARATION_MIN_KEY = "generator.manhattan.building.separation.min";
    private static final String BUILDING_SEPARATION_MAX_KEY = "generator.manhattan.building.separation.max";
    private static final String BUILDING_MIN_SIZE_KEY = "generator.manhattan.building.split.min-size";
    private static final String BUILDING_MAX_SIZE_KEY = "generator.manhattan.building.split.max-size";
    private static final String BUILDING_SPLIT_CHANCE_KEY = "generator.manhattan.building.split.chance";

    private Config config;
    //    private NumberGenerator<Double> widthGenerator;
    //    private NumberGenerator<Double> heightGenerator;
    private NumberGenerator<Double> separationGenerator;
    private Probability split;
    private double minSize;
    private double maxSize;
    private double minWidth;
    private double minHeight;

    private GMLMap map;

    /**
       Construct a ManhattanGenerator.
       @param config The configuration to use.
    */
    public ManhattanGenerator(Config config) {
        this.config = config;
        //        widthGenerator = new ContinuousUniformGenerator(config.getFloatValue(BUILDING_WIDTH_MIN_KEY), config.getFloatValue(BUILDING_WIDTH_MAX_KEY), config.getRandom());
        //        heightGenerator = new ContinuousUniformGenerator(config.getFloatValue(BUILDING_HEIGHT_MIN_KEY), config.getFloatValue(BUILDING_HEIGHT_MAX_KEY), config.getRandom());
        //        Logger.debug("separation min: " + config.getFloatValue(BUILDING_SEPARATION_MIN_KEY));
        //        Logger.debug("separation max: " + config.getFloatValue(BUILDING_SEPARATION_MAX_KEY));
        separationGenerator = new ContinuousUniformGenerator(config.getFloatValue(BUILDING_SEPARATION_MIN_KEY), config.getFloatValue(BUILDING_SEPARATION_MAX_KEY), config.getRandom());
        //        Logger.debug("Generator: "+ separationGenerator);
        split = new Probability(config.getFloatValue(BUILDING_SPLIT_CHANCE_KEY));
        minSize = config.getFloatValue(BUILDING_MIN_SIZE_KEY);
        maxSize = config.getFloatValue(BUILDING_MAX_SIZE_KEY);
        minWidth = config.getFloatValue(BUILDING_WIDTH_MIN_KEY);
        minHeight = config.getFloatValue(BUILDING_HEIGHT_MIN_KEY);
    }

    @Override
    public void populate(GMLMap gmlMap) {
        this.map = gmlMap;
        int gridWidth = config.getIntValue(GRID_WIDTH_KEY);
        int gridHeight = config.getIntValue(GRID_HEIGHT_KEY);
        double gridSize = config.getIntValue(GRID_SIZE_KEY);
        double roadWidth = config.getIntValue(ROAD_WIDTH_KEY);
        Logger.debug("Generating manhattan map: grid size " + gridWidth + " x " + gridHeight);
        Logger.debug("Grid cell size: " + gridSize + "m");
        Logger.debug("Road width: " + roadWidth + "m");
        Collection<GMLBuilding> allBuildings = new ArrayList<GMLBuilding>();
        for (int gridX = 0; gridX < gridWidth; ++gridX) {
            for (int gridY = 0; gridY < gridHeight; ++gridY) {
                double cellXMin = (gridX * gridSize) + roadWidth;
                double cellYMin = (gridY * gridSize) + roadWidth;
                double cellXMax = ((gridX + 1) * gridSize) - roadWidth;
                double cellYMax = ((gridY + 1) * gridSize) - roadWidth;
                GMLBuilding base = createBuilding(cellXMin, cellYMin, cellXMax, cellYMax);
                allBuildings.addAll(divide(base));
            }
        }
        map.removeAllNodes();
        map.removeAllEdges();
        map.removeAllBuildings();
        for (GMLBuilding next : allBuildings) {
            map.add(next);
            for (GMLDirectedEdge edge : next.getEdges()) {
                map.add(edge.getEdge());
                map.add(edge.getEdge().getStart());
                map.add(edge.getEdge().getEnd());
            }
        }
    }

    private Collection<GMLBuilding> divide(GMLBuilding b) {
        //        Logger.debug("Possibly dividing building " + b);
        Collection<GMLBuilding> result = new HashSet<GMLBuilding>();
        List<Point2D> vertices = coordinatesToVertices(b.getUnderlyingCoordinates());
        double area = GeometryTools2D.computeArea(vertices);
        //        Logger.debug("Area: " + area + " sqm");
        if (area <= minSize) {
            result.add(b);
        }
        else {
            if (area > maxSize || split.nextEvent(config.getRandom())) {
                // Split the building
                double xMin = b.getBounds().getMinX();
                double xMax = b.getBounds().getMaxX();
                double yMin = b.getBounds().getMinY();
                double yMax = b.getBounds().getMaxY();
                double width = xMax - xMin;
                double height = yMax - yMin;
                //                Logger.debug("Width: " + width);
                //                Logger.debug("Height: " + height);
                //                Logger.debug("width * height = " + (width * height));
                //                Logger.debug("Area           = " + area);
                if (height > width) {
                    //                    Logger.debug("Splitting horizontally");
                    // Horizontal split
                    double splitY = (yMax + yMin) / 2;
                    double topOffset = separationGenerator.nextValue();
                    double bottomOffset = separationGenerator.nextValue();
                    double topY = splitY + topOffset;
                    double bottomY = splitY - bottomOffset;
                    //                    Logger.debug("yMin = " + yMin);
                    //                    Logger.debug("yMax = " + yMax);
                    //                    Logger.debug("splitY = " + splitY);
                    //                    Logger.debug("topOffset = " + topOffset);
                    //                    Logger.debug("bottomOffset = " + bottomOffset);
                    //                    Logger.debug("topY = " + topY);
                    //                    Logger.debug("bottomY = " + bottomY);
                    if (yMax - topY < minHeight || bottomY - yMin < minHeight) {
                        //                        Logger.debug("Split too thin");
                        //                        Logger.debug("Top piece: " + (yMax - topY));
                        //                        Logger.debug("Bottom piece: " + (bottomY - yMin));
                        //                        Logger.debug("Minimum height: " + minHeight);
                        result.add(b);
                    }
                    else {
                        result.addAll(divide(createBuilding(xMin, yMin, xMax, bottomY)));
                        result.addAll(divide(createBuilding(xMin, topY, xMax, yMax)));
                    }
                }
                else {
                    //                    Logger.debug("Splitting vertically");
                    // Vertical split
                    double splitX = (xMax + xMin) / 2;
                    double leftOffset = separationGenerator.nextValue();
                    double rightOffset = separationGenerator.nextValue();
                    double leftX = splitX - leftOffset;
                    double rightX = splitX + rightOffset;
                    //                    Logger.debug("xMin = " + xMin);
                    //                    Logger.debug("xMax = " + xMax);
                    //                    Logger.debug("splitX = " + splitX);
                    //                    Logger.debug("leftOffset = " + leftOffset);
                    //                    Logger.debug("rightOffset = " + rightOffset);
                    //                    Logger.debug("leftX = " + leftX);
                    //                    Logger.debug("rightX = " + rightX);
                    if (xMax - rightX < minWidth || leftX - xMin < minWidth) {
                        //                        Logger.debug("Split too thin");
                        //                        Logger.debug("Left piece: " + (leftX - xMin));
                        //                        Logger.debug("Right piece: " + (xMax - rightX));
                        //                        Logger.debug("Minimum width: " + minWidth);
                        result.add(b);
                    }
                    else {
                        result.addAll(divide(createBuilding(xMin, yMin, leftX, yMax)));
                        result.addAll(divide(createBuilding(rightX, yMin, xMax, yMax)));
                    }
                }
            }
            else {
                //                Logger.debug("Not splitting");
                result.add(b);
            }
        }
        return result;
    }

    private List<Point2D> coordinatesToVertices(List<GMLCoordinates> coords) {
        List<Point2D> result = new ArrayList<Point2D>(coords.size());
        for (GMLCoordinates c : coords) {
            result.add(new Point2D(c.getX(), c.getY()));
        }
        return result;
    }

    private GMLBuilding createBuilding(double xMin, double yMin, double xMax, double yMax) {
        List<GMLNode> nodes = new ArrayList<GMLNode>();
        nodes.add(map.createNode(xMin, yMin));
        nodes.add(map.createNode(xMax, yMin));
        nodes.add(map.createNode(xMax, yMax));
        nodes.add(map.createNode(xMin, yMax));
        return map.createBuildingFromNodes(nodes);
    }
}
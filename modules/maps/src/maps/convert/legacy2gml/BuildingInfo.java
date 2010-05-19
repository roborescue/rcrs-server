package maps.convert.legacy2gml;

import maps.legacy.LegacyBuilding;
import maps.gml.GMLMap;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLNode;
import maps.gml.GMLDirectedEdge;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

/**
   Container for building information during conversion.
*/
public class BuildingInfo {
    private static final int ENTRANCE_SIZE = 2500;

    private LegacyBuilding building;
    private List<GMLNode> apexes;

    /**
       Construct a BuildingInfo object.
       @param b The LegacyBuilding to store info about.
    */
    public BuildingInfo(LegacyBuilding b) {
        building = b;
    }

    /**
       Process the building and create a GMLBuilding.
       @param gml The GML map.
       @param nodeInfo Information about the legacy nodes.
       @param roadInfo Information about the legacy roads.
    */
    public void process(GMLMap gml, Map<Integer, NodeInfo> nodeInfo, Map<Integer, RoadInfo> roadInfo) {
        apexes = new ArrayList<GMLNode>();
        int[] coords = building.getApexes();
        for (int i = 0; i < coords.length; i += 2) {
            int x = coords[i];
            int y = coords[i + 1];
            GMLNode node = gml.createNode(x, y);
            apexes.add(node);
        }
        GMLBuilding b = gml.createBuildingFromNodes(apexes);
        // Create an entrance
        //        Logger.debug("Creating entrance for " + building);
        Point2D entrancePoint = nodeInfo.get(building.getEntrances()[0]).getLocation();
        Point2D centre = new Point2D(building.getX(), building.getY());
        Line2D centreLine = new Line2D(centre, entrancePoint);
        Vector2D centreVector = centreLine.getDirection();
        Vector2D leftVector = centreVector.getNormal().normalised().scale(ENTRANCE_SIZE / 2.0);
        Vector2D rightVector = leftVector.scale(-1);
        Line2D left = new Line2D(centre.plus(leftVector), centreVector);
        Line2D right = new Line2D(centre.plus(rightVector), centreVector);
        //        Logger.debug("Entrance point: " + entrancePoint);
        //        Logger.debug("Building centre: " + centre);
        //        Logger.debug("Left line: " + left);
        //        Logger.debug("Right line: " + right);

        Pair<Line2D, GMLDirectedEdge> leftEntrance = getEntrancePoint(left, centreLine, b.getEdges(), true, true);
        if (leftEntrance == null) {
            Logger.warn(b + ": Left entrance line does not intersect any walls");
            return;
        }
        GMLNode leftNode = gml.createNode(leftEntrance.first().getOrigin().getX(), leftEntrance.first().getOrigin().getY());
        //        Logger.debug("New left node: " + leftNode);
        gml.splitEdge(leftEntrance.second().getEdge(), leftNode);
        Pair<Line2D, GMLDirectedEdge> rightEntrance = getEntrancePoint(right, centreLine, b.getEdges(), false, true);
        if (rightEntrance == null) {
            Logger.warn(b + ": Right entrance line does not intersect any walls");
            return;
        }
        GMLNode rightNode = gml.createNode(rightEntrance.first().getOrigin().getX(), rightEntrance.first().getOrigin().getY());
        //        Logger.debug("New right node: " + rightNode);
        gml.splitEdge(rightEntrance.second().getEdge(), rightNode);
        // Now create the new road segment
        Pair<Line2D, GMLDirectedEdge> leftRoad = getEntrancePoint(left, centreLine, getAllRoadEdges(nodeInfo.values(), roadInfo.values()), true, false);
        if (leftRoad == null) {
            Logger.warn(b + ": Left entrance line does not intersect any roads");
            return;
        }
        GMLNode roadLeftNode = gml.createNode(leftRoad.first().getEndPoint().getX(), leftRoad.first().getEndPoint().getY());
        //        Logger.debug("New road left node: " + roadLeftNode);
        gml.splitEdge(leftRoad.second().getEdge(), roadLeftNode);
        Pair<Line2D, GMLDirectedEdge> rightRoad = getEntrancePoint(right, centreLine, getAllRoadEdges(nodeInfo.values(), roadInfo.values()), false, false);
        if (rightRoad == null) {
            Logger.warn(b + ": Right entrance line does not intersect any roads");
            return;
        }
        GMLNode roadRightNode = gml.createNode(rightRoad.first().getEndPoint().getX(), rightRoad.first().getEndPoint().getY());
        //        Logger.debug("New road left node: " + roadRightNode);
        gml.splitEdge(rightRoad.second().getEdge(), roadRightNode);
        // Create the road
        gml.createRoad(gml.apexesToEdges(roadLeftNode, roadRightNode, rightNode, leftNode));
    }

    /**
       Trim a line to a set of walls and return the trimmed line and intersecting wall. Returns null if the line does not intersect any walls.
       @param line The line to trim.
       @param walls The walls to trim to.
       @param trimStart True to trim the start of the line, false to trim the end.
    */
    private Pair<Line2D, GMLDirectedEdge> trim(Line2D line, Collection<GMLDirectedEdge> walls, boolean trimStart) {
        GMLDirectedEdge wall = null;
        for (GMLDirectedEdge next : walls) {
            Point2D p = GeometryTools2D.getSegmentIntersectionPoint(line, Tools.gmlDirectedEdgeToLine(next));
            if (p != null) {
                if (trimStart) {
                    line = new Line2D(p, line.getEndPoint());
                }
                else {
                    line = new Line2D(line.getOrigin(), p);
                }
                wall = next;
            }
        }
        if (wall == null) {
            return null;
        }
        return new Pair<Line2D, GMLDirectedEdge>(line, wall);
    }

    private Pair<Line2D, GMLDirectedEdge> getEntrancePoint(Line2D line, Line2D centre, Collection<GMLDirectedEdge> walls, boolean left, boolean trimStart) {
        Pair<Line2D, GMLDirectedEdge> trimmed = trim(line, walls, trimStart);
        if (trimmed == null) {
            // Entrance line does not intersect with the building outline. Snap it back to the endpoint of the wall the centre line intersects with.
            trimmed = trim(centre, walls, trimStart);
            if (trimmed == null) {
                // Entrance does not intersect with any walls!
                return null;
            }
            GMLDirectedEdge wall = trimmed.second();
            Line2D wallLine = Tools.gmlDirectedEdgeToLine(wall);
            Vector2D centreNormal = centre.getDirection().getNormal().normalised();
            if (!left) {
                centreNormal = centreNormal.scale(-1);
            }
            Vector2D wallDirection = wallLine.getDirection().normalised();
            Point2D end;
            if (wallDirection.dot(centreNormal) > 0) {
                // Same direction: end point is the end of the wall
                end = wallLine.getEndPoint();
            }
            else {
                // Opposite directions: end point is the start of the wall
                end = wallLine.getOrigin();
            }
            return new Pair<Line2D, GMLDirectedEdge>(new Line2D(end, line.getEndPoint()), wall);
        }
        else {
            return trimmed;
        }
    }

    private Collection<GMLDirectedEdge> getAllRoadEdges(Collection<NodeInfo> nodes, Collection<RoadInfo> roads) {
        Collection<GMLDirectedEdge> all = new HashSet<GMLDirectedEdge>();
        for (NodeInfo next : nodes) {
            GMLRoad r = next.getRoad();
            if (r != null) {
                all.addAll(r.getEdges());
            }
        }
        for (RoadInfo next : roads) {
            all.addAll(next.getRoad().getEdges());
        }
        return all;
    }
}


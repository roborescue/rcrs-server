package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.PolygonLayer;
import maps.convert.osm2gml.debug.StepVisualizer;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.awt.geom.Area;
import java.util.*;

/**
 * Creates entrance roads serving as building entrances to connect
 * building with adjacent roads.
 */
public class CreateEntrancesStep extends ConvertStep {
    private final TemporaryMap map;
    private final double maxConnectDistance;
    private final double minConnectDistance;
    private final double maxAngleDeviation;
    private final double entranceWidth;

    private record EntrancePlan(
            TemporaryIntersection entranceObject,
            Edge buildingEdge, Edge roadEdge,
            Node buildingNode1, Node buildingNode2,
            Node roadNode1, Node roadNode2
    ) {}

    /**
     * Constructs a new {@code CreateEntrancesStep}.
     *
     * @param map the map
     */
    public CreateEntrancesStep(TemporaryMap map) {
        this.map = map;
        maxConnectDistance = ConvertTools.sizeOfMeters(map.getOSMMap(), 20);
        minConnectDistance = ConvertTools.sizeOfMeters(map.getOSMMap(), 1); // Nearby threshold
        maxAngleDeviation = 45;
        entranceWidth = ConvertTools.sizeOfMeters(map.getOSMMap(), Constants.ROAD_WIDTH);
    }

    @Override
    public String getDescription() {
        return "Creating roads serving as building entrance";
    }

    @Override
    protected void step() {
        List<TemporaryBuilding> buildings = new ArrayList<>(map.getBuildings());
        List<TemporaryIntersection> entrance = new ArrayList<>();
        setProgressLimit(buildings.size());

        double cellSize = maxConnectDistance * 1.2;
        SpatialGrid<TemporaryObject> objectGrid = new SpatialGrid<>(map.getBounds(), cellSize);
        map.getAllObjects().forEach(objectGrid::add);

        for (TemporaryBuilding building : buildings) {
            if (isAlreadyConnected(building, map.getRoads())) {
                bumpProgress();
                continue;
            }

            EntrancePlan bestPlan = findBestPlanForBuilding(building, objectGrid);
            if (bestPlan != null) {
                map.splitEdge(bestPlan.buildingEdge(), bestPlan.buildingNode1(), bestPlan.buildingNode2());
                map.splitEdge(bestPlan.roadEdge(), bestPlan.roadNode1(), bestPlan.roadNode2());
                map.addIntersection(bestPlan.entranceObject());
                entrance.add(bestPlan.entranceObject());
            }
            bumpProgress();
        }

        if (!entrance.isEmpty()) {
            map.resynchronizeStateFromObjects();
        }

        setProgress(buildings.size());
        setStatus("Created " + entrance.size() + " new entrances for buildings.");
        visualizeResults(entrance);
    }

    private EntrancePlan findBestPlanForBuilding(
            TemporaryBuilding building, SpatialGrid<TemporaryObject> objectGrid) {
        EntrancePlan bestPlan = null;
        double bestAngleDeviation = Double.MAX_VALUE;
        boolean isBuildingCCW = GeometryTools2D.isCounterClockwise(building.getVertices());

        for (DirectedEdge buildingEdge : building.getEdges()) {
            if (buildingEdge.getLength() < entranceWidth) continue;

            for (TemporaryObject object : objectGrid.getNearbyItems(building)) {
                if (!(object instanceof TemporaryRoad road)) continue;

                boolean isRoadCCW = GeometryTools2D.isCounterClockwise(road.getVertices());

                for (DirectedEdge roadEdge : road.getEdges()) {
                    if (1 < map.getAttachedObjects(roadEdge).size()) continue;
                    if (roadEdge.getLength() < entranceWidth) continue;

                    Point2D wallMidPoint = buildingEdge.getMidpoint();
                    Line2D roadLine = roadEdge.getLine();
                    Point2D connectingPoint = GeometryTools2D.getClosestPointOnSegment(roadLine, wallMidPoint);

                    Vector2D wallToRoad = connectingPoint.minus(wallMidPoint);
                    if (pointsInward(wallToRoad, buildingEdge, isBuildingCCW)) continue;
                    Vector2D roadToWall = wallMidPoint.minus(connectingPoint);
                    if (pointsInward(roadToWall, roadEdge, isRoadCCW)) continue;

                    // Safely calculate the entrance roof on the road, sliding if necessary.
                    double distFromStart = GeometryTools2D.getDistance(roadLine.getOrigin(), connectingPoint);
                    double distFromEnd = roadEdge.getLength() - distFromStart;
                    double halfWidth = entranceWidth / 2.0;
                    if (distFromStart < halfWidth) {
                        double slideAmount = halfWidth - distFromStart;
                        connectingPoint = connectingPoint.plus(roadLine.getDirection().normalised().scale(slideAmount));
                    } else if (distFromEnd < halfWidth) {
                        double slideAmount = halfWidth - distFromEnd;
                        connectingPoint = connectingPoint.plus(roadLine.getDirection().normalised().scale(-slideAmount));
                    }

                    Vector2D wallVector = buildingEdge.getLine().getDirection().normalised();
                    Vector2D roadVector = roadLine.getDirection().normalised();
                    Node b1 = map.getNode(wallMidPoint.plus(wallVector.scale(-halfWidth)));
                    Node b2 = map.getNode(wallMidPoint.plus(wallVector.scale(halfWidth)));
                    Node r1 = map.getNode(connectingPoint.plus(roadVector.scale(-halfWidth)));
                    Node r2 = map.getNode(connectingPoint.plus(roadVector.scale(halfWidth)));

                    // Build entrance edges, merging nearby nodes and skipping degenerate shapes.
                    List<DirectedEdge> entranceEdges = buildEntranceEdges(b1, b2, r1, r2, wallVector, roadVector);
                    if (entranceEdges == null) {
                        continue;
                    }
                    if (connectingEdgesCrossOwnGeometry(entranceEdges, b1, b2, buildingEdge, roadEdge, building, road)) {
                        continue;
                    }

                    TemporaryIntersection entrance = new TemporaryIntersection(entranceEdges);
                    Line2D entranceCenterLine = new Line2D(wallMidPoint, connectingPoint);
                    double entranceLength = entranceCenterLine.getDirection().getLength();

                    // This prevents creating entrances that are too short to be meaningful
                    // or are likely to cause geometric instability.
                    boolean isConnectDistanceTooShort = entranceLength < minConnectDistance;
                    boolean isConnectDistanceTooLong = maxConnectDistance < entranceLength;
                    if (isConnectDistanceTooShort || isConnectDistanceTooLong) continue;

                    double angleDeviation = calculateAngleDeviation(entranceCenterLine, buildingEdge, roadEdge);
                    boolean exceedsAngleToTolerance = maxAngleDeviation < angleDeviation;
                    if (exceedsAngleToTolerance) continue;

                    if (hasCollision(entrance, building, road)) continue;

                    if (angleDeviation < bestAngleDeviation) {
                        bestAngleDeviation = angleDeviation;
                        bestPlan = new EntrancePlan(entrance, buildingEdge.getEdge(), roadEdge.getEdge(), b1, b2, r1, r2);
                    }
                }
            }
        }

        return bestPlan;
    }

    private boolean pointsInward(final Vector2D direction, final DirectedEdge polygonEdge, boolean isCCW) {
        Vector2D edgeDirection = polygonEdge.getLine().getDirection().normalised();
        Vector2D outwardNormal = isCCW ? edgeDirection.getNormal().negate() : edgeDirection.getNormal();
        return direction.dot(outwardNormal) < 0;
    }

    private boolean connectingEdgesCrossOwnGeometry(
            List<DirectedEdge> entranceEdges, final Node b1, final Node b2,
            DirectedEdge buildingEdge, final DirectedEdge roadEdge,
            TemporaryBuilding building, final TemporaryRoad road) {
        for (DirectedEdge entranceEdge : entranceEdges) {
            if (isWallOrRoadEdge(entranceEdge, b1, b2)) continue;

            if (crossAnyEdgeExcept(entranceEdge, building.getEdges(), buildingEdge)) return true;
            if (crossAnyEdgeExcept(entranceEdge, road.getEdges(), roadEdge)) return true;
        }
        return false;
    }

    private boolean isWallOrRoadEdge(final DirectedEdge edge, final Node b1, final Node b2) {
        boolean startOnBuildingSide = edge.getStartNode().equals(b1) || edge.getStartNode().equals(b2);
        boolean endOnBuildingSide = edge.getEndNode().equals(b1) || edge.getEndNode().equals(b2);
        return startOnBuildingSide == endOnBuildingSide;
    }

    private boolean crossAnyEdgeExcept(
            DirectedEdge candidate, List<DirectedEdge> edges, DirectedEdge excluded) {
        Line2D candidateLine = candidate.getLine();
        for (DirectedEdge edge : edges) {
            if (edge.equals(excluded)) continue;
            if (GeometryTools2D.getSegmentIntersectionPoint(candidateLine, edge.getLine()) != null) {
                return true;
            }
        }
        return false;
    }

    // Build a list of directed edges forming the entrance polygon from four corner nodes.
    // Nearby nodes that snap to the same position are deduplicated, yielding a triangle
    // when two corners coincide. Returns null if fewer than 3 distinct corners remain.
    private List<DirectedEdge> buildEntranceEdges(
            Node b1, Node b2, Node r1, Node r2, Vector2D wallVector, Vector2D roadVector) {
        // Preserve winding order based on the relative orientation of wall and road.
        List<Node> orderedCorners = 0 < wallVector.dot(roadVector)
                ? List.of(b1, b2, r2, r1)
                : List.of(b1, b2, r1, r2);
        List<Node> corners = new ArrayList<>(new LinkedHashSet<>(orderedCorners));

        // Skip if fewer than 3 distinct corners exist;
        // the entrance would not form a valid polygon.
        if (corners.size() < 3) return null;

        // Create entrance shape
        List<DirectedEdge> entranceEdges = new ArrayList<>();
        for (int j = 0; j < corners.size(); j++) {
            entranceEdges.add(map.getDirectedEdge(
                    corners.get(j),
                    corners.get((j + 1) % corners.size())));
        }

        return entranceEdges;
    }

    private double calculateAngleDeviation(Line2D centerLine, DirectedEdge buildingEdge, DirectedEdge roadEdge) {
        double angleToBuilding = Math.abs(90.0 - GeometryTools2D.getAngleBetweenVectors(
                centerLine.getDirection(), buildingEdge.getLine().getDirection()));
        double angleToRoad = Math.abs(90.0 - GeometryTools2D.getAngleBetweenVectors(
                centerLine.getDirection(), roadEdge.getLine().getDirection()));
        return Math.max(angleToBuilding, angleToRoad);
    }

    private boolean isAlreadyConnected(TemporaryBuilding building, Collection<TemporaryRoad> roads) {
        Set<Edge> buildingEdges = new HashSet<>();
        for (DirectedEdge de : building.getEdges()) {
            buildingEdges.add(de.getEdge());
        }
        for (TemporaryRoad road : roads) {
            for (DirectedEdge de : road.getEdges()) {
                if (buildingEdges.contains(de.getEdge())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasCollision(
            TemporaryIntersection candidate, TemporaryBuilding building, TemporaryRoad road) {

        final Area entranceArea = new Area(candidate.getShape());
        for (TemporaryObject otherObject : map.getAllObjects()) {
            Area otherArea = new Area(otherObject.getShape());
            otherArea.intersect(entranceArea);
            if (otherArea.isEmpty()) continue;
            if (!otherObject.equals(building) && !otherObject.equals(road)) return true;
        }

        return false;
    }

    private void visualizeResults(List<TemporaryIntersection> entrances) {
        StepVisualizer.create(debug)
                .title("Create Entrances")
                .layer(PolygonLayer.of(entrances)
                        .name("Entrances")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .backgroundLayer(PolygonLayer.of(map.getAllObjects())
                        .name("Objects")
                        .outlineColor(DebugPalette.SLATE_STROKE)
                        .fillColor(DebugPalette.SLATE_FILL))
                .show();
    }
}

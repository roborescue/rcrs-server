package maps.convert.osm2gml;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.awt.geom.Area;
import java.util.*;

public class ConnectBuildingsStep extends BaseModificationStep {

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

    public ConnectBuildingsStep(final TemporaryMap map) {
        super(map);
        maxConnectDistance = ConvertTools.sizeOfMeters(map.getOSMMap(), 20);
        minConnectDistance = ConvertTools.sizeOfMeters(map.getOSMMap(), 1); // Nearby threshold
        maxAngleDeviation = 45;
        entranceWidth = ConvertTools.sizeOfMeters(map.getOSMMap(), Constants.ROAD_WIDTH);
    }

    @Override
    public String getDescription() {
        return "Connecting buildings to roads";
    }

    @Override
    protected void step() {
        final List<TemporaryBuilding> buildings = new ArrayList<>(map.getBuildings());
        final List<TemporaryIntersection> created = new ArrayList<>();
        setProgressLimit(buildings.size());

        final double cellSize = maxConnectDistance * 2;
        final SpatialGrid<TemporaryObject> objectGrid = new SpatialGrid<>(map.getBounds(), cellSize);
        map.getAllObjects().forEach(objectGrid::add);

        for (final TemporaryBuilding building : buildings) {
            if (isAlreadyConnected(building, map.getRoads())) {
                bumpProgress();
                continue;
            }

            final EntrancePlan bestPlan = findBestPlanForBuilding(building, objectGrid);
            if (bestPlan != null) {
                map.splitEdge(bestPlan.buildingEdge(), bestPlan.buildingNode1(), bestPlan.buildingNode2());
                map.splitEdge(bestPlan.roadEdge(), bestPlan.roadNode1(), bestPlan.roadNode2());
                map.addIntersection(bestPlan.entranceObject());
                created.add(bestPlan.entranceObject());
            }
            bumpProgress();
        }

        if (!created.isEmpty()) {
            map.resynchronizeStateFromObjects();
        }

        setProgress(buildings.size());
        setStatus("Created " + created.size() + " new entrances for buildings.");
        visualizeDifference(Collections.emptyList(), created, "Building Connection Results");
    }

    private EntrancePlan findBestPlanForBuilding(TemporaryBuilding building, SpatialGrid<TemporaryObject> roadGrid) {
        EntrancePlan bestPlan = null;
        double bestAngleDeviation = Double.MAX_VALUE;

        for (DirectedEdge de : building.getEdges()) {
            Edge buildingEdge = de.getEdge();
            if (buildingEdge.getLine().getDirection().getLength() < entranceWidth) continue;

            for (TemporaryObject obj : roadGrid.getNearbyItems(building)) {
                if (!(obj instanceof final TemporaryRoad road)) continue;

                for (DirectedEdge roadDE : road.getEdges()) {
                    Edge roadEdge = roadDE.getEdge();

                    boolean isPassableEdge = 1 < map.getAttachedObjects(roadEdge).size();
                    if (isPassableEdge) continue;

                    boolean isRoadEdgeTooShort = roadEdge.getLine().getDirection().getLength() < entranceWidth;
                    if (isRoadEdgeTooShort) continue;

                    Point2D wallMidPoint = buildingEdge.getMidPoint();
                    Line2D roadLine = roadEdge.getLine();
                    Point2D connectingPoint = GeometryTools2D.getClosestPointOnSegment(roadLine, wallMidPoint);

                    // Safely calculate the entrance roof on the road, sliding if necessary.
                    double distFromStart = GeometryTools2D.getDistance(roadLine.getOrigin(), connectingPoint);
                    double distFromEnd = roadLine.getDirection().getLength() - distFromStart;
                    double halfWidth = entranceWidth / 2.0;
                    if (distFromStart < halfWidth) {
                        double slideAmount = halfWidth - distFromStart;
                        connectingPoint = connectingPoint.plus(roadLine.getDirection().normalised().scale(slideAmount));
                    } else if (distFromEnd < halfWidth) {
                        double slideAmount = halfWidth - distFromEnd;
                        connectingPoint = connectingPoint.plus(roadLine.getDirection().normalised().scale(-slideAmount));
                    }

                    final Vector2D wallVector = buildingEdge.getLine().getDirection().normalised();
                    final Vector2D roadVector = roadLine.getDirection().normalised();
                    final Node b1 = map.getNode(wallMidPoint.plus(wallVector.scale(-halfWidth)));
                    final Node b2 = map.getNode(wallMidPoint.plus(wallVector.scale(halfWidth)));
                    final Node r1 = map.getNode(connectingPoint.plus(roadVector.scale(-halfWidth)));
                    final Node r2 = map.getNode(connectingPoint.plus(roadVector.scale(halfWidth)));

                    // Build entrance edges, merging nearby nodes and skipping degenerate shapes.
                    final List<DirectedEdge> entranceEdges = buildEntranceEdges(b1, b2, r1, r2, wallVector, roadVector);
                    if (entranceEdges == null) continue;

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
                        bestPlan = new EntrancePlan(entrance, buildingEdge, roadEdge, b1, b2, r1, r2);
                    }
                }
            }
        }

        return bestPlan;
    }

    // Build a list of directed edges forming the entrance polygon from four corner nodes.
    // Nearby nodes that snap to the same position are deduplicated, yielding a triangle
    // when two corners coincide. Returns null if fewer than 3 distinct corners remain.
    private List<DirectedEdge> buildEntranceEdges(
            final Node b1, final Node b2, final Node r1, final Node r2,
            final Vector2D wallVector, final Vector2D roadVector) {
        // Preserve winding order based on the relative orientation of wall and road.
        final List<Node> orderedCorners = 0 < wallVector.dot(roadVector)
                ? List.of(b1, b2, r2, r1)
                : List.of(b1, b2, r1, r2);
        final List<Node> corners = new ArrayList<>(new LinkedHashSet<>(orderedCorners));

        // Skip if fewer than 3 distinct corners exist;
        // the entrance would not form a valid polygon.
        if (corners.size() < 3) return null;

        // Create entrance shape
        final List<DirectedEdge> entranceEdges = new ArrayList<>();
        for (int j = 0; j < corners.size(); j++) {
            entranceEdges.add(map.getDirectedEdge(
                    corners.get(j),
                    corners.get((j + 1) % corners.size())));
        }

        return entranceEdges;
    }

    private double calculateAngleDeviation(Line2D centerLine, Edge buildingEdge, Edge roadEdge) {
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

    private boolean hasCollision(TemporaryIntersection candidate, TemporaryBuilding building, TemporaryRoad road) {
        if (candidate.getShape() == null) return false;
        Area entranceArea = new Area(candidate.getShape());

        for (TemporaryObject otherObject : map.getAllObjects()) {
            if (otherObject.getShape() == null) continue;

            Area otherArea = new Area(otherObject.getShape());
            otherArea.intersect(entranceArea);

            if (otherArea.isEmpty()) continue;
            if (!otherObject.equals(building) && !otherObject.equals(road)) return true;

            // A significant collision was found
            if (isSignificantOverlap(otherArea, map)) return true;
        }

        return false;
    }

    private boolean isSignificantOverlap(Area intersectionArea, TemporaryMap map) {
        double oneMeterInDegrees = ConvertTools.sizeOf1Metre(map.getOSMMap());
        double epsilon = Math.pow(oneMeterInDegrees / 10, 2);
        double geometricArea = ConvertTools.getGeometricArea(intersectionArea);
        return epsilon < geometricArea;
    }
}

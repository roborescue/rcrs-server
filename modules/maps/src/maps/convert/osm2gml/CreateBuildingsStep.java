package maps.convert.osm2gml;

import maps.gml.GMLMap;

import maps.convert.ConvertStep;

import java.util.Random;

/**
   This class populates a GMLMap with random buildings.
*/
public class CreateBuildingsStep extends ConvertStep {
    private static final double SIMILAR_LENGTH_THRESHOLD = 0.1;
    private static final double NEARLY_PARALLEL_THRESHOLD = 0.0001;

    //    private GMLMap gmlMap;
    //    private double sizeOf1m;
    //    private Random random;

    /**
       Construct a CreateBuildingsStep.
       @param gmlMap The GMLMap to use.
       @param sizeOf1m The size of 1m in GMLMap units.
       @param random The random number generator to use.
    */
    public CreateBuildingsStep(GMLMap gmlMap, double sizeOf1m, Random random) {
        //        this.gmlMap = gmlMap;
        //        this.sizeOf1m = sizeOf1m;
        //        this.random = random;
    }

    @Override
    public String getDescription() {
        return "Creating buildings";
    }

    @Override
    protected void step() {
        /*
        debug.setBackground(ConvertTools.getAllGMLShapes(gmlMap));
        // Find open spaces with no buildings
        setProgressLimit(gmlMap.getEdges().size());
        Set<GMLEdge> seenLeft = new HashSet<GMLEdge>();
        Set<GMLEdge> seenRight = new HashSet<GMLEdge>();
        for (GMLEdge edge : gmlMap.getEdges()) {
            if (seenLeft.contains(edge) && seenRight.contains(edge)) {
                bumpProgress();
                continue;
            }
            // Try walking from this edge looking for space on the left
            if (!seenLeft.contains(edge)) {
                List<GMLDirectedEdge> edges = walk(edge, true);
                if (edges != null) {
                    processOpenSpace(edges);
                    for (GMLDirectedEdge dEdge : edges) {
                        seenLeft.add(dEdge.getEdge());
                    }
                }
            }
            if (!seenRight.contains(edge)) {
                List<GMLDirectedEdge> edges = walk(edge, false);
                if (edges != null) {
                    processOpenSpace(reverseEdgeList(edges));
                    for (GMLDirectedEdge dEdge : edges) {
                        seenRight.add(dEdge.getEdge());
                    }
                }
            }
            bumpProgress();
        }
        */
    }

    /*
    private List<GMLDirectedEdge> walk(GMLEdge edge, boolean left) {
        if (hasConnectedFace(edge, left)) {
            return null;
        }
        GMLNode start = edge.getStart();
        GMLNode current = edge.getEnd();
        GMLDirectedEdge dEdge = new GMLDirectedEdge(edge, start);
        List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>();
        result.add(dEdge);
        GMLEdge last = edge;
        while (current != start) {
            GMLEdge next = ConvertTools.findBestTurn(dEdge, gmlMap.getAttachedEdges(current), left);
            boolean forward = current == next.getStart();
            boolean lookOnLeft = (left && forward) || (!left && !forward);
//            debug.show("Walking outside " + (left ? "left" : "right"),
//                       new GMLEdgeShapeInfo(last, "From edge", Constants.RED, true),
//                       new GMLEdgeShapeInfo(next, "To edge", Constants.BLUE, true));
            // See if any faces are connected on the side we care about
            if (hasConnectedFace(next, lookOnLeft)) {
                // There's a connected face so this walk isn't going to result in an open space.
                return null;
            }
            dEdge = new GMLDirectedEdge(next, current);
            current = dEdge.getEndNode();
            last = next;
            result.add(dEdge);
        }
        return result;
    }

    private boolean hasConnectedFace(GMLEdge edge, boolean left) {
        Set<GMLFace> faces = gmlMap.getAttachedFaces(edge);
//        List<ShapeDebugFrame.ShapeInfo> shapes = new ArrayList<ShapeDebugFrame.ShapeInfo>();
//        shapes.add(new GMLEdgeShapeInfo(edge, "Test edge", Constants.BLUE, true));
//        shapes.add(new GMLNodeShapeInfo(edge.getStart(), "Start node", Constants.RED, true));
//        shapes.add(new GMLNodeShapeInfo(edge.getEnd(), "End node", Constants.NAVY, true));
//        for (GMLFace face : faces) {
//            shapes.add(new GMLFaceShapeInfo(face, "Attached face", Constants.BLACK, Constants.TRANSPARENT_ORANGE, true));
//        }
//        debug.show("Checking for connected faces", shapes);
        for (GMLFace face : faces) {
            if (FaceType.BUILDING.equals(face.getFaceType())) {
                // Always exclude edges on buildings even if they're on the opposite side
                return true;
            }
            if (left) {
                if (face.isConnectedLeft(edge)) {
                    return true;
                }
            }
            else {
                if (face.isConnectedRight(edge)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<GMLDirectedEdge> reverseEdgeList(List<GMLDirectedEdge> edges) {
        List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>(edges.size());
        for (GMLDirectedEdge edge : edges) {
            result.add(new GMLDirectedEdge(edge.getEdge(), !edge.isForward()));
        }
        Collections.reverse(result);
        return result;
    }

    private void processOpenSpace(List<GMLDirectedEdge> edges) {
        GMLFace face = gmlMap.createFace(edges, FaceType.BUILDING);
        gmlMap.removeFace(face);
        if (!ConvertTools.isClockwise(face)) {
            List<GMLDirectedEdgeShapeInfo> e = new ArrayList<GMLDirectedEdgeShapeInfo>(edges.size());
            for (GMLDirectedEdge next : edges) {
                e.add(new GMLDirectedEdgeShapeInfo(next, "Open space edge", Constants.OLIVE, true, true));
            }
            // Split into "nice" shapes
            //            if (isNearlyRectangular(face)) {
                debug.show("Open space", e);
                RowHousingBuildingSpaceFiller filler = new RowHousingBuildingSpaceFiller(sizeOf1m, random, debug);
                filler.createBuildings(face, gmlMap);
                //            }
        }
    }

    private boolean isNearlyRectangular(GMLFace face) {
        if (face.getEdges().size() != 4) {
            return false;
        }
        // Check if the opposing faces are approximately parallel
        Iterator<GMLDirectedEdge> it = face.getEdges().iterator();
        GMLDirectedEdge e1 = it.next();
        GMLDirectedEdge e2 = it.next();
        GMLDirectedEdge e3 = it.next();
        GMLDirectedEdge e4 = it.next();
        if (nearlyParallel(e1, e3) && nearlyParallel(e2, e4) && similarLength(e1, e3) && similarLength(e2, e4)) {
            return true;
        }
        return false;
    }

    private boolean nearlyParallel(GMLDirectedEdge e1, GMLDirectedEdge e2) {
        Line2D l1 = ConvertTools.gmlDirectedEdgeToLine(e1);
        Line2D l2 = ConvertTools.gmlDirectedEdgeToLine(e2);
        double d = (l1.getDirection().getX() * l2.getDirection().getY()) - (l1.getDirection().getY() * l2.getDirection().getX());
        return ConvertTools.nearlyEqual(d, 0, NEARLY_PARALLEL_THRESHOLD);
    }

    private boolean similarLength(GMLDirectedEdge e1, GMLDirectedEdge e2) {
        double l1 = ConvertTools.gmlDirectedEdgeToLine(e1).getDirection().getLength();
        double l2 = ConvertTools.gmlDirectedEdgeToLine(e1).getDirection().getLength();
        return ConvertTools.nearlyEqual(l1 - l2, 0, SIMILAR_LENGTH_THRESHOLD);
    }
    */
}

package maps.convert.osm2gml.buildings.row;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;

import maps.gml.GMLDirectedEdge;
import maps.gml.GMLShape;
import maps.gml.GMLMap;

/**
   A RowFiller that creates long, thin duplex units.
*/
public class ThinDuplexRowFiller implements RowFiller {
    private static final double BUILDING_WIDTH_M = 10;
    private static final double BUILDING_DEPTH_M = 20;
    private static final double MIN_OFFSET_M = 2;
    private static final double MAX_OFFSET_M = 3;
    private static final int MIN_RUN_LENGTH = 1;
    private static final int MAX_RUN_LENGTH = 5;

    private final double buildingWidth;
    private final double buildingDepth;
    private final double minOffset;
    private final double maxOffset;

    private final Random random;

    /**
       Construct a ThinDuplexRowFiller.
       @param sizeOf1m The size of 1m.
       @param random A random number generator.
    */
    public ThinDuplexRowFiller(double sizeOf1m, Random random) {
        buildingWidth = BUILDING_WIDTH_M * sizeOf1m;
        buildingDepth = BUILDING_DEPTH_M * sizeOf1m;
        minOffset = MIN_OFFSET_M * sizeOf1m;
        maxOffset = MAX_OFFSET_M * sizeOf1m;
        this.random = random;
    }

    @Override
    public Set<GMLShape> fillRow(GMLDirectedEdge edge, GMLMap map) {
        Set<GMLShape> result = new HashSet<GMLShape>();
        /*
        Line2D edgeLine = ConvertTools.gmlDirectedEdgeToLine(edge);
        Vector2D normal = edgeLine.getDirection().getNormal().normalised();
        // Create buildings along the edge until we run out of room
        double edgeLength = edgeLine.getDirection().getLength();
        double offset = getRandomOffset();
        int runLength = getRandomRunLength();
        double d = 0;
        while (d < 1) {
            if (runLength-- == 0) {
                offset = getRandomOffset();
                runLength = getRandomRunLength();
            }
            double d1 = d;
            double d2 = d + (BUILDING_WIDTH / edgeLength);
            Point2D topRight = edgeLine.getPoint(d1);
            Point2D topLeft = edgeLine.getPoint(d2);
            result.addAll(createBuildingInLot(edgeLine, topRight, topLeft, normal, offset, map));
            d = d2;
        }
        */
        return result;
    }

    /*
    private Set<GMLFace> createBuildingInLot(Line2D edgeLine, Point2D topRight, Point2D topLeft, Vector2D edgeNormal, double depthOffset, GMLMap map) {
        // Offset from the top of the boundary
        topRight = topRight.plus(edgeNormal.scale(depthOffset));
        topLeft = topLeft.plus(edgeNormal.scale(depthOffset));
        // Find the other end of the building
        Point2D bottomRight = topRight.plus(edgeNormal.scale(BUILDING_DEPTH));
        Point2D bottomLeft = topLeft.plus(edgeNormal.scale(BUILDING_DEPTH));
        // Create new nodes and directed edges for the lot
        GMLNode n1 = map.ensureNodeNear(topRight);
        GMLNode n2 = map.ensureNodeNear(topLeft);
        GMLNode n3 = map.ensureNodeNear(bottomLeft);
        GMLNode n4 = map.ensureNodeNear(bottomRight);
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        GMLDirectedEdge e1 = map.ensureDirectedEdge(topRight, topLeft);
        GMLDirectedEdge e2 = map.ensureDirectedEdge(topLeft, bottomLeft);
        GMLDirectedEdge e3 = map.ensureDirectedEdge(bottomLeft, bottomRight);
        GMLDirectedEdge e4 = map.ensureDirectedEdge(bottomRight, topRight);
        edges.add(e1);
        edges.add(e2);
        edges.add(e3);
        edges.add(e4);
        GMLFace buildingFace = map.createFace(edges, FaceType.BUILDING);
        // Make the entrance face
        Set<GMLFace> result = new HashSet<GMLFace>();
        result.add(buildingFace);
        return result;
    }

    private double getRandomOffset() {
        double d = random.nextDouble();
        double range = MAX_OFFSET - MIN_OFFSET;
        return MIN_OFFSET + (d * range);
    }

    private int getRandomRunLength() {
        return MIN_RUN_LENGTH + random.nextInt(MAX_RUN_LENGTH - MIN_RUN_LENGTH + 1);
    }
    */
}

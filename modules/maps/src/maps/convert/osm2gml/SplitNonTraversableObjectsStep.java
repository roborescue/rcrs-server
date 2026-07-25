package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.*;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Splits non-traversable temporary objects into traversable sub-shapes.
 */
public class SplitNonTraversableObjectsStep extends ConvertStep {
    private final TemporaryMap map;
    private final double clearanceThreshold;
    private final double minSplitLength;

    private static final double CLEARANCE_THRESHOLD_METER = 0.1;
    private static final double MIN_SPLIT_LENGTH_METER = 1.0;
    private static final boolean VISUALIZE_SPLIT_ITERATIONS = false;

    /**
     * Constructs a new {@code SplitNonTraversableObjectsStep}.
     *
     * @param map the map
     */
    public SplitNonTraversableObjectsStep(TemporaryMap map) {
        this.map = map;
        clearanceThreshold = ConvertTools.sizeOfMeters(map.getOSMMap(), CLEARANCE_THRESHOLD_METER);
        minSplitLength = ConvertTools.sizeOfMeters(map.getOSMMap(), MIN_SPLIT_LENGTH_METER);
    }

    @Override
    public String getDescription() {
        return "Ensure shapes are traversable";
    }

    @Override
    protected void step() {
        Collection<TemporaryObject> allObjects = new LinkedHashSet<>(map.getAllObjects());

        Set<TemporaryObject> removed = new LinkedHashSet<>();
        Set<TemporaryObject> created = new LinkedHashSet<>();

        setProgressLimit(allObjects.size());

        for (TemporaryObject object : allObjects) {
            SplitCandidate candidate = SplitCandidate.of(object, map);
            if (candidate.isTraversable(clearanceThreshold)) {
                bumpProgress();
                continue;
            }

            Set<SplitCandidate> safePieces = splitIntoTraversableObjects(candidate);
            map.removeTemporaryObject(object);
            removed.add(object);
            for (SplitCandidate safePiece : safePieces) {
                TemporaryObject objectToCreate = createTemporaryObject(safePiece, object);
                map.addTemporaryObject(objectToCreate);
                created.add(objectToCreate);
            }
            bumpProgress();
        }

        map.resynchronizeStateFromObjects();

        setStatus("Split " + removed.size() + " objects into " + created.size() + " traversable sub-shapes");
        visualizeResults(removed, created);
    }

    private record SplitCandidate(
            List<Point2D> vertices,
            List<Boolean> edgePassability
    ) {

        /**
         * Creates a {@link SplitCandidate} from a temporary object.
         *
         * @param object the source object
         * @param map the map used to determine edge passability
         * @return the corresponding split candidate
         */
        static SplitCandidate of(TemporaryObject object, TemporaryMap map) {
            List<Point2D> vertices = new ArrayList<>();
            List<Boolean> edgePassability = new ArrayList<>();
            for (DirectedEdge edge : object.getEdges()) {
                vertices.add(edge.getStartCoordinates());
                edgePassability.add(map.getAttachedObjects(edge).size() == 2);
            }
            return new SplitCandidate(vertices, edgePassability);
        }

        // Returns all passable boundary edges.
        Set<Line2D> getPassableLines() {
            Set<Line2D> lines = new LinkedHashSet<>();
            int n = vertices.size();
            for (int i = 0; i < n; i++) {
                if (edgePassability.get(i))
                    lines.add(new Line2D(vertices.get(i), vertices.get((i + 1) % n)));
            }
            return Collections.unmodifiableSet(lines);
        }

        // Returns all impassable boundary edges.
        Set<Line2D> getImpassableLines() {
            Set<Line2D> lines = new LinkedHashSet<>();
            int n = vertices.size();
            for (int i = 0; i < n; i++) {
                if (!edgePassability.get(i))
                    lines.add(new Line2D(vertices.get(i), vertices.get((i + 1) % n)));
            }
            return Collections.unmodifiableSet(lines);
        }

        // Returns the lines of sight from the centroid to each passable edge.
        public Set<Line2D> getLinesOfSight() {
            Point2D centroid = GeometryTools2D.computeCentroid(vertices);
            return getPassableLines().stream()
                    .map(exit -> new Line2D(centroid, exit.getMidpoint()))
                    .collect(Collectors.toUnmodifiableSet());
        }

        boolean isTraversable(double threshold) {
            if (vertices.size() < 4) return true;

            Set<Line2D> linesOfSight = getLinesOfSight();
            for (Line2D lineOfSight : linesOfSight) {
                for (Line2D impassableLine : getImpassableLines()) {
                    if (GeometryTools2D.getDistance(impassableLine, lineOfSight) < threshold) {
                        return false;
                    }
                }
            }
            return true;
        }

        List<Point2D> getConcaveVertices() {
            List<Point2D> concave = new ArrayList<>();
            int n = vertices.size();

            if (n < 4) return concave;

            boolean isCCW = GeometryTools2D.isCounterClockwise(vertices);
            for (int i = 0; i < n; i++) {
                Point2D p1 = vertices.get((i - 1 + n) % n);
                Point2D p2 = vertices.get(i);
                Point2D p3 = vertices.get((i + 1) % n);
                Vector2D v1 = p2.minus(p1);
                Vector2D v2 = p3.minus(p2);
                double cross = v1.cross(v2);
                if (isCCW && cross < 0 || !isCCW && 0 < cross) {
                    concave.add(p2);
                }
            }
            return concave;
        }

        List<Line2D> getBoundaryLines() {
            List<Line2D> lines = new ArrayList<>();
            int n = vertices.size();
            for (int i = 0; i < n; i++) {
                lines.add(new Line2D(vertices.get(i), vertices.get((i + 1) % n)));
            }
            return lines;
        }
    }

    private record SplitProposal(Line2D line, int targetVertexIndex, int targetEdgeStartIndex) {

        // Returns whether the split line terminates at an existing vertex.
        boolean isVertexTarget() {
            return targetVertexIndex != -1;
        }
    }

    // Recursively splits a polygon until every piece become traversable.
    private Set<SplitCandidate> splitIntoTraversableObjects(SplitCandidate candidate) {
        Queue<SplitCandidate> queue = new ArrayDeque<>();
        Set<SplitCandidate> result = new LinkedHashSet<>();

        queue.add(candidate);

        while (!queue.isEmpty()) {
            SplitCandidate current = queue.poll();

            if (current.isTraversable(clearanceThreshold)) {
                result.add(current);
                continue;
            }

            List<Point2D> concaveVertices = current.getConcaveVertices();
            if (concaveVertices.isEmpty()) {
                result.add(current);
                continue;
            }

            SplitProposal bestProposal = findBestSplitLine(current);
            if (bestProposal == null) {
                throw new IllegalStateException(
                    "Failed to find a valid split line for a non-traversable polygon. " +
                    "This indicates that the polygon has a complex self-interesting shape, " +
                    "or there is a severe floating-point precision issue."
                );
            }

            if (VISUALIZE_SPLIT_ITERATIONS) {
                visualizeSplitProposal(current, bestProposal);
            }

            Set<SplitCandidate> splitResults = performSplit(current, bestProposal);
            queue.addAll(splitResults);

            if (VISUALIZE_SPLIT_ITERATIONS) {
                visualizeSplitResults(splitResults);
            }
        }
        return result;
    }

    // Creates a temporary object from a split polygon.
    private TemporaryObject createTemporaryObject(SplitCandidate safePiece, TemporaryObject object) {
        List<Point2D> vertices = safePiece.vertices();
        List<DirectedEdge> edges = new ArrayList<>();
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            Node startNode = map.getNode(vertices.get(i));
            Node endNode = map.getNode(vertices.get((i + 1) % n));
            edges.add(map.getDirectedEdge(startNode, endNode));
        }
        return object.copyWithEdges(edges);
    }

    // Finds the highest-scoring valid split line.
    private SplitProposal findBestSplitLine(SplitCandidate candidate) {
        SplitProposal bestProposal = null;
        double bestScore = -Double.MAX_VALUE;

        List<Point2D> vertices = candidate.vertices();
        int n = vertices.size();

        for (Point2D origin : candidate.getConcaveVertices()) {
            for (int i = 1; i < n; i++) {
                Point2D end = vertices.get(i);
                Line2D testLine = new Line2D(origin, end);
                if (isInvalidSplitLine(testLine, candidate))
                    continue;

                SplitProposal proposal = new SplitProposal(testLine, i, -1);
                double score = evaluateSplit(candidate, proposal);
                if (bestScore < score) {
                    bestScore = score;
                    bestProposal = proposal;
                }
            }

            for (Line2D impassableLine : candidate.getImpassableLines()) {
                if (origin.equals(impassableLine.getOrigin()) || origin.equals(impassableLine.getEndPoint()))
                    continue;

                Point2D end = GeometryTools2D.getClosestPointOnSegment(impassableLine, origin);
                if (end.equals(impassableLine.getOrigin()) || end.equals(impassableLine.getEndPoint()) ||
                        map.containsNode(end))
                    continue;

                Line2D testLine = new Line2D(origin, end);
                if (isInvalidSplitLine(testLine, candidate))
                    continue;

                int index = candidate.vertices().indexOf(impassableLine.getOrigin());
                SplitProposal proposal = new SplitProposal(testLine, -1, index);
                double score = evaluateSplit(candidate, proposal);
                if (bestScore < score) {
                    bestScore = score;
                    bestProposal = proposal;
                }
            }
        }
        return bestProposal;
    }

    // Returns whether the candidate split line is geometrically valid.
    private boolean isInvalidSplitLine(Line2D testLine, SplitCandidate candidate) {
        if (testLine.getLength() <= minSplitLength) {
            return true;
        }

        for (Line2D boundaryLine : candidate.getBoundaryLines()) {
            if (testLine.isGeometricallyEquivalent(boundaryLine)) {
                return true;
            }
        }

        for (Line2D boundaryLine : candidate.getBoundaryLines()) {
            Point2D intersection = GeometryTools2D.getSegmentIntersectionPoint(boundaryLine, testLine);
            if (intersection != null &&
                    !intersection.equals(testLine.getOrigin()) &&
                    !intersection.equals(testLine.getEndPoint())) {
                return true;
            }
        }

        return !GeometryTools2D.isPointInsidePolygon(testLine.getMidpoint(), candidate.vertices());
    }

    // Evaluates the quality of a split proposal.
    private double evaluateSplit(SplitCandidate candidate, SplitProposal proposal) {
        Set<SplitCandidate> splitPieces = performSplit(candidate, proposal);
        double score = 0.0;
        for (SplitCandidate piece : splitPieces) {
            if (piece.isTraversable(clearanceThreshold)) {
                score += 100.0;
            }
        }
        score -= proposal.line().getLength();
        return score;
    }

    // Splits a polygon into two polygons.
    private Set<SplitCandidate> performSplit(SplitCandidate candidate, SplitProposal proposal) {
        Point2D p1 = proposal.line().getOrigin();
        Point2D p2 = proposal.line().getEndPoint();

        List<Point2D> workingVertices = new ArrayList<>(candidate.vertices());
        List<Boolean> workingPassability = new ArrayList<>(candidate.edgePassability());

        int p2Index;
        if (proposal.isVertexTarget()) {
            p2Index = proposal.targetVertexIndex();
        } else {
            int insertIndex = proposal.targetEdgeStartIndex() + 1;
            workingVertices.add(insertIndex, p2);
            workingPassability.add(insertIndex, workingPassability.get(proposal.targetEdgeStartIndex()));
            p2Index = insertIndex;
        }

        int p1Index = workingVertices.indexOf(p1);

        SplitCandidate c1 = extractSubCandidate(workingVertices, workingPassability, p1Index, p2Index);
        SplitCandidate c2 = extractSubCandidate(workingVertices, workingPassability, p2Index, p1Index);
        return Set.of(c1, c2);
    }

    // Extracts a polygon between two vertices.
    private SplitCandidate extractSubCandidate(
            List<Point2D> vertices, List<Boolean> passability, int startIndex, int endIndex) {

        List<Point2D> polyVertices = new ArrayList<>();
        List<Boolean> polyPassability = new ArrayList<>();
        int n = vertices.size();

        int curr = startIndex;
        while (curr != endIndex) {
            polyVertices.add(vertices.get(curr));
            polyPassability.add(passability.get(curr));
            curr = (curr + 1) % n;
        }
        polyVertices.add(vertices.get(endIndex));
        polyPassability.add(true);

        return new SplitCandidate(polyVertices, polyPassability);
    }

    // --- Split-iteration debug visualization (see VISUALIZE_SPLIT_ITERATIONS) ---

    // Visualize the chosen split line before it is applied.
    private void visualizeSplitProposal(SplitCandidate current, SplitProposal bestProposal) {
        createCandidateVisualizer(current, "Before Split: Best Proposal")
                .layer(LineLayer.of(bestProposal.line())
                        .name("Split Line")
                        .color(DebugPalette.VIOLET_STROKE))
                .show();
    }

    // Visualize each resulting piece immediately after a split.
    private void visualizeSplitResults(Set<SplitCandidate> splitResults) {
        Iterator<SplitCandidate> iterator = splitResults.iterator();
        SplitCandidate c1 = iterator.next();
        SplitCandidate c2 = iterator.next();
        createCandidateVisualizer(c1, "After Split: Piece 1").show();
        createCandidateVisualizer(c2, "After Split: Piece 2").show();
    }

    private StepVisualizer createCandidateVisualizer(SplitCandidate candidate, String title) {
        return StepVisualizer.create(debug)
                .title(title)
                .layer(LineLayer.of(candidate.getLinesOfSight())
                        .name("Lines of Sight")
                        .color(DebugPalette.MOSS_STROKE)
                        .dashed(true))
                .layer(LineLayer.of(candidate.getImpassableLines())
                        .name("Impassable Lines")
                        .color(DebugPalette.AZURE_STROKE))
                .layer(LineLayer.of(candidate.getPassableLines())
                        .name("Passable Lines")
                        .color(DebugPalette.SKY_STROKE)
                        .dashed(true))
                .layer(PointLayer.of(candidate.getConcaveVertices())
                        .name("Concave Vertices")
                        .color(DebugPalette.AMBER_STROKE).shape(PointShape.SQUARE))
                .backgroundLayer(LineLayer.of(map.getAllEdges())
                        .name("Edges")
                        .color(DebugPalette.SLATE_STROKE));
    }

    private void visualizeResults(Set<TemporaryObject> removed, Set<TemporaryObject> created) {
        StepVisualizer.create(debug)
                .title("Split Non-traversable Objects")
                .layer(PolygonLayer.of(removed)
                        .name("Removed Objects")
                        .fillColor(DebugPalette.CORAL_FILL)
                        .outlineColor(DebugPalette.CORAL_STROKE))
                .layer(PolygonLayer.of(created)
                        .name("Created Objects")
                        .fillColor(DebugPalette.MOSS_FILL)
                        .outlineColor(DebugPalette.MOSS_STROKE))
                .backgroundLayer(PolygonLayer.of(map.getAllObjects())
                        .name("Objects")
                        .fillColor(DebugPalette.SLATE_FILL)
                        .outlineColor(DebugPalette.SLATE_STROKE))
                .show();
    }
}

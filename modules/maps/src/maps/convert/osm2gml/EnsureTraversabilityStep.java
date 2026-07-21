package maps.convert.osm2gml;

import maps.convert.osm2gml.debug.*;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This step modify the map so that all shapes are traversable from their centroid.
 */
public class EnsureTraversabilityStep extends BaseModificationStep {

    /**
     * Constructs a new {@link EnsureTraversabilityStep}.
     *
     * @param map The {@link TemporaryMap} to be modified.
     */
    public EnsureTraversabilityStep(TemporaryMap map) {
        super(map);
    }

    @Override
    public String getDescription() {
        return "Ensure shapes are traversable";
    }

    @Override
    protected void step() {
        Collection<TemporaryObject> allObjects = new LinkedHashSet<>(map.getAllObjects());

        List<TemporaryObject> removedObjects = new ArrayList<>();
        List<TemporaryObject> createdObjects = new ArrayList<>();

        setProgressLimit(allObjects.size());

        for (TemporaryObject object : allObjects) {
            SplitCandidate candidate = SplitCandidate.of(object, map);
            if (candidate.isTraversable()) {
                bumpProgress();
                continue;
            }

            Set<SplitCandidate> safePieces = splitIntoTraversableObjects(candidate);
            map.removeTemporaryObject(object);
            removedObjects.add(object);
            for (SplitCandidate safePiece : safePieces) {
                TemporaryObject objectToCreate = createTemporaryObject(safePiece, object);
                map.addTemporaryObject(objectToCreate);
                createdObjects.add(objectToCreate);
            }
            bumpProgress();
        }

        map.resynchronizeStateFromObjects();

        setStatus("Split " + removedObjects.size() + " objects into " + createdObjects.size() + " traversable sub-shapes");
        visualizeDifference(removedObjects, createdObjects, "Ensure Traversability (Split Polygons)");
    }

    private record SplitCandidate(
            List<Point2D> vertices,
            List<Boolean> edgePassability
    ) {
        public static SplitCandidate of(TemporaryObject object, TemporaryMap map) {
            List<Point2D> vertices = new ArrayList<>();
            List<Boolean> edgePassability = new ArrayList<>();
            for (DirectedEdge edge : object.getEdges()) {
                vertices.add(edge.getStartCoordinates());
                edgePassability.add(map.getAttachedObjects(edge).size() == 2);
            }
            return new SplitCandidate(vertices, edgePassability);
        }

        public Set<Line2D> getPassableLines() {
            Set<Line2D> lines = new LinkedHashSet<>();
            final int n = vertices.size();
            for (int i = 0; i < n; i++) {
                if (edgePassability.get(i))
                    lines.add(new Line2D(vertices.get(i), vertices.get((i + 1) % n)));
            }
            return Collections.unmodifiableSet(lines);
        }

        public Set<Line2D> getImpassableLines() {
            Set<Line2D> lines = new LinkedHashSet<>();
            final int n = vertices.size();
            for (int i = 0; i < n; i++) {
                if (!edgePassability.get(i))
                    lines.add(new Line2D(vertices.get(i), vertices.get((i + 1) % n)));
            }
            return Collections.unmodifiableSet(lines);
        }

        public Set<Line2D> getLinesOfSight() {
            final Point2D centroid = GeometryTools2D.computeCentroid(vertices);
            return getPassableLines().stream()
                    .map(exit -> new Line2D(centroid, exit.getMidpoint()))
                    .collect(Collectors.toUnmodifiableSet());
        }

        private boolean isTraversable() {
            if (vertices.size() < 4) return true;

            Set<Line2D> linesOfSight = getLinesOfSight();
            for (Line2D lineOfSight : linesOfSight) {
                for (Line2D impassableLine : getImpassableLines()) {
                    if (GeometryTools2D.getSegmentIntersectionPoint(impassableLine, lineOfSight) != null)
                        return false;
                }
            }
            return true;
        }

        public List<Point2D> getConcaveVertices() {
            List<Point2D> concave = new ArrayList<>();
            final int n = vertices.size();

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

        public List<Line2D> getBoundaryLines() {
            List<Line2D> lines = new ArrayList<>();
            final int n = vertices.size();
            for (int i = 0; i < n; i++) {
                lines.add(new Line2D(vertices.get(i), vertices.get((i + 1) % n)));
            }
            return lines;
        }
    }

    private record SplitProposal(Line2D line, int targetVertexIndex, int targetEdgeStartIndex) {
        public boolean isVertexTarget() {
            return targetVertexIndex != -1;
        }
    }

    private Set<SplitCandidate> splitIntoTraversableObjects(final SplitCandidate candidate) {
        Queue<SplitCandidate> queue = new ArrayDeque<>();
        Set<SplitCandidate> result = new LinkedHashSet<>();

        queue.add(candidate);

        while (!queue.isEmpty()) {
            SplitCandidate current = queue.poll();

            if (current.isTraversable()) {
                result.add(current);
                continue;
            }

            List<Point2D> concaveVertices = current.getConcaveVertices();
            if (concaveVertices.isEmpty()) {
                throw new IllegalStateException(
                    "A non-traversable polygon must have at least one concave vertex. " +
                    "This indicates a geometric calculation error or an invalid polygon shape."
                );
            }

            SplitProposal bestProposal = findBestSplitLine(current);
            if (bestProposal == null) {
                throw new IllegalStateException(
                    "Failed to find a valid split line for a non-traversable polygon. " +
                    "This indicates that the polygon has a complex self-interesting shape, " +
                    "or there is a severe floating-point precision issue."
                );
            }

            // DEBUG
            createCandidateVisualizer(candidate, "Before Split: Best Proposal")
                    .layer(LineLayer.of(bestProposal.line()).color(DebugPalette.VIOLET_STROKE).dashed(true))
                    .show();

            Set<SplitCandidate> splitResults = performSplit(candidate, bestProposal);
            queue.addAll(splitResults);

            // DEBUG
            Iterator<SplitCandidate> iterator = splitResults.iterator();
            SplitCandidate c1 = iterator.next();
            SplitCandidate c2 = iterator.next();
            createCandidateVisualizer(c1, "After Split: Piece 1").show();
            createCandidateVisualizer(c2, "After Split: Piece 2").show();
        }
        return result;
    }

    private StepVisualizer createCandidateVisualizer(SplitCandidate candidate, String title) {
        return StepVisualizer.create(debug)
                .title(title)
                .layer(LineLayer.of(candidate.getLinesOfSight())
                        .color(DebugPalette.MOSS_STROKE).dashed(true))
                .layer(LineLayer.of(candidate.getImpassableLines())
                        .color(DebugPalette.INK_STROKE))
                .layer(LineLayer.of(candidate.getPassableLines())
                        .color(DebugPalette.SLATE_STROKE).dashed(true))
                .layer(PointLayer.of(candidate.getConcaveVertices())
                        .color(DebugPalette.AMBER_STROKE).shape(PointShape.SQUARE))
                .backgroundLayer(LineLayer.of(map.getAllEdges())
                        .color(DebugPalette.CONTEXT_STROKE));
    }

    private TemporaryObject createTemporaryObject(final SplitCandidate safePiece, final TemporaryObject object) {
        List<Point2D> vertices = safePiece.vertices();
        List<DirectedEdge> edges = new ArrayList<>();
        final int n = vertices.size();
        for (int i = 0; i < n; i++) {
            final Node startNode = map.getNode(vertices.get(i));
            final Node endNode = map.getNode(vertices.get((i + 1) % n));
            edges.add(map.getDirectedEdge(startNode, endNode));
        }
        return switch (object) {
            case TemporaryRoad ignore -> new TemporaryRoad(edges);
            case TemporaryIntersection ignore -> new TemporaryIntersection(edges);
            case TemporaryBuilding building -> new TemporaryBuilding(edges, building.getOsmId());
            default -> throw new IllegalStateException("Unsupported object type: " + object.getClass().getName());
        };
    }

    private SplitProposal findBestSplitLine(final SplitCandidate candidate) {
        SplitProposal bestProposal = null;
        double bestScore = -Double.MAX_VALUE;

        final List<Point2D> vertices = candidate.vertices();
        final int n = vertices.size();

        for (final Point2D origin : candidate.getConcaveVertices()) {
            for (int i = 1; i < n; i++) {
                Point2D end = vertices.get(i);
                Line2D testLine = new Line2D(origin, end);
                if (isInvalidSplitLine(testLine, candidate))
                    continue;

                SplitProposal proposal = new SplitProposal(testLine, i, -1);
                final double score = evaluateSplit(candidate, proposal);
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

                final int index = candidate.vertices().indexOf(impassableLine.getOrigin());
                SplitProposal proposal = new SplitProposal(testLine, -1, index);
                final double score = evaluateSplit(candidate, proposal);
                if (bestScore < score) {
                    bestScore = score;
                    bestProposal = proposal;
                }
            }
        }
        return bestProposal;
    }

    private boolean isInvalidSplitLine(final Line2D testLine, final SplitCandidate candidate) {
        if (testLine.getOrigin().equals(testLine.getEndPoint()))
            return true;

        for (Line2D boundaryLine : candidate.getBoundaryLines()) {
            if (testLine.isGeometricallyEquivalent(boundaryLine))
                return true;
        }

        for (Line2D boundaryLine : candidate.getBoundaryLines()) {
            Point2D intersection = GeometryTools2D.getSegmentIntersectionPoint(boundaryLine, testLine);
            if (intersection != null &&
                    !intersection.equals(testLine.getOrigin()) &&
                    !intersection.equals(testLine.getEndPoint()))
                return true;
        }

        return !GeometryTools2D.isPointInsidePolygon(testLine.getMidpoint(), candidate.vertices());
    }

    private double evaluateSplit(final SplitCandidate candidate, final SplitProposal proposal) {
        final Set<SplitCandidate> splitPieces = performSplit(candidate, proposal);
        double score = 0.0;
        for (SplitCandidate piece : splitPieces) {
            if (piece.isTraversable()) {
                score += 100.0;
            }
        }
        score -= proposal.line().getLength();
        return score;
    }

    private Set<SplitCandidate> performSplit(SplitCandidate candidate, SplitProposal proposal) {
        Point2D p1 = proposal.line().getOrigin();
        Point2D p2 = proposal.line().getEndPoint();

        List<Point2D> workingVertices = new ArrayList<>(candidate.vertices());
        List<Boolean> workingPassability = new ArrayList<>(candidate.edgePassability());

        int p2Index;
        if (proposal.isVertexTarget()) {
            p2Index = proposal.targetVertexIndex();
        } else {
            final int insertIndex = proposal.targetEdgeStartIndex() + 1;
            workingVertices.add(insertIndex, p2);
            workingPassability.add(insertIndex, workingPassability.get(proposal.targetEdgeStartIndex()));
            p2Index = insertIndex;
        }

        final int p1Index = workingVertices.indexOf(p1);

        SplitCandidate c1 = extractSubCandidate(workingVertices, workingPassability, p1Index, p2Index);
        SplitCandidate c2 = extractSubCandidate(workingVertices, workingPassability, p2Index, p1Index);
        return Set.of(c1, c2);
    }

    private SplitCandidate extractSubCandidate(
            List<Point2D> vertices, List<Boolean> passability, int startIndex, int endIndex) {

        List<Point2D> polyVertices = new ArrayList<>();
        List<Boolean> polyPassability = new ArrayList<>();
        final int n = vertices.size();

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
}

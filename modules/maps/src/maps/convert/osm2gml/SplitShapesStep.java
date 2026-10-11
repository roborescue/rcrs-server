package maps.convert.osm2gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import maps.convert.ConvertStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Splits any shapes that overlap.
 */
public class SplitShapesStep extends ConvertStep {
    private final TemporaryMap map;

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitShapesStep.class);

    /**
     * Constructs a new {@code SplitShapesStep}.
     *
     * @param map the map
     */
    public SplitShapesStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Splitting overlapping shapes";
    }

    @Override
    protected void step() {
        Collection<TemporaryObject> all = new HashSet<>(map.getAllObjects());
        setProgressLimit(all.size());
        int count = 0;
        for (TemporaryObject shape : all) {
            count += splitShapeIfRequired(shape);
            bumpProgress();
        }
        setStatus("Added " + count + " new shapes");
    }

    private int splitShapeIfRequired(TemporaryObject shape) {
        Set<DirectedEdge> edgesRemaining = new HashSet<>(shape.getEdges());
        boolean firstShape = true;
        int newShapeCount = 0;

        while (!edgesRemaining.isEmpty()) {
            DirectedEdge dEdge = edgesRemaining.iterator().next();
            Node start = dEdge.getStartNode();
            Node end = dEdge.getEndNode();

            List<DirectedEdge> result = new ArrayList<>();
            result.add(dEdge);
            edgesRemaining.remove(dEdge); // Remove edge as it is used

            LOGGER.debug("Starting walk from {}", dEdge);

            while (!end.equals(start)) {
                Set<Edge> candidates = new HashSet<>(map.getAttachedEdges(end));

                candidates.remove(dEdge.getEdge());

                Edge turn = ConvertTools.findLeftTurn(dEdge, candidates);

                // If no left turn is found (e.g., we are at a dead end), break the loop.
                if (turn == null) {
                    result.clear();
                    break;
                }

                dEdge = new DirectedEdge(turn, end);
                end = dEdge.getEndNode();

                // If we are removing a directed edge that has the opposite direction in the set.
                if (!edgesRemaining.remove(dEdge) && !edgesRemaining.remove(dEdge.getReverse())) {
                    LOGGER.warn("Walked along an edge not in the original shape: {}. Abandoning path.", dEdge);
                    result.clear();
                    break;
                }

                result.add(dEdge);
                LOGGER.debug("Added {}, new end: {}", dEdge, end);
            }

            // If the inner loop was broken, result will be empty.
            // Only process if we found a valid, closed loop.
            if (result.isEmpty() || !end.equals(start)) {
                continue;
            }

            if (!firstShape || !edgesRemaining.isEmpty()) {
                // Didn't cover all edges so new shapes are needed.
                if (firstShape) {
                    map.removeTemporaryObject(shape);
                    firstShape = false;
                }
                else {
                    ++newShapeCount;
                }
                TemporaryObject newObject = shape.copyWithEdges(result);
                map.addTemporaryObject(newObject);
            }
        }
        return newShapeCount;
    }
}

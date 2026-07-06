package maps.convert.osm2gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import maps.convert.ConvertStep;

import rescuecore2.log.Logger;

/**
   This step splits any shapes that overlap.
*/
public class SplitShapesStep extends ConvertStep {
    private TemporaryMap map;

    /**
       Construct a SplitFacesStep.
       @param map The map to use.
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
        Collection<TemporaryObject> all = map.getAllObjects();
        setProgressLimit(all.size());
        int count = 0;
        debug.setBackground(ConvertTools.getAllDebugShapes(map));
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

            Logger.debug("Starting walk from " + dEdge);

            while (!end.equals(start)) {
                Set<Edge> candidates = map.getAttachedEdges(end);

                candidates.remove(dEdge.getEdge());

                Edge turn = ConvertTools.findLeftTurn(dEdge, candidates);

                // If no left turn is found (e.g., we are at a dead end), break the loop.
                if (turn == null) {
                    Logger.warn("Could not find a closed loop starting from " + result.get(0) + ". Abandoning path.");
                    result.clear();
                    break;
                }

                DirectedEdge newDEdge = new DirectedEdge(turn, end);

                dEdge = newDEdge;
                end = dEdge.getEndNode();

                // If we are removing a directed edge that has the opposite direction in the set.
                if (!edgesRemaining.remove(dEdge) && !edgesRemaining.remove(dEdge.getReverse())) {
                    Logger.warn("Walked along an edge not in the original shape: " + dEdge + ". Abandoning path.");
                    result.clear();
                    break;
                }

                result.add(dEdge);
                Logger.debug("Added " + dEdge + ", new end: " + end);
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
                TemporaryObject newObject = null;
                if (shape instanceof TemporaryRoad) {
                    newObject = new TemporaryRoad(result);
                }
                if (shape instanceof TemporaryIntersection) {
                    newObject = new TemporaryIntersection(result);
                }
                if (shape instanceof TemporaryBuilding) {
                    newObject = new TemporaryBuilding(result, ((TemporaryBuilding)shape).getOsmId());
                }
                if (newObject != null) {
                    map.addTemporaryObject(newObject);
                }
            }
        }
        return newShapeCount;
    }
}

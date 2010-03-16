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
        Set<DirectedEdge> edgesRemaining = new HashSet<DirectedEdge>(shape.getEdges());
        boolean firstShape = true;
        int newShapeCount = 0;
        //        debug.show("Splitting shapes", new TemporaryObjectInfo(shape, "Shape", Constants.BLACK, Constants.TRANSPARENT_ORANGE));
        //        Logger.debug("Splitting shape " + shape);
        //        Logger.debug("Edges: ");
        //        for (DirectedEdge e : edgesRemaining) {
        //            Logger.debug("  " + e);
        //        }
        while (!edgesRemaining.isEmpty()) {
            //            Logger.debug(edgesRemaining.size() + " edges remaining");
            DirectedEdge dEdge = edgesRemaining.iterator().next();
            edgesRemaining.remove(dEdge);
            Node start = dEdge.getStartNode();
            Node end = dEdge.getEndNode();
            List<DirectedEdge> result = new ArrayList<DirectedEdge>();
            result.add(dEdge);
            // Now walk around
            Logger.debug("Starting walk from " + dEdge);
            Logger.debug("Start: " + start);
            Logger.debug("End: " + end);
            while (!end.equals(start)) {
                Set<Edge> candidates = map.getAttachedEdges(end);
                //                Logger.debug("From edge: " + dEdge);
                //                Logger.debug("Candidates: ");
                //                for (Edge e : candidates) {
                //                    Logger.debug("  " + e);
                //                }
                Edge turn = ConvertTools.findLeftTurn(dEdge, candidates);
                Logger.debug("Best turn: " + turn);
                DirectedEdge newDEdge = new DirectedEdge(turn, end);
                //                debug.show("Splitting shapes",
                //                           new TemporaryObjectInfo(shape, "Shape", Constants.BLACK, Constants.TRANSPARENT_ORANGE),
                //                           new ShapeDebugFrame.Line2DShapeInfo(dEdge.getLine(), "Edge", Constants.BLUE, true, true),
                //                           new ShapeDebugFrame.Line2DShapeInfo(newDEdge.getLine(), "Turn", Constants.RED, true, true));
                dEdge = newDEdge;
                end = dEdge.getEndNode();
                edgesRemaining.remove(dEdge);
                result.add(dEdge);
                Logger.debug("Added " + dEdge);
                Logger.debug("New end: " + end);
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
                    newObject = new TemporaryBuilding(result, ((TemporaryBuilding)shape).getBuildingID());
                }
                map.addTemporaryObject(newObject);
                //                debug.show("Splitting shapes",
                //                           new TemporaryObjectInfo(shape, "Original shape", Constants.BLACK, null),
                //                           new TemporaryObjectInfo(newObject, "New shape", Constants.RED, Constants.TRANSPARENT_RED));
            }
        }
        return newShapeCount;
    }
}

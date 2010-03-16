package maps.convert.osm2gml;

import maps.gml.GMLMap;

import maps.convert.ConvertStep;

/**
   This step removes extra nodes and edges.
*/
public class PruneStep extends ConvertStep {
    //    private GMLMap gmlMap;

    /**
       Construct a PruneStep.
       @param gmlMap The GMLMap to use.
    */
    public PruneStep(GMLMap gmlMap) {
        super();
        //        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Pruning nodes and edges";
    }

    @Override
    protected void step() {
        /*
        setProgressLimit(gmlMap.getEdges().size() + gmlMap.getNodes().size());
        int edgeCount = 0;
        int nodeCount = 0;
        // Any edge that is not part of a face can be pruned
        setStatus("Pruning edges");
        for (GMLEdge next : gmlMap.getEdges()) {
            if (gmlMap.getAttachedFaces(next).isEmpty()) {
                gmlMap.removeEdge(next);
                ++edgeCount;
            }
            bumpProgress();
        }
        // Any node that is not part of an edge can be pruned
        setStatus("Pruning nodes");
        for (GMLNode next : gmlMap.getNodes()) {
            if (gmlMap.getAttachedEdges(next).isEmpty()) {
                gmlMap.removeNode(next);
                ++nodeCount;
            }
            bumpProgress();
        }
        setStatus("Removed " + edgeCount + " edges and " + nodeCount + " nodes");
        */
    }
}

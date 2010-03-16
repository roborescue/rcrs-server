package maps.convert.osm2gml;

import maps.gml.GMLMap;

import maps.convert.ConvertStep;

/**
   This class computes the entrances for buildings.
*/
public class CreateEntrancesStep extends ConvertStep {
    //    private GMLMap gmlMap;

    /**
       Construct a CreateEntrancesStep.
       @param gmlMap The GMLMap to use.
    */
    public CreateEntrancesStep(GMLMap gmlMap) {
        super();
        //        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Creating building entrances";
    }

    @Override
    protected void step() {
        /*
        setProgressLimit(gmlMap.getFaces().size());
        int sharedCount = 0;
        int corridorCount = 0;
        for (GMLFace face : gmlMap.getFaces()) {
            if (FaceType.BUILDING.equals(face.getFaceType())) {
                // Look to see if we have any edges shared with a road
                boolean found = false;
                for (GMLDirectedEdge directedEdge : face.getEdges()) {
                    GMLEdge edge = directedEdge.getEdge();
                    if (isSharedWithRoad(edge)) {
                        // Make the edge passable
                        // TO DO: Make part of the edge passable
                        // TO DO: Make more edges passable if this edge is too short
                        edge.setPassable(true);
                        found = true;
                        ++sharedCount;
                        break;
                    }
                }
                // If we couldn't find a shared edge then we need to create a corridor that connects an edge to a road.
                if (!found) {
                    makeCorrider(face);
                    ++corridorCount;
                }
            }
            bumpProgress();
        }
        setStatus("Made " + sharedCount + " shared edges passable and created " + corridorCount + " corridors");
        */
    }

    /*
    private boolean isSharedWithRoad(GMLEdge edge) {
        for (GMLFace face : gmlMap.getAttachedFaces(edge)) {
            if (FaceType.ROAD.equals(face.getFaceType())) {
                return true;
            }
        }
        return false;
    }

    private void makeCorrider(GMLFace face) {
        // Find an edge that is close to a road or intersection
        GMLEdge bestBuildingEdge = null;
        GMLEdge bestRoadEdge = null;
        for (GMLDirectedEdge next : face.getEdges()) {
            GMLEdge buildingEdge = next.getEdge();
            // Look for the nearest road or intersection edge
        }
    }
    */
}

package maps.convert.osm2gml;

import maps.convert.ConvertStep;

import java.util.Collection;

/**
   This step computes which edges are passable and sets up neighbours accordingly.
*/
public class ComputePassableEdgesStep extends ConvertStep {
    private TemporaryMap map;

    /**
       Construct a ComputePassableEdgesStep.
       @param map The TemporaryMap to use.
    */
    public ComputePassableEdgesStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Computing passable edges";
    }

    @Override
    protected void step() {
        setProgressLimit(map.getAllEdges().size());
        // For each edge see if it is shared by two road faces
        // If so, make it passable.
        int count = 0;
        for (Edge next : map.getAllEdges()) {
            int roadCount = 0;
            Collection<TemporaryObject> attached = map.getAttachedObjects(next);
            for (TemporaryObject o : attached) {
                if (o instanceof TemporaryRoad || o instanceof TemporaryIntersection) {
                    ++roadCount;
                }
            }
            if (roadCount > 1) {
                // Edge is passable. Make the neighbours.
                for (TemporaryObject o1 : attached) {
                    for (TemporaryObject o2 : attached) {
                        if (o1 == o2) {
                            continue;
                        }
                        o1.setNeighbour(next, o2);
                    }
                }
                ++count;
            }
            bumpProgress();
        }
        setStatus("Made " + count + " edges passable");
    }
}

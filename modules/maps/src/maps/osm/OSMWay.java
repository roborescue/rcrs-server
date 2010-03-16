package maps.osm;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
   An OSM way.
*/
public abstract class OSMWay extends OSMObject {
    private List<Long> ids;

    /**
       Construct an OSMWay.
       @param id The ID of the way.
       @param ids The IDs of the nodes of the way.
     */
    public OSMWay(Long id, List<Long> ids) {
        super(id);
        this.ids = ids;
    }

    /**
       Get the IDs of the way nodes.
       @return The IDs of the nodes of this way.
     */
    public List<Long> getNodeIDs() {
        return new ArrayList<Long>(ids);
    }

    /**
       Set the IDs of the way nodes.
       @param newIDs The new IDs of the nodes of this way.
     */
    public void setNodeIDs(List<Long> newIDs) {
        ids = newIDs;
    }

    /**
       Replace a node ID in this way.
       @param oldID The old node ID.
       @param newID The new node ID.
    */
    public void replace(Long oldID, Long newID) {
        Collections.replaceAll(ids, oldID, newID);
    }
}

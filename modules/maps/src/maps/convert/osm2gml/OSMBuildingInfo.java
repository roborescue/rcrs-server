package maps.convert.osm2gml;

import maps.osm.OSMBuilding;
import maps.osm.OSMMap;
import maps.osm.OSMNode;

import rescuecore2.misc.geometry.Point2D;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
   Information about an OSM building.
*/
public class OSMBuildingInfo implements OSMShape {
    private List<Point2D> vertices;
    private Area area;
    private long buildingID;

    /**
       Construct a new OSMBuildingInfo.
       @param building The building.
       @param map The map.
    */
    public OSMBuildingInfo(OSMBuilding building, OSMMap map) {
        buildingID = building.getID();
        vertices = new ArrayList<Point2D>();
        for (Long next : building.getNodeIDs()) {
            OSMNode node = map.getNode(next);
            vertices.add(new Point2D(node.getLongitude(), node.getLatitude()));
        }
        // Compute the area
        Path2D.Double path = new Path2D.Double();
        Iterator<Point2D> it = vertices.iterator();
        Point2D point = it.next();
        path.moveTo(point.getX(), point.getY());
        while (it.hasNext()) {
            point = it.next();
            path.lineTo(point.getX(), point.getY());
        }
        path.closePath();
        area = new Area(path.createTransformedShape(null));
    }

    @Override
    public List<Point2D> getVertices() {
        return vertices;
    }

    @Override
    public Area getArea() {
        return area;
    }

    /**
       Get the ID of the building.
       @return The building ID.
    */
    public long getBuildingID() {
        return buildingID;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("BuildingInfo [");
        for (Iterator<Point2D> it = vertices.iterator(); it.hasNext();) {
            result.append(it.next().toString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }
}

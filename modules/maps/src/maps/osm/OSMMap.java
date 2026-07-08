package maps.osm;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.*;

import java.io.File;
import java.io.IOException;

/**
   An OpenStreetMap map.
*/
public class OSMMap {

    private Map<Long, OSMNode> nodes;
    private Map<Long, OSMRoad> roads;
    private Map<Long, OSMBuilding> buildings;

    private boolean boundsCalculated;
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;

    /**
       Construct an empty map.
    */
    public OSMMap() {
        boundsCalculated = false;
        nodes = new HashMap<Long, OSMNode>();
        roads = new HashMap<Long, OSMRoad>();
        buildings = new HashMap<Long, OSMBuilding>();
    }

    /**
       Construct a map from an XML document.
       @param doc The document to read.
    */
    public OSMMap(Document doc) throws OSMException {
        this();
        read(doc);
    }

    /**
       Construct a map from an XML file.
       @param file The file to read.
    */
    public OSMMap(File file) throws OSMException, DocumentException, IOException {
        this();
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        read(doc);
    }

    /**
       Construct a copy of an OSMMap over a bounded area.
       @param other The map to copy.
       @param minLat The minimum latitude of the new map.
       @param minLon The minimum longitude of the new map.
       @param maxLat The maximum latitude of the new map.
       @param maxLon The maximum longitude of the new map.
    */
    public OSMMap(OSMMap other, double minLat, double minLon, double maxLat, double maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
        boundsCalculated = true;
        nodes = new HashMap<Long, OSMNode>();
        roads = new HashMap<Long, OSMRoad>();
        buildings = new HashMap<Long, OSMBuilding>();
        // Copy all nodes inside the bounds
        for (OSMNode next : other.nodes.values()) {
            double lat = next.getLatitude();
            double lon = next.getLongitude();
            long id = next.getID();
            if (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon) {
                this.nodes.put(id, new OSMNode(id, lat, lon));
            }
        }
        // Now copy the bits of roads and buildings that do not have missing nodes
        for (OSMRoad next : other.roads.values()) {
            List<Long> ids = new ArrayList<Long>(next.getNodeIDs());
            for (Iterator<Long> it = ids.iterator(); it.hasNext();) {
                Long nextID = it.next();
                if (!nodes.containsKey(nextID)) {
                    it.remove();
                }
            }
            if (!ids.isEmpty()) {
                roads.put(next.getID(), new OSMRoad(next.getID(), ids, next.getType()));
            }
        }
        for (OSMBuilding next : other.buildings.values()) {
            boolean allFound = true;
            for (Long nextID : next.getNodeIDs()) {
                if (!nodes.containsKey(nextID)) {
                    allFound = false;
                }
            }
            if (allFound) {
                buildings.put(next.getID(), new OSMBuilding(next.getID(), new ArrayList<Long>(next.getNodeIDs())));
            }
        }
    }

    /**
       Read an XML document and populate this map.
       @param doc The document to read.
    */
    public void read(Document doc) throws OSMException {
        boundsCalculated = false;
        nodes = new HashMap<Long, OSMNode>();
        roads = new HashMap<Long, OSMRoad>();
        buildings = new HashMap<Long, OSMBuilding>();
        Element root = doc.getRootElement();
        if (!"osm".equals(root.getName())) {
            throw new OSMException("Invalid map file: root element must be 'osm', not " + root.getName());
        }
        for (Object next : root.elements("node")) {
            Element e = (Element)next;
            OSMNode node = processNode(e);
        }
        for (Object next : root.elements("way")) {
            Element e = (Element)next;
            processWay(e);
        }
    }

    /**
       Turn this map into XML.
       @return A new XML document.
    */
    public Document toXML() {
        Element root = DocumentHelper.createElement("osm");
        Element bounds = root.addElement("bounds");
        calculateBounds();
        bounds.addAttribute("minlat", String.valueOf(minLat));
        bounds.addAttribute("maxlat", String.valueOf(maxLat));
        bounds.addAttribute("minlon", String.valueOf(minLon));
        bounds.addAttribute("maxlon", String.valueOf(maxLon));
        for (OSMNode next : nodes.values()) {
            Element node = root.addElement("node");
            node.addAttribute("id", String.valueOf(next.getID()));
            node.addAttribute("lat", String.valueOf(next.getLatitude()));
            node.addAttribute("lon", String.valueOf(next.getLongitude()));
        }
        for (OSMRoad next : roads.values()) {
            Element node = root.addElement("way");
            node.addAttribute("id", String.valueOf(next.getID()));
            for (Long nextID : next.getNodeIDs()) {
                node.addElement("nd").addAttribute("ref", String.valueOf(nextID));
            }
            node.addElement("tag").addAttribute("k", "highway").addAttribute("v", "primary");
        }
        for (OSMBuilding next : buildings.values()) {
            Element node = root.addElement("way");
            node.addAttribute("id", String.valueOf(next.getID()));
            for (Long nextID : next.getNodeIDs()) {
                node.addElement("nd").addAttribute("ref", String.valueOf(nextID));
            }
            node.addElement("tag").addAttribute("k", "building").addAttribute("v", "yes");
        }
        return DocumentHelper.createDocument(root);
    }

    /**
       Get the minimum longitude in this map.
       @return The minimum longitude.
    */
    public double getMinLongitude() {
        calculateBounds();
        return minLon;
    }

    /**
       Get the maximum longitude in this map.
       @return The maximum longitude.
    */
    public double getMaxLongitude() {
        calculateBounds();
        return maxLon;
    }

    /**
       Get the centre longitude in this map.
       @return The centre longitude.
    */
    public double getCentreLongitude() {
        calculateBounds();
        return (maxLon + minLon) / 2;
    }

    /**
       Get the minimum latitude in this map.
       @return The minimum latitude.
    */
    public double getMinLatitude() {
        calculateBounds();
        return minLat;
    }

    /**
       Get the maximum latitude in this map.
       @return The maximum latitude.
    */
    public double getMaxLatitude() {
        calculateBounds();
        return maxLat;
    }

    /**
       Get the centre latitude in this map.
       @return The centre latitude.
    */
    public double getCentreLatitude() {
        calculateBounds();
        return (maxLat + minLat) / 2;
    }

    /**
       Get all nodes in the map.
       @return All nodes.
    */
    public Collection<OSMNode> getNodes() {
        return new HashSet<OSMNode>(nodes.values());
    }

    /**
       Remove a node.
       @param node The node to remove.
    */
    public void removeNode(OSMNode node) {
        nodes.remove(node.getID());
    }

    /**
       Get a node by ID.
       @param id The ID of the node.
       @return The node with the given ID or null.
    */
    public OSMNode getNode(Long id) {
        return nodes.get(id);
    }

    /**
       Get the nearest node to a point.
       @param lat The latitude of the point.
       @param lon The longitude of the point.
       @return The nearest node.
    */
    public OSMNode getNearestNode(double lat, double lon) {
        double smallest = Double.MAX_VALUE;
        OSMNode best = null;
        for (OSMNode next : nodes.values()) {
            double d1 = next.getLatitude() - lat;
            double d2 = next.getLongitude() - lon;
            double d = (d1 * d1) + (d2 * d2);
            if (d < smallest) {
                best = next;
                smallest = d;
            }
        }
        return best;
    }

    /**
       Replace a node and update all references.
       @param old The node to replace.
       @param replacement The replacement node.
    */
    public void replaceNode(OSMNode old, OSMNode replacement) {
        for (OSMRoad r : roads.values()) {
            r.replace(old.getID(), replacement.getID());
        }
        for (OSMBuilding b : buildings.values()) {
            b.replace(old.getID(), replacement.getID());
        }
        removeNode(old);
    }

    /**
       Get all roads.
       @return All roads.
    */
    public Collection<OSMRoad> getRoads() {
        return new HashSet<OSMRoad>(roads.values());
    }

    /**
       Remove a road.
       @param road The road to remove.
    */
    public void removeRoad(OSMRoad road) {
        roads.remove(road.getID());
    }

    /**
       Get all buildings.
       @return All buildings.
    */
    public Collection<OSMBuilding> getBuildings() {
        return new HashSet<OSMBuilding>(buildings.values());
    }

    /**
       Remove a building.
       @param building The building to remove.
    */
    public void removeBuilding(OSMBuilding building) {
        buildings.remove(building.getID());
    }

    private void calculateBounds() {
        if (boundsCalculated) {
            return;
        }
        minLat = Double.POSITIVE_INFINITY;
        maxLat = Double.NEGATIVE_INFINITY;
        minLon = Double.POSITIVE_INFINITY;
        maxLon = Double.NEGATIVE_INFINITY;
        for (OSMNode node : nodes.values()) {
            minLat = Math.min(minLat, node.getLatitude());
            maxLat = Math.max(maxLat, node.getLatitude());
            minLon = Math.min(minLon, node.getLongitude());
            maxLon = Math.max(maxLon, node.getLongitude());
        }
        boundsCalculated = true;
    }

    private OSMNode processNode(Element e) {
        long id = Long.parseLong(e.attributeValue("id"));
        double lat = Double.parseDouble(e.attributeValue("lat"));
        double lon = Double.parseDouble(e.attributeValue("lon"));
        OSMNode node = new OSMNode(id, lat, lon);
        nodes.put(id, node);
        return node;
    }

    private void processWay(Element e) {
        long id = Long.parseLong(e.attributeValue("id"));
        List<Long> ids = new ArrayList<>();

        for (final Element next : e.elements("nd")) {
            Long nextID = Long.parseLong(next.attributeValue("ref"));
            ids.add(nextID);
        }

        for (final Element tag : e.elements("tag")) {
            String key = tag.attributeValue("k");
            String value = tag.attributeValue("v");

            if ("building".equals(key) && "yes".equals(value)) {
                final OSMBuilding building = new OSMBuilding(id, ids);
                buildings.put(id, building);
                return;
            }

            final Optional<OSMRoadType> type = OSMRoadType.fromTagValue(value);
            if ("highway".equals(key) && type.isPresent()) {
                final OSMRoad road = new OSMRoad(id, ids, type.get());
                roads.put(id, road);
                return;
            }

            // Check if this object is on a different level (bridge, tunnel, etc.)
            if ("layer".equals(key) && !"0".equals(value) ||
                "bridge".equals(key) && "yes".equals(value) ||
                "tunnel".equals(key) && "yes".equals(value)) return;
        }
    }
}

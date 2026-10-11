package maps.osm;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.*;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

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
        nodes = new HashMap<>();
        roads = new HashMap<>();
        buildings = new HashMap<>();
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
        nodes = new HashMap<>();
        roads = new HashMap<>();
        buildings = new HashMap<>();
        // Copy all nodes inside the bounds
        for (OSMNode next : other.nodes.values()) {
            double lat = next.getLatitude();
            double lon = next.getLongitude();
            long id = next.getId();
            if (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon) {
                this.nodes.put(id, new OSMNode(id, lat, lon));
            }
        }
        // Now copy the bits of roads and buildings that do not have missing nodes
        for (OSMRoad next : other.roads.values()) {
            List<Long> ids = new ArrayList<>(next.getNodeIDs());
            ids.removeIf(nextID -> !nodes.containsKey(nextID));
            if (!ids.isEmpty()) {
                roads.put(next.getId(), new OSMRoad(next));
            }
        }
        for (OSMBuilding next : other.buildings.values()) {
            boolean allFound = true;
            for (Long nextID : next.getNodeIDs()) {
                if (!nodes.containsKey(nextID)) {
                    allFound = false;
                    break;
                }
            }
            if (allFound) {
                buildings.put(next.getId(), new OSMBuilding(next.getId(), new ArrayList<>(next.getNodeIDs())));
            }
        }
    }

    /**
       Read an XML document and populate this map.
       @param doc The document to read.
    */
    public void read(Document doc) throws OSMException {
        boundsCalculated = false;
        nodes = new HashMap<>();
        roads = new HashMap<>();
        buildings = new HashMap<>();
        Element root = doc.getRootElement();
        if (!"osm".equals(root.getName())) {
            throw new OSMException("Invalid map file: root element must be 'osm', not " + root.getName());
        }
        processNodes(root);
        processWays(root);
        processRelations(root);
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
            node.addAttribute("id", String.valueOf(next.getId()));
            node.addAttribute("lat", String.valueOf(next.getLatitude()));
            node.addAttribute("lon", String.valueOf(next.getLongitude()));
        }
        for (OSMRoad next : roads.values()) {
            Element node = root.addElement("way");
            node.addAttribute("id", String.valueOf(next.getId()));
            for (Long nextID : next.getNodeIDs()) {
                node.addElement("nd").addAttribute("ref", String.valueOf(nextID));
            }
            node.addElement("tag").addAttribute("k", "highway").addAttribute("v", "primary");
        }
        for (OSMBuilding next : buildings.values()) {
            Element node = root.addElement("way");
            node.addAttribute("id", String.valueOf(next.getId()));
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
       Get the center longitude in this map.
       @return The center longitude.
    */
    public double getCenterLongitude() {
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
       Get the center latitude in this map.
       @return The center latitude.
    */
    public double getCenterLatitude() {
        calculateBounds();
        return (maxLat + minLat) / 2;
    }

    /**
       Get all nodes in the map.
       @return All nodes.
    */
    public Collection<OSMNode> getNodes() {
        return new HashSet<>(nodes.values());
    }

    /**
       Remove a node.
       @param node The node to remove.
    */
    public void removeNode(OSMNode node) {
        nodes.remove(node.getId());
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
            r.replace(old.getId(), replacement.getId());
        }
        for (OSMBuilding b : buildings.values()) {
            b.replace(old.getId(), replacement.getId());
        }
        removeNode(old);
    }

    /**
       Get all roads.
       @return All roads.
    */
    public Collection<OSMRoad> getRoads() {
        return new HashSet<>(roads.values());
    }

    /**
       Remove a road.
       @param road The road to remove.
    */
    public void removeRoad(OSMRoad road) {
        roads.remove(road.getId());
    }

    /**
       Get all buildings.
       @return All buildings.
    */
    public Collection<OSMBuilding> getBuildings() {
        return new HashSet<>(buildings.values());
    }

    /**
       Remove a building.
       @param building The building to remove.
    */
    public void removeBuilding(OSMBuilding building) {
        buildings.remove(building.getId());
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

    private void processNodes(Element root) {
        root.elements("node").forEach(this::processNode);
    }

    private void processNode(Element e) {
        long id = Long.parseLong(e.attributeValue("id"));
        double lat = Double.parseDouble(e.attributeValue("lat"));
        double lon = Double.parseDouble(e.attributeValue("lon"));
        OSMNode node = new OSMNode(id, lat, lon);
        nodes.put(id, node);
    }

    private void processWays(Element root) {
        root.elements("way").forEach(this::processWay);
    }

    private void processWay(final Element e) {
        long id = Long.parseLong(e.attributeValue("id"));
        List<Long> ids = new ArrayList<>();
        for (Element next : e.elements("nd")) {
            ids.add(Long.parseLong(next.attributeValue("ref")));
        }
        Map<String, String> tags = readTags(e);

        // Buildings take priority over roads, as before
        if (OSMBuilding.isBuildingTagValue(tags.get("building"))) {
            buildings.put(id, new OSMBuilding(id, ids));
            return;
        }

        // Ignore ways whose "highway" value is absent or unsupported
        Optional<OSMRoadType> type = OSMRoadType.fromTagValue(tags.get("highway"));
        if (type.isEmpty()) return;

        int laneCount = Optional.ofNullable(tags.get("lanes")).map(Integer::parseInt).orElse(-1);
        roads.put(id, new OSMRoad(id, ids, type.get(), laneCount));
    }

    // Converts multipolygon relations tagged as buildings into buildings
    private void processRelations(Element root) {
        Map<Long, List<Long>> wayNodes = readWayNodes(root);

        // Relation IDs may collide with IDs, so use IDs above all way IDs
        long nextId = wayNodes.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;

        for (Element relation : root.elements("relation")) {
            Map<String, String> tags = readTags(relation);
            if (!"multipolygon".equals(tags.get("type"))) continue;
            if (!OSMBuilding.isBuildingTagValue(tags.get("building"))) continue;

            for (List<Long> ring : outerRings(relation, wayNodes)) {
                buildings.put(nextId, new OSMBuilding(nextId, ring));
                nextId++;
            }
        }
    }

    // Reads the node IDs of all ways, including untagged ways that make up rings
    private Map<Long, List<Long>> readWayNodes(Element root) {
        Map<Long, List<Long>> wayNodes = new HashMap<>();
        for (Element way : root.elements("way")) {
            wayNodes.put(Long.parseLong(way.attributeValue("id")), readNodeIDs(way));
        }
        return wayNodes;
    }

    // Assembles the closed outer rings of a multipolygon relation
    private List<List<Long>> outerRings(Element relation, Map<Long, List<Long>> wayNodes) {
        List<List<Long>> segments = relation.elements("member").stream()
                .filter(member -> "way".equals(member.attributeValue("type")))
                .filter(member -> "outer".equals(member.attributeValue("role")))
                .map(member -> wayNodes.get(Long.parseLong(member.attributeValue("ref"))))
                // Members missing from the extract are skipped
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return OSMRingAssembler.assemble(segments);
    }

    // Reads the node IDs reference by a way
    private List<Long> readNodeIDs(Element way) {
        return way.elements("nd").stream()
                .map(nd -> Long.parseLong(nd.attributeValue("ref")))
                .collect(Collectors.toList());
    }

    // Collect the key-value pairs of all tags of a way
    private Map<String, String> readTags(Element way) {
        Map<String, String> tags = new HashMap<>();
        way.elements("tag").forEach(tag -> tags.put(tag.attributeValue("k"), tag.attributeValue("v")));
        return tags;
    }
}

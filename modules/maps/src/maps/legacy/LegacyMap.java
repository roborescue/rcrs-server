package maps.legacy;

import static rescuecore2.misc.EncodingTools.readInt32LE;
import static rescuecore2.misc.EncodingTools.reallySkip;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.log.Logger;

/**
   A legacy (version 0) RoboCup Rescue map.
*/
public class LegacyMap implements maps.Map {
    private Map<Integer, LegacyNode> nodes;
    private Map<Integer, LegacyRoad> roads;
    private Map<Integer, LegacyBuilding> buildings;

    /**
       Construct an empty map.
    */
    public LegacyMap() {
        nodes = new HashMap<Integer, LegacyNode>();
        roads = new HashMap<Integer, LegacyRoad>();
        buildings = new HashMap<Integer, LegacyBuilding>();
    }

    /**
       Construct a map and read from a directory.
       @param baseDir The map directory.
       @throws IOException If there is a problem reading the map.
    */
    public LegacyMap(File baseDir) throws IOException {
        this();
        read(baseDir);
    }

    /**
       Read map data from a directory.
       @param baseDir The map directory.
       @throws IOException If there is a problem reading the map.
    */
    public void read(File baseDir) throws IOException {
        nodes.clear();
        roads.clear();
        buildings.clear();
        readNodes(baseDir);
        readRoads(baseDir);
        readBuildings(baseDir);
    }

    /**
       Get all roads.
       @return All roads.
    */
    public Collection<LegacyRoad> getRoads() {
        return Collections.unmodifiableCollection(roads.values());
    }

    /**
       Get a road by ID.
       @param id The ID to look up.
       @return The road with the given ID or null if no such road exists.
    */
    public LegacyRoad getRoad(int id) {
        return roads.get(id);
    }

    /**
       Get all nodes.
       @return All nodes.
    */
    public Collection<LegacyNode> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
       Get a node by ID.
       @param id The ID to look up.
       @return The node with the given ID or null if no such node exists.
    */
    public LegacyNode getNode(int id) {
        return nodes.get(id);
    }

    /**
       Get all buildings.
       @return All buildings.
    */
    public Collection<LegacyBuilding> getBuildings() {
        return Collections.unmodifiableCollection(buildings.values());
    }

    /**
       Get a building by ID.
       @param id The ID to look up.
       @return The building with the given ID or null if no such building exists.
    */
    public LegacyBuilding getBuilding(int id) {
        return buildings.get(id);
    }

    private void readNodes(File baseDir) throws IOException {
        File f = new File(baseDir, "node.bin");
        InputStream in = new FileInputStream(f);
        // CHECKSTYLE:OFF:MagicNumber
        reallySkip(in, 12);
        // CHECKSTYLE:ON:MagicNumber
        int num = readInt32LE(in);
        Logger.debug("Reading " + num + " nodes");
        for (int i = 0; i < num; ++i) {
            LegacyNode node = new LegacyNode();
            node.read(in);
            nodes.put(node.getID(), node);
        }
    }

    private void readRoads(File baseDir) throws IOException {
        File f = new File(baseDir, "road.bin");
        InputStream in = new FileInputStream(f);
        // CHECKSTYLE:OFF:MagicNumber
        reallySkip(in, 12);
        // CHECKSTYLE:ON:MagicNumber
        int num = readInt32LE(in);
        Logger.debug("Reading " + num + " roads");
        for (int i = 0; i < num; ++i) {
            LegacyRoad road = new LegacyRoad();
            road.read(in);
            roads.put(road.getID(), road);
        }
    }

    private void readBuildings(File baseDir) throws IOException {
        File f = new File(baseDir, "building.bin");
        InputStream in = new FileInputStream(f);
        // CHECKSTYLE:OFF:MagicNumber
        reallySkip(in, 12);
        // CHECKSTYLE:ON:MagicNumber
        int num = readInt32LE(in);
        Logger.debug("Reading " + num + " buildings");
        for (int i = 0; i < num; ++i) {
            LegacyBuilding building = new LegacyBuilding();
            building.read(in);
            buildings.put(building.getID(), building);
        }
    }
}
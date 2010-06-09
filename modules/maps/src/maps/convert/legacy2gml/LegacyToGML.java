package maps.convert.legacy2gml;

import maps.legacy.LegacyMap;
import maps.legacy.LegacyMapFormat;
import maps.legacy.LegacyRoad;
import maps.legacy.LegacyNode;
import maps.legacy.LegacyBuilding;
import maps.gml.GMLMap;
import maps.gml.formats.RobocupFormat;
import maps.ScaleConversion;

import java.io.File;

import java.util.Map;
import java.util.HashMap;

import rescuecore2.log.Logger;

/**
   This class converts maps from the legacy format to GML.
*/
public final class LegacyToGML {
    private static final double MM_TO_M = 0.001;

    private LegacyToGML() {}

    /**
       Run the map convertor.
       @param args Command line arguments: legacy-mapdir gml-mapname.
    */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: LegacyToGML <legacy-mapdir> <gml-mapname>");
            return;
        }
        try {
            Logger.info("Reading legacy map");
            LegacyMap legacy = LegacyMapFormat.INSTANCE.read(new File(args[0]));
            GMLMap gml = new GMLMap();
            Logger.info("Converting");
            convert(legacy, gml);
            Logger.info("Writing GML map");
            RobocupFormat.INSTANCE.write(gml, new File(args[1]));
            Logger.info("Done");
        }
        // CHECKSTYLE:OFF:IllegalCatch
        catch (Exception e) {
            e.printStackTrace();
        }
        // CHECKSTYLE:ON:IllegalCatch
        System.exit(0);
    }

    private static void convert(LegacyMap legacy, GMLMap gml) {
        Map<Integer, RoadInfo> roadInfo = new HashMap<Integer, RoadInfo>();
        Map<Integer, NodeInfo> nodeInfo = new HashMap<Integer, NodeInfo>();
        Map<Integer, BuildingInfo> buildingInfo = new HashMap<Integer, BuildingInfo>();
        Logger.debug("Reading roads");
        for (LegacyRoad r : legacy.getRoads()) {
            roadInfo.put(r.getID(), new RoadInfo());
        }
        Logger.debug("Removing duplicate roads");
        for (LegacyNode n : legacy.getNodes()) {
            Map<Integer, LegacyRoad> roadToFarNode = new HashMap<Integer, LegacyRoad>();
            for (int rid : n.getEdges()) {
                LegacyRoad road = legacy.getRoad(rid);
                if (road == null) {
                    continue;
                }
                int farNodeId = (n.getID() == road.getHead()) ? road.getTail() : road.getHead();

                // Use the widest road
                LegacyRoad existingRoad = roadToFarNode.get(farNodeId);
                if (existingRoad != null && road.getWidth() <= existingRoad.getWidth()) {
                    roadInfo.remove(road.getID());
                }
                else if (existingRoad != null) {
                    roadInfo.remove(existingRoad.getID());
                    roadToFarNode.put(farNodeId, road);
                }
                else {
                    roadToFarNode.put(farNodeId, road);
                }
            }
        }
        Logger.debug("Reading nodes");
        for (LegacyNode n : legacy.getNodes()) {
            nodeInfo.put(n.getID(), new NodeInfo(n));
        }
        Logger.debug("Reading buildings");
        for (LegacyBuilding b : legacy.getBuildings()) {
            buildingInfo.put(b.getID(), new BuildingInfo(b));
        }
        Logger.debug("Creating intersections");
        for (NodeInfo n : nodeInfo.values()) {
            n.process(legacy, gml, roadInfo, buildingInfo);
        }
        Logger.debug("Creating roads");
        for (RoadInfo r : roadInfo.values()) {
            r.process(gml);
        }
        Logger.debug("Creating buildings");
        for (BuildingInfo b : buildingInfo.values()) {
            b.process(gml, nodeInfo, roadInfo);
        }
        // Rescale to m
        gml.convertCoordinates(new ScaleConversion(gml.getMinX(), gml.getMinY(), MM_TO_M, MM_TO_M));
    }
}

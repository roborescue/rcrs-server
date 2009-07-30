package kernel.standard;

import static rescuecore2.misc.EncodingTools.readInt32LE;
import static rescuecore2.misc.EncodingTools.reallySkip;
import static rescuecore2.misc.EntityTools.copyProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import kernel.KernelException;
import kernel.WorldModelCreator;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Civilian;

/**
   A WorldModelCreator that reads .bin files directly.
 */
public class InlineWorldModelCreator implements WorldModelCreator {
    private static final int HEADER_SIZE = 12;
    private static final int STAMINA = 10000;
    private static final int HP = 10000;

    private int nextAgentID;
    private int initialWater;

    @Override
    public WorldModel<? extends Entity> buildWorldModel(Config config) throws KernelException {
        StandardWorldModel world = new StandardWorldModel();
        initialWater = config.getIntValue("fire.tank.maximum");
        try {
            File baseDir = new File(config.getValue("gis.map.dir"));
            int maxID = readRoads(baseDir, world);
            maxID = Math.max(maxID, readNodes(baseDir, world));
            maxID = Math.max(maxID, readBuildings(baseDir, world));
            nextAgentID = maxID + 1;
            readGISIni(baseDir, world);
            MapValidator.validate(world);
            world.index(config.getIntValue("gis.map.meshsize"));
            return world;
        }
        catch (MapValidationException e) {
            throw new KernelException(e);
        }
        catch (IOException e) {
            throw new KernelException(e);
        }
    }

    private int readRoads(File baseDir, StandardWorldModel world) throws IOException {
        File roadFile = new File(baseDir, "road.bin");
        InputStream in = new FileInputStream(roadFile);
        // Skip 12 byte header
        reallySkip(in, HEADER_SIZE);
        int count = readInt32LE(in);
        System.out.print("Reading " + count + " roads");
        int max = 0;
        for (int i = 0; i < count; ++i) {
            int size = readInt32LE(in);
            int id = readInt32LE(in);
            max = Math.max(max, id);
            Road road = new Road(new EntityID(id));
            road.setHead(new EntityID(readInt32LE(in)));
            road.setTail(new EntityID(readInt32LE(in)));
            road.setLength(readInt32LE(in));
            road.setRoadKind(readInt32LE(in));
            road.setCarsToHead(readInt32LE(in));
            road.setCarsToTail(readInt32LE(in));
            road.setHumansToHead(readInt32LE(in));
            road.setHumansToTail(readInt32LE(in));
            road.setWidth(readInt32LE(in));
            road.setBlock(readInt32LE(in));
            road.setRepairCost(readInt32LE(in));
            road.setMedianStrip(readInt32LE(in) != 0);
            road.setLinesToHead(readInt32LE(in));
            road.setLinesToTail(readInt32LE(in));
            road.setWidthForWalkers(readInt32LE(in));
            world.addEntity(road);
            System.out.print(".");
        }
        System.out.println();
        return max;
    }

    private int readNodes(File baseDir, StandardWorldModel world) throws IOException {
        File nodeFile = new File(baseDir, "node.bin");
        InputStream in = new FileInputStream(nodeFile);
        // Skip 12 byte header
        reallySkip(in, HEADER_SIZE);
        int count = readInt32LE(in);
        System.out.print("Reading " + count + " nodes");
        int max = 0;
        for (int i = 0; i < count; ++i) {
            int size = readInt32LE(in);
            int id = readInt32LE(in);
            max = Math.max(max, id);
            Node node = new Node(new EntityID(id));
            node.setX(readInt32LE(in));
            node.setY(readInt32LE(in));
            int edgeCount = readInt32LE(in);
            node.setEdges(readIDs(edgeCount, in));
            node.setHasSignals(readInt32LE(in) != 0);
            node.setShortcutToTurn(readInts(edgeCount, in));
            node.setPocketToTurnAcross(readInts(edgeCount * 2, in));
            // CHECKSTYLE:OFF:MagicNumber
            node.setSignalTiming(readInts(edgeCount * 3, in));
            // CHECKSTYLE:ON:MagicNumber
            world.addEntity(node);
            System.out.print(".");
        }
        System.out.println();
        return max;
    }

    private int readBuildings(File baseDir, StandardWorldModel world) throws IOException {
        File buildingFile = new File(baseDir, "building.bin");
        InputStream in = new FileInputStream(buildingFile);
        // Skip 12 byte header
        reallySkip(in, HEADER_SIZE);
        int count = readInt32LE(in);
        System.out.print("Reading " + count + " buildings");
        int max = 0;
        for (int i = 0; i < count; ++i) {
            int size = readInt32LE(in);
            int id = readInt32LE(in);
            max = Math.max(max, id);
            Building building = new Building(new EntityID(id));
            building.setX(readInt32LE(in));
            building.setY(readInt32LE(in));
            building.setFloors(readInt32LE(in));
            building.setBuildingAttributes(readInt32LE(in));
            building.setIgnition(readInt32LE(in) != 0);
            building.setFieryness(readInt32LE(in));
            building.setBrokenness(readInt32LE(in));
            int entranceCount = readInt32LE(in);
            building.setEntrances(readIDs(entranceCount, in));
            readInt32LE(in); // Skip shape ID
            building.setGroundArea(readInt32LE(in));
            building.setTotalArea(readInt32LE(in));
            building.setBuildingCode(readInt32LE(in));
            int apexCount = readInt32LE(in);
            building.setApexes(readInts(apexCount * 2, in));
            world.addEntity(building);
            System.out.print(".");
        }
        System.out.println();
        return max;
    }

    private void readGISIni(File baseDir, StandardWorldModel world) throws IOException, KernelException {
        File file = new File(baseDir, "gisini.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = "";
        while (line != null) {
            line = in.readLine();
            if (line != null) {
                line = line.trim().toLowerCase();
                if ("".equals(line) || line.startsWith("#")) {
                    continue;
                }
                boolean found = false;
                for (Entry next : Entry.values()) {
                    found = found || next.process(line, world, this);
                }
                if (!found) {
                    System.err.println("Unrecognised line in gisini.txt: " + line);
                }
            }
        }
        in.close();
    }

    private List<EntityID> readIDs(int count, InputStream in) throws IOException {
        List<EntityID> result = new ArrayList<EntityID>(count);
        for (int i = 0; i < count; ++i) {
            result.add(new EntityID(readInt32LE(in)));
        }
        return result;
    }

    private int[] readInts(int count, InputStream in) throws IOException {
        int[] result = new int[count];
        for (int i = 0; i < count; ++i) {
            result[i] = readInt32LE(in);
        }
        return result;
    }

    /**
       Get the next agent ID.
       @return The next agent ID.
     */
    private int nextAgentID() {
        return nextAgentID++;
    }

    /**
       Get the initial water quantity for fire brigades.
       @return The initial water quantity.
     */
    private int initialWater() {
        return initialWater;
    }

    private enum Entry {
        FIRE_STATION(Pattern.compile("^firestation\\s*=\\s*(\\d+)$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID id = new EntityID(Integer.parseInt(m.group(1)));
                Building b = (Building)model.getEntity(id);
                if (b == null) {
                    throw new KernelException("Building id " + id + " not found");
                }
                // Turn it into a FireStation
                FireStation fs = new FireStation(id);
                copyProperties(b, fs);
                model.removeEntity(b);
                model.addEntity(fs);
            }
        },
        POLICE_OFFICE(Pattern.compile("^policeoffice\\s*=\\s*(\\d+)$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID id = new EntityID(Integer.parseInt(m.group(1)));
                Building b = (Building)model.getEntity(id);
                if (b == null) {
                    throw new KernelException("Building id " + id + " not found");
                }
                // Turn it into a PoliceOffice
                PoliceOffice po = new PoliceOffice(id);
                copyProperties(b, po);
                model.removeEntity(b);
                model.addEntity(po);
            }
        },
        AMBULANCE_CENTRE(Pattern.compile("^ambulancecent(?:(?:re)|(?:er))\\s*=\\s*(\\d+)$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID id = new EntityID(Integer.parseInt(m.group(1)));
                Building b = (Building)model.getEntity(id);
                if (b == null) {
                    throw new KernelException("Building id " + id + " not found");
                }
                // Turn it into an AmbulanceCentre
                AmbulanceCentre ac = new AmbulanceCentre(id);
                copyProperties(b, ac);
                model.removeEntity(b);
                model.addEntity(ac);
            }
        },
        REFUGE(Pattern.compile("^refuge\\s*=\\s*(\\d+)$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID id = new EntityID(Integer.parseInt(m.group(1)));
                Building b = (Building)model.getEntity(id);
                if (b == null) {
                    throw new KernelException("Building id " + id + " not found");
                }
                // Turn it into a Refuge
                Refuge r = new Refuge(id);
                copyProperties(b, r);
                model.removeEntity(b);
                model.addEntity(r);
            }
        },
        FIRE_BRIGADE(Pattern.compile("^firebrigade\\s*=\\s*(\\d+)(?:\\s*,\\s*(\\d+))?$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID locationID = new EntityID(Integer.parseInt(m.group(1)));
                FireBrigade fb = new FireBrigade(new EntityID(c.nextAgentID()));
                fb.setPosition(locationID);
                fb.setWater(c.initialWater());
                fb.setStamina(STAMINA);
                fb.setHP(HP);
                if (m.group(2) != null) {
                    fb.setPositionExtra(Integer.parseInt(m.group(2)));
                }
                model.addEntity(fb);
            }
        },
        POLICE_FORCE(Pattern.compile("^policeforce\\s*=\\s*(\\d+)(?:\\s*,\\s*(\\d+))?$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID locationID = new EntityID(Integer.parseInt(m.group(1)));
                PoliceForce pf = new PoliceForce(new EntityID(c.nextAgentID()));
                pf.setPosition(locationID);
                pf.setStamina(STAMINA);
                pf.setHP(HP);
                if (m.group(2) != null) {
                    pf.setPositionExtra(Integer.parseInt(m.group(2)));
                }
                model.addEntity(pf);
            }
        },
        AMBULANCE_TEAM(Pattern.compile("^ambulanceteam\\s*=\\s*(\\d+)(?:\\s*,\\s*(\\d+))?$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID locationID = new EntityID(Integer.parseInt(m.group(1)));
                AmbulanceTeam at = new AmbulanceTeam(new EntityID(c.nextAgentID()));
                at.setPosition(locationID);
                at.setStamina(STAMINA);
                at.setHP(HP);
                if (m.group(2) != null) {
                    at.setPositionExtra(Integer.parseInt(m.group(2)));
                }
                model.addEntity(at);
            }
        },
        CIVILIAN(Pattern.compile("^civilian\\s*=\\s*(\\d+)(?:\\s*,\\s*(\\d+))?$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID locationID = new EntityID(Integer.parseInt(m.group(1)));
                Civilian civ = new Civilian(new EntityID(c.nextAgentID()));
                civ.setPosition(locationID);
                civ.setStamina(STAMINA);
                civ.setHP(HP);
                if (m.group(2) != null) {
                    civ.setPositionExtra(Integer.parseInt(m.group(2)));
                }
                model.addEntity(civ);
            }
        },
        FIRE(Pattern.compile("^fire(?:point)?\\s*=\\s*(\\d+)$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID id = new EntityID(Integer.parseInt(m.group(1)));
                Building b = (Building)model.getEntity(id);
                if (b == null) {
                    throw new KernelException("Building id " + id + " not found");
                }
                b.setIgnition(true);
            }
        },
        IMPORTANT(Pattern.compile("^important(?:building)?\\s+(\\d+)\\s*=\\s*(\\d+)$")) {
            protected void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
                EntityID id = new EntityID(Integer.parseInt(m.group(1)));
                int importance = Integer.parseInt(m.group(2));
                Building b = (Building)model.getEntity(id);
                if (b == null) {
                    throw new KernelException("Building id " + id + " not found");
                }
                b.setImportance(importance);
            }
        };

        private Pattern pattern;

        private Entry(Pattern pattern) {
            this.pattern = pattern;
        }

        public boolean process(String line, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                process(m, model, c);
                return true;
            }
            return false;
        }

        protected abstract void process(Matcher m, StandardWorldModel model, InlineWorldModelCreator c) throws KernelException;
    }
}
package traffic3.simulator;

import java.util.List;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;

import rescuecore2.GUIComponent;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.components.StandardSimulator;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.number.NumberGenerator;

/**
   The Area model traffic simulator.
 */
public class TrafficSimulator extends StandardSimulator implements GUIComponent {
    private static final double STEP_TIME_MS = 100; // 100ms
    private static final double REAL_TIME = 60;
    private static final int MICROSTEPS = (int)((1000.0 / STEP_TIME_MS) * REAL_TIME);

    private static final int RESCUE_AGENT_RADIUS = 500;
    private static final int CIVILIAN_RADIUS = 200;
    private static final double RESCUE_AGENT_VELOCITY_MEAN = 0.7;
    private static final double RESCUE_AGENT_VELOCITY_SD = 0.1;
    private static final double CIVILIAN_VELOCITY_MEAN = 0.2;
    private static final double CIVILIAN_VELOCITY_SD = 0.002;

    private static final Color FIRE_BRIGADE_COLOUR = Color.RED;
    private static final Color POLICE_FORCE_COLOUR = Color.BLUE;
    private static final Color AMBULANCE_TEAM_COLOUR = Color.WHITE;
    private static final Color CIVILIAN_COLOUR = Color.GREEN;

    private WorldManager worldManager;
    private TrafficSimulatorGUI gui;

    public TrafficSimulator() {
        worldManager = new WorldManager();
        //        try {
            //            WorldManagerGUI wmg = new WorldManagerGUI(worldManager, new org.util.xml.io.XMLConfigManager());
            //            gui = new JPanel(new BorderLayout());
            //            gui.add(wmg.createMenuBar(), BorderLayout.NORTH);
            //            gui.add(wmg, BorderLayout.CENTER);
        //        }
        //        catch (java.io.IOException e) {
        //            gui = new JLabel(e.toString());
        //        }
        gui = new TrafficSimulatorGUI(worldManager);
    }

    @Override
    public JComponent getGUIComponent() {
        return gui;
    }

    @Override
    public String getGUIComponentName() {
        return "Traffic simulator";
    }

    @Override
    protected void postConnect() {
        try {
            for (StandardEntity next : model) {
                if (next instanceof Area) {
                    convertAreaToTrafficArea((Area)next);
                }
            }
            worldManager.check();
            NumberGenerator<Double> agentVelocityGenerator = new GaussianGenerator(RESCUE_AGENT_VELOCITY_MEAN, RESCUE_AGENT_VELOCITY_SD, config.getRandom());
            NumberGenerator<Double> civilianVelocityGenerator = new GaussianGenerator(CIVILIAN_VELOCITY_MEAN, CIVILIAN_VELOCITY_SD, config.getRandom());
            for (StandardEntity next : model) {
                if (next instanceof Human) {
                    convertHuman((Human)next, agentVelocityGenerator, civilianVelocityGenerator);
                }
            }
            for (StandardEntity next : model) {
                if (next instanceof Blockade) {
                    convertBlockade((Blockade)next);
                }
            }
            worldManager.notifyInputted(this);
        }
        catch (WorldManagerException e) {
            Logger.error("Error starting traffic simulator", e);
        }
        gui.initialise();
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        for (Command next : c.getCommands()) {
            if (next instanceof AKMove) {
                try {
                    handleMove((AKMove)next);
                }
                catch (WorldManagerException e) {
                    Logger.error("Error processing move", e);
                }
            }
        }
        timestep();
        for (TrafficAgent agent : worldManager.getAgentList()) {
            // Update position and positionHistory
            Human human = agent.getHuman();
            Point2D[] history = agent.getPositionHistory();
            int[] historyArray = new int[history.length * 2];
            for (int i = 0; i < history.length; ++i) {
                historyArray[i * 2] = (int)history[i].getX();
                historyArray[(i * 2) + 1] = (int)history[i].getY();
            }
            double x = agent.getX();
            double y = agent.getY();
            TrafficArea location = agent.getArea();
            if (location != null) {
                String id = location.getID();
                human.setPosition(new EntityID(Integer.parseInt(id.substring(5, id.indexOf(")")))));
                //                Logger.debug(human + " new position: " + human.getPosition());
                changes.addChange(human, human.getPositionProperty());
            }
            human.setX((int)x);
            human.setY((int)y);
            human.setPositionHistory(historyArray);
            human.setTravelDistance((int)agent.getLogDistance());
            changes.addChange(human, human.getXProperty());
            changes.addChange(human, human.getYProperty());
            changes.addChange(human, human.getPositionHistoryProperty());
            changes.addChange(human, human.getTravelDistanceProperty());
        }
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
    }

    private void convertAreaToTrafficArea(Area area) throws WorldManagerException {
        Logger.debug("Converting area to traffic area");
        Logger.debug(area.getFullDescription());
        List<Edge> edges = area.getEdges();
        String[] neighbourText = new String[edges.size()];
        for (int i = 0; i < neighbourText.length; i++) {
            EntityID n = edges.get(i).getNeighbour();
            if (n != null) {
                neighbourText[i] = "rcrs(" + n + ")";
                Logger.debug("Neighbour " + i + ": " + neighbourText[i]);
            }
        }
        double cx = area.getX();
        double cy = area.getY();
        TrafficArea result = new TrafficArea(worldManager, "rcrs(" + area.getID() + ")", cx, cy, area.getApexList(), neighbourText, area, model);
        if (area instanceof Building) {
            result.setType("building");
        }
        else if (area instanceof Refuge) {
            result.setType("refuge");
        }
        else {
            result.setType("open space");
        }
        worldManager.appendWithoutCheck(result);
    }

    private void convertHuman(Human h, NumberGenerator<Double> agentVelocityGenerator, NumberGenerator<Double> civilianVelocityGenerator) throws WorldManagerException {
        double radius = 0;
        double velocityLimit = 0;
        String type = null;
        Color colour = null;
        if (h instanceof FireBrigade) {
            type = "FireBrigade";
            radius = RESCUE_AGENT_RADIUS;
            colour = FIRE_BRIGADE_COLOUR;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof PoliceForce) {
            type = "PoliceForce";
            radius = RESCUE_AGENT_RADIUS;
            colour = POLICE_FORCE_COLOUR;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof AmbulanceTeam) {
            type = "AmbulanceTeam";
            radius = RESCUE_AGENT_RADIUS;
            colour = AMBULANCE_TEAM_COLOUR;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof Civilian) {
            type = "Civlian";
            radius = CIVILIAN_RADIUS;
            colour = CIVILIAN_COLOUR;
            velocityLimit = civilianVelocityGenerator.nextValue();
        }
        else {
            throw new IllegalArgumentException("Unrecognised agent type: " + h + " (" + h.getClass().getName() + ")");
        }
        String id = "rcrs(" + h.getID() + ")";
        TrafficAgent agent = new TrafficAgent(worldManager, id, radius, velocityLimit, h);
        agent.setLocation(h.getX(), h.getY(), 0);
        agent.setType(type);
        agent.setColor(colour);
        worldManager.appendWithoutCheck(agent);
    }

    private void convertBlockade(Blockade blockade) throws WorldManagerException {
        double cx = blockade.getX();
        double cy = blockade.getY();
        TrafficArea area = (TrafficArea)worldManager.getTrafficObject("rcrs(" + blockade.getPosition().getValue() + ")");
        int[] apexes = blockade.getApexes();
        String id = "rcrs(" + blockade.getID().getValue() + ")";
        TrafficBlockade result = new TrafficBlockade(worldManager, id, cx, cy, apexes);
        area.addBlockade(result);
        worldManager.appendWithoutCheck(result);
    }

    private void handleMove(AKMove move) throws WorldManagerException {
        Human human = (Human)model.getEntity(move.getAgentID());
        TrafficAgent agent = (TrafficAgent)worldManager.getTrafficObject("rcrs(" + human.getID() + ")");
        EntityID current = human.getPosition();
        List<EntityID> list = move.getPath();
        List<TrafficAreaNode> nodes = new ArrayList<TrafficAreaNode>();
        // Check that all elements refer to Area instances and build the list of target nodes
        // Skip the first entry if it is the agent's current position
        boolean first = true;
        for (EntityID next : list) {
            if (first && next.equals(current)) {
                first = false;
                continue;
            }
            first = false;
            Entity e = model.getEntity(next);
            if (!(e instanceof Area)) {
                Logger.warn("Rejecting move: Entity ID " + next + " is not an area: " + e);
                return;
            }
            Area a = (Area)e;
            nodes.add(worldManager.createAreaNode(a.getX(), a.getY(), 0));
        }
        int targetX = move.getDestinationX();
        int targetY = move.getDestinationY();
        if (targetX != -1 && targetY != -1) {
            nodes.add(worldManager.createAreaNode(targetX, targetY, 0));
        }
        agent.setDestination(nodes);
        Logger.debug("Agent " + agent + " path set: " + nodes);
    }

    private void timestep() {
        for (TrafficAgent agent : worldManager.getAgentList()) {
            agent.clearPositionHistory();
        }
        Logger.debug("Running " + MICROSTEPS + " microsteps");
        long start = System.currentTimeMillis();
        for (int i = 0; i < MICROSTEPS; i++) {
            microstep();
        }
        long end = System.currentTimeMillis();
        Logger.debug("Time: " + (end - start) + "ms (average " + ((end - start) / MICROSTEPS) + "ms)");
    }

    private void microstep() {
        for (TrafficAgent agent : worldManager.getAgentList()) {
            agent.plan();
        }
        for (TrafficAgent agent : worldManager.getAgentList()) {
            agent.step(STEP_TIME_MS);
        }
        worldManager.stepFinished(this);
        gui.refresh();
    }
}
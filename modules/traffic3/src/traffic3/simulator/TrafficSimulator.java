package traffic3.simulator;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Color;

import javax.swing.JComponent;

import traffic3.objects.TrafficArea;
import traffic3.objects.TrafficBlockade;
import traffic3.objects.TrafficAgent;
import traffic3.manager.TrafficManager;

import rescuecore2.GUIComponent;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.WorldModelListener;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.config.Config;
import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.components.StandardSimulator;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.number.NumberGenerator;

/**
   The Area model traffic simulator.
 */
public class TrafficSimulator extends StandardSimulator implements GUIComponent {
    private static final double STEP_TIME_MS = 100;
    private static final double REAL_TIME_S = 60;
    private static int MICROSTEPS = (int)((1000.0 / STEP_TIME_MS) * REAL_TIME_S);

    private static final int RESCUE_AGENT_RADIUS = 500;
    private static final int CIVILIAN_RADIUS = 200;
    private static double RESCUE_AGENT_VELOCITY_MEAN = 0.7;
    private static double RESCUE_AGENT_VELOCITY_SD = 0.1;
    private static double CIVILIAN_VELOCITY_MEAN = 0.2;
    private static double CIVILIAN_VELOCITY_SD = 0.002;

    private static final Color FIRE_BRIGADE_COLOUR = Color.RED;
    private static final Color POLICE_FORCE_COLOUR = Color.BLUE;
    private static final Color AMBULANCE_TEAM_COLOUR = Color.WHITE;
    private static final Color CIVILIAN_COLOUR = Color.GREEN;

    private TrafficSimulatorGUI gui;

    private TrafficManager manager;

    /**
       Construct a new TrafficSimulator.
    */
    public TrafficSimulator() {
        manager = new TrafficManager();
        gui = new TrafficSimulatorGUI(manager);
    }

    @Override
    public JComponent getGUIComponent() {
        return gui;
    }

    @Override
    public String getGUIComponentName() {
        return "Traffic simulator";
    }
    private void init(Config config) {
    	RESCUE_AGENT_VELOCITY_MEAN =config.getFloatValue("traffic3.rescue-agent.velocity.mean",RESCUE_AGENT_VELOCITY_MEAN);
        RESCUE_AGENT_VELOCITY_SD =config.getFloatValue("traffic3.rescue-agent.velocity.sd",RESCUE_AGENT_VELOCITY_SD);
        CIVILIAN_VELOCITY_MEAN =config.getFloatValue("traffic3.civilian.velocity.mean",CIVILIAN_VELOCITY_MEAN);
        CIVILIAN_VELOCITY_SD =config.getFloatValue("traffic3.civilian.velocity.sd",CIVILIAN_VELOCITY_SD);
        MICROSTEPS = (int)((config.getIntValue("kernel.agents.think-time") / STEP_TIME_MS) * REAL_TIME_S);
	}
    @Override
    protected void postConnect() {
        TrafficConstants.init(config);
        init(config);
        manager.clear();
        for (StandardEntity next : model) {
            if (next instanceof Area) {
                convertAreaToTrafficArea((Area)next);
            }
        }
        NumberGenerator<Double> agentVelocityGenerator = new GaussianGenerator(RESCUE_AGENT_VELOCITY_MEAN, RESCUE_AGENT_VELOCITY_SD, config.getRandom());
        NumberGenerator<Double> civilianVelocityGenerator = new GaussianGenerator(CIVILIAN_VELOCITY_MEAN, CIVILIAN_VELOCITY_SD, config.getRandom());
        for (StandardEntity next : model) {
            if (next instanceof Human) {
                convertHuman((Human)next, agentVelocityGenerator, civilianVelocityGenerator);
            }
            if (next instanceof Blockade) {
                convertBlockade((Blockade)next);
            }
        }
        model.addWorldModelListener(new WorldModelListener<StandardEntity>() {
                @Override
                public void entityAdded(WorldModel<? extends StandardEntity> model, StandardEntity e) {
                    if (e instanceof Blockade) {
                        convertBlockade((Blockade)e);
                    }
                }

                @Override
                public void entityRemoved(WorldModel<? extends StandardEntity> model, StandardEntity e) {
                    if (e instanceof Blockade) {
                        Blockade b = (Blockade)e;
                        TrafficBlockade block = manager.getTrafficBlockade(b);
                        block.getArea().removeBlockade(block);
                        manager.remove(block);
                    }
                }
            });
        gui.initialise();
        manager.cacheInformation(model);
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        long start = System.currentTimeMillis();
        Logger.info("Timestep " + c.getTime());
        // Clear all cached blockade information
        for (TrafficArea next : manager.getAreas()) {
            next.clearBlockadeCache();
        }
        // Clear all destinations and position history
        for (TrafficAgent agent : manager.getAgents()) {
            agent.clearPath();
            agent.clearPositionHistory();
            agent.setMobile(true);
        }
        for (Command next : c.getCommands()) {
            if (next instanceof AKMove) {
                handleMove((AKMove)next);
            }
            if (next instanceof AKLoad) {
                handleLoad((AKLoad)next, changes);
            }
            if (next instanceof AKUnload) {
                handleUnload((AKUnload)next, changes);
            }
            if (next instanceof AKRescue) {
                handleRescue((AKRescue)next, changes);
            }
            if (next instanceof AKClear) {
                handleClear((AKClear)next, changes);
            }
            if (next instanceof AKClearArea) {
                handleClearArea((AKClearArea)next, changes);
            }
            if (next instanceof AKExtinguish) {
                handleExtinguish((AKExtinguish)next, changes);
            }
        }
        // Any agents that are dead or in ambulances are immobile
        // Civilians that are injured are immobile
        // Agents that are buried are immobile
        // Civilians in refuges are immobile
        for (StandardEntity next : model) {
            if (next instanceof Human) {
                Human h = (Human)next;
                if (h.isHPDefined() && h.getHP() <= 0) {
                    Logger.debug("Agent " + h + " is dead");
                    manager.getTrafficAgent(h).setMobile(false);
                }
                if (h.isPositionDefined() && (model.getEntity(h.getPosition()) instanceof AmbulanceTeam)) {
                    Logger.debug("Agent " + h + " is in an ambulance");
                    manager.getTrafficAgent(h).setMobile(false);
                }
                if (h.isBuriednessDefined() && h.getBuriedness() > 0) {
                    Logger.debug("Agent " + h + " is buried");
                    manager.getTrafficAgent(h).setMobile(false);
                }
                if (h instanceof Civilian && h.isDamageDefined() && h.getDamage() > 0) {
                    Logger.debug("Agent " + h + " is injured");
                    manager.getTrafficAgent(h).setMobile(false);
                }
                if (h instanceof Civilian && h.isPositionDefined() && (model.getEntity(h.getPosition()) instanceof Refuge)) {
                    Logger.debug("Agent " + h + " is in a refuge");
                    manager.getTrafficAgent(h).setMobile(false);
                }
            }
        }
        timestep();
        for (TrafficAgent agent : manager.getAgents()) {
            // Update position and positionHistory for agents that were not loaded or unloaded
            Human human = agent.getHuman();
            if (!agent.isMobile()) {
                human.undefinePositionHistory();
                human.setTravelDistance(0);
                changes.addChange(human, human.getPositionHistoryProperty());
                changes.addChange(human, human.getTravelDistanceProperty());
                continue;
            }
            Point2D[] history = agent.getPositionHistory().toArray(new Point2D[0]);
            int[] historyArray = new int[history.length * 2];
            for (int i = 0; i < history.length; ++i) {
                historyArray[i * 2] = (int)history[i].getX();
                historyArray[(i * 2) + 1] = (int)history[i].getY();
            }
            double x = agent.getX();
            double y = agent.getY();
            TrafficArea location = agent.getArea();
            if (location != null) {
                human.setPosition(location.getArea().getID());
                //                Logger.debug(human + " new position: " + human.getPosition());
                changes.addChange(human, human.getPositionProperty());
            }
            human.setX((int)x);
            human.setY((int)y);
            human.setPositionHistory(historyArray);
            human.setTravelDistance((int)agent.getTravelDistance());
            changes.addChange(human, human.getXProperty());
            changes.addChange(human, human.getYProperty());
            changes.addChange(human, human.getPositionHistoryProperty());
            changes.addChange(human, human.getTravelDistanceProperty());
        }
        long end = System.currentTimeMillis();
        Logger.info("Timestep " + c.getTime() + " took " + (end - start) + " ms");
   }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
    }

    private void convertAreaToTrafficArea(Area area) {
        manager.register(new TrafficArea(area));
    }

    private void convertBlockade(Blockade b) {
        Logger.debug("Converting blockade: " + b.getFullDescription());
        Area a = (Area)model.getEntity(b.getPosition());
        Logger.debug("Area: " + a);
        TrafficArea area = manager.getTrafficArea(a);
        Logger.debug("Traffic area: " + area);
        TrafficBlockade block = new TrafficBlockade(b, area);
        manager.register(block);
        area.addBlockade(block);
    }

    private void convertHuman(Human h, NumberGenerator<Double> agentVelocityGenerator, NumberGenerator<Double> civilianVelocityGenerator) {
        double radius = 0;
        double velocityLimit = 0;
        if (h instanceof FireBrigade
            || h instanceof PoliceForce
            || h instanceof AmbulanceTeam) {
            radius = RESCUE_AGENT_RADIUS;
            velocityLimit = agentVelocityGenerator.nextValue();
        }
        else if (h instanceof Civilian) {
            radius = CIVILIAN_RADIUS;
            velocityLimit = civilianVelocityGenerator.nextValue();
        }
        else {
            throw new IllegalArgumentException("Unrecognised agent type: " + h + " (" + h.getClass().getName() + ")");
        }
        TrafficAgent agent = new TrafficAgent(h, manager, radius, velocityLimit);
        agent.setLocation(h.getX(), h.getY());
        manager.register(agent);
    }

    private void handleMove(AKMove move) {
        Human human = (Human)model.getEntity(move.getAgentID());
        TrafficAgent agent = manager.getTrafficAgent(human);
        EntityID current = human.getPosition();
        if (current == null) {
            Logger.warn("Rejecting move: Agent position is not defined");
            return;
        }
        Entity currentEntity = model.getEntity(human.getPosition());
        if (!(currentEntity instanceof Area)) {
            Logger.warn("Rejecting move: Agent position is not an area: " + currentEntity);
            return;
        }
        Area currentArea = (Area)currentEntity;
        List<EntityID> list = move.getPath();
        List<PathElement> steps = new ArrayList<PathElement>();
        Edge lastEdge = null;
        // Check that all elements refer to Area instances and build the list of target points
        // Target points between areas are the midpoint of the shared edge
        for (Iterator<EntityID> it = list.iterator(); it.hasNext();) {
            EntityID next = it.next();
            if (next.equals(current)) {
                continue;
            }
            Entity e = model.getEntity(next);
            if (!(e instanceof Area)) {
                Logger.warn("Rejecting move: Entity ID " + next + " is not an area: " + e);
                return;
            }
            Edge edge = currentArea.getEdgeTo(next);
            if (edge == null) {
                Logger.warn("Rejecting move: Entity ID " + next + " is not adjacent to " + currentArea);
                return;
            }
            Area nextArea = (Area)e;
            steps.addAll(getPathElements(human, currentArea, lastEdge, nextArea, edge));
            current = next;
            currentArea = nextArea;
        }
        int targetX = move.getDestinationX();
        int targetY = move.getDestinationY();
        if (targetX == -1 && targetY == -1) {
            targetX = currentArea.getX();
            targetY = currentArea.getY();
        }
        else if (list.isEmpty()) {
            Logger.warn("Rejecting move: Path is empty");
            return;
        }
        steps.add(new PathElement(current, null, new Point2D(targetX, targetY)));
        agent.setPath(steps);
    }

    private Collection<? extends PathElement> getPathElements(Human human, Area lastArea, Edge lastEdge, Area nextArea, Edge nextEdge) {
		if (human.getID().getValue() == 204623396) {
			System.out.println("lastArea=" + lastArea + " lastEdge=" + lastEdge + " nextArea=" + nextArea + " nextEdge=" + nextEdge);
		}
		ArrayList<PathElement> steps = new ArrayList<PathElement>();
		Point2D edgePoint = getBestPoint(nextEdge);
		Point2D centrePoint = new Point2D(lastArea.getX(), lastArea.getY());
		Point2D start;
		if (lastEdge == null) {
			start = new Point2D(human.getX(), human.getY());
			Point2D entracePoint = getEntranceOfArea(nextEdge, lastArea);
			if (entracePoint != null) {
				steps.add(new PathElement(lastArea.getID(), null, entracePoint, centrePoint));
				steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, entracePoint));
			} else
				steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint));

		} else {
			start = getMidPoint(lastEdge.getStart(), lastEdge.getEnd());
			Point2D startEntracePoint = getEntranceOfArea(lastEdge, lastArea);
			if (startEntracePoint != null)
				start = startEntracePoint;
			Point2D ced = getMidPoint(centrePoint, edgePoint);
			Point2D ce_ced = getMidPoint(centrePoint, ced);
			Point2D ed_ced = getMidPoint(edgePoint, ced);
			Point2D st_ce = getMidPoint(centrePoint, start);
			Point2D ce_stce = getMidPoint(centrePoint, st_ce);
			Point2D st_stce = getMidPoint(start, st_ce);
			if (startEntracePoint != null)
				steps.add(new PathElement(lastArea.getID(), null, startEntracePoint));
			// startEnterance st_stce st_ce ce_stce center ce_ced ced ed_ced
			// enterance edge nextEnterance
			// steps.add(new PathElement(lastArea.getID(), null, st_stce));
			// steps.add(new PathElement(lastArea.getID(), null, st_ce,
			// st_stce));
			// steps.add(new PathElement(lastArea.getID(), null, ce_stce,
			// st_ce));
			// steps.add(new PathElement(lastArea.getID(), null, centrePoint,
			// ce_stce));
			// steps.add(new PathElement(lastArea.getID(), null, ce_ced,
			// centrePoint));
			// steps.add(new PathElement(lastArea.getID(), null, ced, ce_ced));
			// steps.add(new PathElement(lastArea.getID(), null, ed_ced, ced));
			Point2D entracePoint = getEntranceOfArea(nextEdge, lastArea);
			if (entracePoint != null) {
				// steps.add(new PathElement(lastArea.getID(),
				// nextEdge.getLine(),
				// entracePoint,st_stce,st_ce,ce_stce,centrePoint,ce_ced,ced,ed_ced));
				steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), entracePoint, centrePoint));
				steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, entracePoint));
			} else {
				// steps.add(new PathElement(lastArea.getID(),
				// nextEdge.getLine(),
				// edgePoint,st_stce,st_ce,ce_stce,centrePoint,ce_ced,ced,ed_ced));
				steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, centrePoint));
			}
		}

		// Point2D nextEntracePoint = getEntranceOfArea(nextEdge, nextArea);
		// if (nextEntracePoint != null)
		// steps.add(new PathElement(nextArea.getID(), null, nextEntracePoint,
		// edgePoint));
		return steps;
	}
    private Point2D getBestPoint(Edge edge) {
		int dx = (edge.getStartX() + edge.getEndX());
		int dy = (edge.getStartY() + edge.getEndY());
		int x = dx / 2;
		int y = dy / 2;
		Line2D line = edge.getLine();
		ArrayList<Blockade> blockades = new ArrayList<Blockade>();
		Collection<StandardEntity> objects = model.getObjectsInRange(x, y, TrafficSimulator.RESCUE_AGENT_RADIUS);
		for (StandardEntity standardEntity : objects) {
			if (standardEntity instanceof Blockade) {
				Blockade blockade = (Blockade) standardEntity;
				blockades.add(blockade);
			}
		}
		Point2D centerPoint = new Point2D(x, y);
		if (getMinDistance(blockades, centerPoint) > 500)
			return centerPoint;
		Point2D bestpoint = edge.getStart();
		double bestDistance = Integer.MIN_VALUE;
		for (double i = 0; i < 1; i += .1d) {
			Point2D tempPoint = line.getPoint(i);

			double minDistance = getMinDistance(blockades, tempPoint);
			if (minDistance > bestDistance) {
				bestDistance = minDistance;
				bestpoint = tempPoint;
			}

		}
		return bestpoint;
	}
	private double getMinDistance(ArrayList<Blockade> blockades, Point2D point) {
		double min = Integer.MAX_VALUE;
		for (Blockade block : blockades) {
			double minDistance = getMinDistance(block.getApexes(), point);
			if (min > minDistance)
				min = minDistance;
		}
		return min;

	}

	private double getMinDistance(int[] apexes, Point2D point) {
		double best = Integer.MAX_VALUE;
		List<Line2D> edges = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(apexes), true);
		for (Line2D edge : edges) {
			Point2D tempPoint = GeometryTools2D.getClosestPointOnSegment(edge, point);
			double tempDistance = GeometryTools2D.getDistance(point, tempPoint);
			if (tempDistance < best)
				best = tempDistance;
		}
		return best;
	}

    private Point2D getEntranceOfArea(Edge inComingEdge, Area dest) {
		Point2D edgeMid = getMidPoint(inComingEdge.getStart(), inComingEdge.getEnd());
		Line2D wallLine = inComingEdge.getLine();

		int distance = 1000;
		while (distance > 0) {
			Vector2D offset = wallLine.getDirection().getNormal().normalised().scale(distance);
			Point2D destXY = edgeMid.plus(offset);
			if (dest.getShape().contains(destXY.getX(), destXY.getY())) {
				return destXY;
			}
			offset = wallLine.getDirection().getNormal().normalised().scale(-distance);
			destXY = edgeMid.plus(offset);
			if (dest.getShape().contains(destXY.getX(), destXY.getY())) {
				return destXY;
			}
			distance -= 100;
		}
		return null;

	}
    private Point2D getMidPoint(Point2D p1, Point2D p2) {
		return new Point2D((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
	}
    // Return the loaded civilian (if any)
    private Civilian handleLoad(AKLoad load, ChangeSet changes) {
        EntityID agentID = load.getAgentID();
        EntityID targetID = load.getTarget();
        Entity agent = model.getEntity(agentID);
        Entity target = model.getEntity(targetID);
        if (agent == null) {
            Logger.warn("Rejecting load command from agent " + agentID + ": agent does not exist");
            return null;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            Logger.warn("Rejecting load command from agent " + agentID + ": agent type is " + agent.getURN());
            return null;
        }
        if (target == null) {
            Logger.warn("Rejecting load command from agent " + agentID + ": target does not exist " + targetID);
            return null;
        }
        if (!(target instanceof Civilian)) {
            Logger.warn("Rejecting load command from agent " + agentID + ": target " + targetID + " is of type " + target.getURN());
            return null;
        }
        AmbulanceTeam at = (AmbulanceTeam)agent;
        Civilian h = (Civilian)target;
        if (at.isHPDefined() && at.getHP() <= 0) {
            Logger.warn("Rejecting load command from agent " + agentID + ": agent is dead");
            return null;
        }
        if (at.isBuriednessDefined() && at.getBuriedness() > 0) {
            Logger.warn("Rejecting load command from agent " + agentID + ": agent is buried");
            return null;
        }
        if (h.isBuriednessDefined() && h.getBuriedness() > 0) {
            Logger.warn("Rejecting load command from agent " + agentID + ": target " + targetID + " is buried");
            return null;
        }
        if (!h.isPositionDefined() || !at.isPositionDefined() || !h.getPosition().equals(at.getPosition())) {
            Logger.warn("Rejecting load command from agent " + agentID + ": target is non-adjacent " + targetID);
            return null;
        }
        if (h.getID().equals(at.getID())) {
            Logger.warn("Rejecting load command from agent " + agentID + ": tried to load self");
            return null;
        }
        // Is there something already loaded?
        for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Civilian c = (Civilian)e;
            if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
                Logger.warn("Rejecting load command from agent " + agentID + ": agent already has civilian " + c.getID() + " loaded");
                return null;
            }
        }
        // All checks passed: do the load
        h.setPosition(agentID);
        h.undefineX();
        h.undefineY();
        changes.addChange(h, h.getPositionProperty());
        changes.addChange(h, h.getXProperty());
        changes.addChange(h, h.getYProperty());
        manager.getTrafficAgent(at).setMobile(false);
        manager.getTrafficAgent(h).setMobile(false);
        Logger.debug(at + " loaded " + h);
        return h;
    }

    // Return the unloaded civilian (if any)
    private Civilian handleUnload(AKUnload unload, ChangeSet changes) {
        EntityID agentID = unload.getAgentID();
        Entity agent = model.getEntity(agentID);
        if (agent == null) {
            Logger.warn("Rejecting unload command from agent " + agentID + ": agent does not exist");
            return null;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            Logger.warn("Rejecting unload command from agent " + agentID + ": agent type is " + agent.getURN());
            return null;
        }
        AmbulanceTeam at = (AmbulanceTeam)agent;
        if (!at.isPositionDefined() || !at.isXDefined() || !at.isYDefined()) {
            Logger.warn("Rejecting unload command from agent " + agentID + ": could not locate agent");
            return null;
        }
        if (at.isHPDefined() && at.getHP() <= 0) {
            Logger.warn("Rejecting unload command from agent " + agentID + ": agent is dead");
            return null;
        }
        if (at.isBuriednessDefined() && at.getBuriedness() > 0) {
            Logger.warn("Rejecting unload command from agent " + agentID + ": agent is buried");
            return null;
        }
        // Is there something loaded?
        Civilian target = null;
        Logger.debug("Looking for civilian carried by " + agentID);
        for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Civilian c = (Civilian)e;
            Logger.debug(c + " is at " + c.getPosition());
            if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
                target = c;
                Logger.debug("Found civilian " + c);
                break;
            }
        }
        if (target == null) {
            Logger.warn("Rejecting unload command from agent " + agentID + ": agent is not carrying any civilians");
            return null;
        }
        // All checks passed
        target.setPosition(at.getPosition());
        target.setX(at.getX());
        target.setY(at.getY());
        changes.addChange(target, target.getPositionProperty());
        changes.addChange(target, target.getXProperty());
        changes.addChange(target, target.getYProperty());
        for (TrafficAgent trafficAgent : manager.getAgents()) {
            if (trafficAgent.getHuman() == target) {
                trafficAgent.setLocation(at.getX(), at.getY());
                trafficAgent.clearPath();
            }
        }
        manager.getTrafficAgent(at).setMobile(false);
        manager.getTrafficAgent(target).setMobile(false);
        Logger.debug(at + " unloaded " + target);
        return target;
    }

    private void handleClear(AKClear clear, ChangeSet changes) {
        // Agents clearing roads are not mobile
        EntityID agentID = clear.getAgentID();
        Entity agent = model.getEntity(agentID);
        if (agent instanceof Human) {
            manager.getTrafficAgent((Human)agent).setMobile(false);
            Logger.debug(agent + " is clearing");
        }
    }

    private void handleClearArea(AKClearArea clear, ChangeSet changes) {
        // Agents clearing roads are not mobile
        EntityID agentID = clear.getAgentID();
        Entity agent = model.getEntity(agentID);
        if (agent instanceof Human) {
            manager.getTrafficAgent((Human)agent).setMobile(false);
            Logger.debug(agent + " is clearing");
        }
    }

    private void handleRescue(AKRescue rescue, ChangeSet changes) {
        // Agents rescueing civilians are not mobile
        EntityID agentID = rescue.getAgentID();
        Entity agent = model.getEntity(agentID);
        if (agent instanceof Human) {
            manager.getTrafficAgent((Human)agent).setMobile(false);
            Logger.debug(agent + " is rescueing");
        }
    }

    private void handleExtinguish(AKExtinguish ex, ChangeSet changes) {
        // Agents extinguishing fires are not mobile
        EntityID agentID = ex.getAgentID();
        Entity agent = model.getEntity(agentID);
        if (agent instanceof Human) {
            manager.getTrafficAgent((Human)agent).setMobile(false);
            Logger.debug(agent + " is extinguishing");
        }
    }

    private void timestep() {
        long start = System.currentTimeMillis();
        for (TrafficAgent agent : manager.getAgents()) {
            agent.beginTimestep();
        }
        long pre = System.currentTimeMillis();
        Logger.debug("Running " + MICROSTEPS + " microsteps");
        for (int i = 0; i < MICROSTEPS; i++) {
            microstep();
        }
        long post = System.currentTimeMillis();
        for (TrafficAgent agent : manager.getAgents()) {
            agent.endTimestep();
        }
        long end = System.currentTimeMillis();
        if (manager.getAgents().size() != 0) {
            Logger.debug("Pre-timestep took " + (pre - start) + " ms (average " + ((pre - start) / manager.getAgents().size()) + "ms per agent)");
            Logger.debug("Microsteps took: " + (post - pre) + "ms (average " + ((post - pre) / MICROSTEPS) + "ms)");
            Logger.debug("Post-timestep took " + (end - post) + " ms (average " + ((end - post) / manager.getAgents().size()) + "ms per agent)");
        }
        Logger.debug("Total time: " + (end - start) + "ms");
    }

    private void microstep() {
        for (TrafficAgent agent : manager.getAgents()) {
            agent.step(STEP_TIME_MS);
        }
        gui.refresh();
    }
}

package clear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
   The area model clear simulator. This simulator processes AKClear messages.
 */
public class ClearSimulator extends StandardSimulator {
    private static final String SIMULATOR_NAME = "Area model clear simulator";

    private static final String REPAIR_RATE_KEY = "clear.repair.rate";
	private static final String REPAIR_RAD_KEY = "clear.repair.rad";
    private static final String REPAIR_DISTANCE_KEY = "clear.repair.distance";

    private static final double REPAIR_COST_FACTOR = 0.000001; // Converts square mm to square m.

	@Override
    public String getName() {
        return SIMULATOR_NAME;
    }

    @Override

    protected void processCommands(KSCommands c, ChangeSet changes) {
        long start = System.currentTimeMillis();
        int time = c.getTime();
        Logger.info("Timestep " + time);
        Map<Blockade, Integer> partiallyCleared = new HashMap<Blockade, Integer>();
        Set<EntityID> cleared = new HashSet<EntityID>();
        for (Command command : c.getCommands()) {
            if (command instanceof AKClear) {
		AKClear clear = (AKClear)command;
		if (!isValid(clear, cleared)) {
		    continue;
		}
		Logger.debug("Processing " + clear);
		EntityID blockadeID = clear.getTarget();
		Blockade blockade = (Blockade)model.getEntity(blockadeID);
		Area area = (Area)model.getEntity(blockade.getPosition());
		int cost = blockade.getRepairCost();
		int rate = config.getIntValue(REPAIR_RATE_KEY);
		Logger.debug("Blockade repair cost: " + cost);
		Logger.debug("Blockade repair rate: " + rate);
		if (rate >= cost) {
		    // Remove the blockade entirely
		    List<EntityID> ids = new ArrayList<EntityID>(area.getBlockades());
		    ids.remove(blockadeID);
		    area.setBlockades(ids);
		    model.removeEntity(blockadeID);
		    changes.addChange(area, area.getBlockadesProperty());
		    changes.entityDeleted(blockadeID);
		    partiallyCleared.remove(blockade);
		    cleared.add(blockadeID);
		    Logger.debug("Cleared " + blockade);
		}
		else {
		    // Update the repair cost
		    if (!partiallyCleared.containsKey(blockade)) {
			partiallyCleared.put(blockade, cost);
		    }
		    cost -= rate;
		    blockade.setRepairCost(cost);
		    changes.addChange(blockade, blockade.getRepairCostProperty());
		}
            } else if (command instanceof AKClearArea) {
		AKClearArea clear = (AKClearArea) command;
		if (!isValid(clear, cleared)) {
		    continue;
		}

		processClearArea(clear, changes);
		Logger.debug("Processing " + clear);
	    }
        }
        // Shrink partially cleared blockades
        for (Map.Entry<Blockade, Integer> next : partiallyCleared.entrySet()) {
            Blockade b = next.getKey();
            double original = next.getValue();
            double current = b.getRepairCost();
            // d is the new size relative to the old size
            double d = current / original;
            Logger.debug("Partially cleared " + b);
            Logger.debug("Original repair cost: " + original);
            Logger.debug("New repair cost: " + current);
            Logger.debug("Proportion left: " + d);
            int[] apexes = b.getApexes();
            double cx = b.getX();
            double cy = b.getY();
            // Move each apex towards the centre
            for (int i = 0; i < apexes.length; i += 2) {
                double x = apexes[i];
                double y = apexes[i + 1];
                double dx = x - cx;
                double dy = y - cy;
                // Shift both x and y so they are now d * dx from the centre
                double newX = cx + (dx * d);
                double newY = cy + (dy * d);
                apexes[i] = (int)newX;
                apexes[i + 1] = (int)newY;
            }
            b.setApexes(apexes);
            changes.addChange(b, b.getApexesProperty());
        }
        long end = System.currentTimeMillis();
        Logger.info("Timestep " + time + " took " + (end - start) + " ms");
    }

	private void processClearArea(AKClearArea clear, ChangeSet changes) {
		int rate = config.getIntValue(REPAIR_RATE_KEY);
		int clearRad = config.getIntValue(REPAIR_RAD_KEY);

		PoliceForce agent = (PoliceForce) model.getEntity(clear.getAgentID());
		int targetX = clear.getDestinationX();
		int targetY = clear.getDestinationY();

		int length = config.getIntValue(REPAIR_DISTANCE_KEY);
		Area agentPosition = (Area) agent
				.getPosition(model);

		Map<Blockade, java.awt.geom.Area> blockades = new HashMap<Blockade, java.awt.geom.Area>();
		for (StandardEntity entity : model.getObjectsInRange(agent.getX(), agent.getY(), length)) {
			if(entity instanceof Area) {
				Area area = (Area) entity;
				if (area.isBlockadesDefined()) {
					for (EntityID blockadeID : area.getBlockades()) {
						Blockade blockade = (Blockade) model.getEntity(blockadeID);
						if (blockade == null)
							continue;
						if (blockade.getShape() == null)
							System.err.println("Blockade Shape is null");
						blockades.put(blockade, new java.awt.geom.Area(blockade.getShape()));
					}
				}
			}
		}

		int counter = 0;
		int min = 0, max = 2 * length;
		while (true) {
			counter++;
			length = (min + max) / 2;
			java.awt.geom.Area area = Geometry.getClearArea(agent, targetX, targetY, length,
					clearRad);

			double firstSurface = Geometry.surface(area);
			for (java.awt.geom.Area blockade : blockades.values())
				area.subtract(blockade);
			double surface = Geometry.surface(area);
			double clearedSurface = firstSurface - surface;

			if (clearedSurface * REPAIR_COST_FACTOR > rate) {
				max = length;
				continue;
			}

			if (counter != 1 && counter < 15 && max - min > 5) { // 5 is a very
																	// little
																	// number!
				min = length;
				continue;
			}
			break;
		}

		java.awt.geom.Area area = Geometry.getClearArea(agent, targetX, targetY, length,
				clearRad);
		for (Map.Entry<Blockade, java.awt.geom.Area> entry : blockades.entrySet()) {
			Blockade blockade = entry.getKey();
			java.awt.geom.Area blockadeArea = entry.getValue();
			Road road = (Road) model.getEntity(blockade.getPosition());
			double firstSurface = Geometry.surface(blockadeArea);
			blockadeArea.subtract(area);
			double surface = Geometry.surface(blockadeArea);
			if (surface < firstSurface) {
				changes.addChange(blockade, blockade.getApexesProperty());
				List<int[]> areas = Geometry.getAreas(blockadeArea);
				if (areas.size() == 1) {
					Blockade backupBlockade = blockade;
					blockade = updateBlockadeApexes(blockade, areas.get(0));
					if (blockade == null) {
						blockade = backupBlockade;
						areas.clear();
					}
					else {
						changes.addChange(blockade,
								blockade.getApexesProperty());
						changes.addChange(blockade, blockade.getXProperty());
						changes.addChange(blockade, blockade.getYProperty());
						changes.addChange(blockade,
								blockade.getRepairCostProperty());
					}
				}
				if (areas.size() != 1) {
					try {
						List<EntityID> newIDs = requestNewEntityIDs(areas
								.size());
						Iterator<EntityID> it = newIDs.iterator();
						List<Blockade> newBlockades = new ArrayList<Blockade>();
						if (areas.size() > 0)
							Logger.debug("Creating new blockade objects for " + blockade.getID().getValue()
									+ " " + areas.size());
						for (int[] apexes : areas) {
							EntityID id = it.next();
							Blockade b = makeBlockade(id, apexes, road.getID());
							if (b != null)
								newBlockades.add(b);
						}
						List<EntityID> existing = road.getBlockades();
						List<EntityID> ids = new ArrayList<EntityID>();
						if (existing != null)
							ids.addAll(existing);
						for (Blockade blocakde : newBlockades)
							ids.add(blocakde.getID());
						ids.remove(blockade.getID());
						road.setBlockades(ids);
						changes.addAll(newBlockades);

						model.removeEntity(blockade.getID());
						changes.addChange(road, road.getBlockadesProperty());
						changes.entityDeleted(blockade.getID());
					} catch (InterruptedException e) {
						Logger.error("Interrupted while requesting IDs");
					}
				}
			}
		}
	}

	private Blockade updateBlockadeApexes(Blockade blockade, int[] apexes) {
		List<Point2D> points = GeometryTools2D.vertexArrayToPoints(apexes);
		if (points.size() < 2)
			return null;
		Point2D centroid = GeometryTools2D.computeCentroid(points);
		blockade.setApexes(apexes);
		blockade.setX((int) centroid.getX());
		blockade.setY((int) centroid.getY());
		int cost = (int) (GeometryTools2D.computeArea(points) * REPAIR_COST_FACTOR);
		if (cost == 0)
			return null;
		blockade.setRepairCost(cost);
		return blockade;
	}

	private Blockade makeBlockade(EntityID id, int[] apexes, EntityID roadID) {
		Blockade blockade = new Blockade(id);
		blockade.setPosition(roadID);
		return updateBlockadeApexes(blockade, apexes);
	}

    private boolean isValid(AKClear clear, Set<EntityID> cleared) {
        StandardEntity agent = model.getEntity(clear.getAgentID());
        StandardEntity target = model.getEntity(clear.getTarget());
        if (agent == null) {
            Logger.info("Rejecting clear command " + clear + ": agent does not exist");
            return false;
        }
        if (cleared.contains(clear.getTarget())) {
            Logger.info("Ignoring clear command " + clear + ": target already cleared this timestep");
            return false;
        }
        if (target == null) {
            Logger.info("Rejecting clear command " + clear + ": target does not exist");
            return false;
        }
        if (!(agent instanceof PoliceForce)) {
            Logger.info("Rejecting clear command " + clear + ": agent is not a police officer");
            return false;
        }
        if (!(target instanceof Blockade)) {
            Logger.info("Rejecting clear command " + clear + ": target is not a road");
            return false;
        }
        PoliceForce police = (PoliceForce)agent;
        StandardEntity agentPosition = police.getPosition(model);
        if (agentPosition == null) {
            Logger.info("Rejecting clear command " + clear + ": could not locate agent");
            return false;
        }
        if (!police.isHPDefined() || police.getHP() <= 0) {
            Logger.info("Rejecting clear command " + clear + ": agent is dead");
            return false;
        }
        if (police.isBuriednessDefined() && police.getBuriedness() > 0) {
            Logger.info("Rejecting clear command " + clear + ": agent is buried");
            return false;
        }
        Blockade targetBlockade = (Blockade)target;
        if (!targetBlockade.isPositionDefined()) {
            Logger.info("Rejecting clear command " + clear + ": blockade has no position");
            return false;
        }
        if (!targetBlockade.isRepairCostDefined()) {
            Logger.info("Rejecting clear command " + clear + ": blockade has no repair cost");
            return false;
        }
        // Check location
        // Find the closest point on the blockade to the agent
        int range = config.getIntValue(REPAIR_DISTANCE_KEY);
        Point2D agentLocation = new Point2D(police.getX(), police.getY());
        double bestDistance = Double.MAX_VALUE;
        for (Line2D line : GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(targetBlockade.getApexes()), true)) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, agentLocation);
            double distance = GeometryTools2D.getDistance(agentLocation, closest);
            if (distance < range) {
                return true;
            }
            if (bestDistance > distance) {
                bestDistance = distance;
            }
        }
        Logger.info("Rejecting clear command " + clear + ": agent is not adjacent to target: distance is " + bestDistance);
        return false;
    }

	private boolean isValid(AKClearArea clear, Set<EntityID> cleared) {
		StandardEntity agent = model.getEntity(clear.getAgentID());
		if (agent == null) {
			Logger.info("Rejecting clear command " + clear
					+ ": agent does not exist");
			return false;
		}
		if (!(agent instanceof PoliceForce)) {
			Logger.info("Rejecting clear command " + clear
					+ ": agent is not a police officer");
			return false;
		}
		PoliceForce police = (PoliceForce) agent;
		StandardEntity agentPosition = police.getPosition(model);
		if (agentPosition == null) {
			Logger.info("Rejecting clear command " + clear
					+ ": could not locate agent");
			return false;
		}
		if (!(agentPosition instanceof Area)) {
			Logger.info("Rejecting clear command " + clear
					+ ": could not locate agent");
			return false;
		}
		if (!police.isHPDefined() || police.getHP() <= 0) {
			Logger.info("Rejecting clear command " + clear + ": agent is dead");
			return false;
		}
		if (police.isBuriednessDefined() && police.getBuriedness() > 0) {
			Logger.info("Rejecting clear command " + clear
					+ ": agent is buried");
			return false;
		}
		return true;
	}
}

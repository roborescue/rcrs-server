package clear;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKClear;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
   The area model clear simulator. This simulator processes AKClear messages.
 */
public class ClearSimulator extends StandardSimulator {
    private static final String SIMULATOR_NAME = "Area model clear simulator";

    private static final String REPAIR_RATE_KEY = "clear.repair.rate";
    private static final String REPAIR_DISTANCE_KEY = "clear.repair.distance";

    @Override
    public String getName() {
        return SIMULATOR_NAME;
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        Map<Blockade, Integer> partiallyCleared = new HashMap<Blockade, Integer>();
        for (Command command : c.getCommands()) {
            if (command instanceof AKClear) {
                AKClear clear = (AKClear)command;
                if (!isValid(clear)) {
                    continue;
                }
                Logger.debug("Processing " + clear);
                EntityID blockadeID = clear.getTarget();
                Blockade blockade = (Blockade)model.getEntity(blockadeID);
                Area area = (Area)model.getEntity(blockade.getPosition());
                int cost = blockade.getRepairCost();
                int rate = config.getIntValue(REPAIR_RATE_KEY);
                if (rate > cost) {
                    // Remove the blockade entirely
                    List<EntityID> ids = new ArrayList<EntityID>(area.getBlockades());
                    ids.remove(blockadeID);
                    area.setBlockades(ids);
                    model.removeEntity(blockadeID);
                    changes.addChange(area, area.getBlockadesProperty());
                    changes.entityDeleted(blockadeID);
                    partiallyCleared.remove(blockade);
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
            }
        }
        // Shrink partially cleared blockades
        for (Map.Entry<Blockade, Integer> next : partiallyCleared.entrySet()) {
            Blockade b = next.getKey();
            double original = next.getValue();
            double current = b.getRepairCost();
            // d is the new size relative to the old size
            double d = current / original;
            Logger.debug("Partially cleared " + b + ": " + d);
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
    }

    private boolean isValid(AKClear clear) {
        StandardEntity agent = model.getEntity(clear.getAgentID());
        StandardEntity target = model.getEntity(clear.getTarget());
        if (agent == null) {
            Logger.info("Rejecting clear command " + clear + ": agent does not exist");
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
}

package misc;

import rescuecore2.config.Config;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.misc.EntityTools;
import rescuecore2.log.Logger;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;

import org.uncommons.maths.random.GaussianGenerator;

import java.util.Formatter;

/**
   A simple misc simulator. This simulator handles buriedness, health, loading, unloading and road clearing.
 */
public class MiscSimulator extends StandardSimulator {
    private static final String[] CODES = {"wood", "steel", "concrete"};

    private static final String PREFIX = "misc.";
    private static final String BURIEDNESS_SUFFIX = ".buriedness";
    private static final String SLIGHT_SUFFIX = ".slight";
    private static final String MODERATE_SUFFIX = ".moderate";
    private static final String SEVERE_SUFFIX = ".severe";
    private static final String DESTROYED_SUFFIX = ".destroyed";

    private static final String DAMAGE_MEAN_KEY = "misc.damage.mean";
    private static final String DAMAGE_SD_KEY = "misc.damage.sd";
    private static final String DAMAGE_FIRE_KEY = "misc.damage.fire";

    private static final String CLEAR_RATE_KEY = "misc.clear.rate";

    private static final int SLIGHT = 25;
    private static final int MODERATE = 50;
    private static final int SEVERE = 75;
    private static final int DESTROYED = 100;

    private ChangeSet changes;

    private BuriednessStats[] stats;

    private int fire;

    private int clearRate;

    private GaussianGenerator gaussian;

    /**
       Create a MiscSimulator.
    */
    public MiscSimulator() {
        changes = new ChangeSet();
    }

    @Override
    public String getName() {
        return "Basic misc simulator";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        stats = new BuriednessStats[CODES.length];
        for (int i = 0; i < CODES.length; ++i) {
            stats[i] = new BuriednessStats(i, config);
        }
        fire = config.getIntValue(DAMAGE_FIRE_KEY);
        clearRate = config.getIntValue(CLEAR_RATE_KEY);
        double mean = config.getFloatValue(DAMAGE_MEAN_KEY);
        double sd = config.getFloatValue(DAMAGE_SD_KEY);
        gaussian = new GaussianGenerator(mean, sd, config.getRandom());
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet cs) {
        int time = c.getTime();
        // Handle clear and rescue commands
        for (Command next : c.getCommands()) {
            if (next instanceof AKClear) {
                processClear((AKClear)next);
            }
            if (next instanceof AKRescue) {
                processRescue((AKRescue)next);
            }
            if (next instanceof AKLoad) {
                processLoad((AKLoad)next);
            }
            if (next instanceof AKUnload) {
                processUnload((AKUnload)next);
            }
        }
        updateHealth();
        Logger.info("Time: " + time);
        writeInfo();
        cs.merge(changes);
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
        changes = new ChangeSet();
        // Update buriedness if buildings have collapsed
        for (EntityID id : u.getChangeSet().getChangedEntities()) {
            Entity next = model.getEntity(id);
            if (next instanceof Building) {
                Building b = (Building)next;
                Property brokenness = u.getChangeSet().getChangedProperty(id, StandardPropertyURN.BROKENNESS.toString());
                if (brokenness != null) {
                    // Brokenness has changed. Bury any agents inside.
                    Logger.debug(b + " is broken. Updating trapped agents");
                    for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                            StandardEntityURN.FIRE_BRIGADE,
                                                            StandardEntityURN.POLICE_FORCE,
                                                            StandardEntityURN.AMBULANCE_TEAM)) {
                        Human h = (Human)e;
                        if (h.isPositionDefined() && h.getPosition().equals(b.getID())) {
                            Logger.debug("Human in building: " + h);
                            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
                            int increase = calculateBuriedness(h, b);
                            buriedness += increase;
                            h.setBuriedness(buriedness);
                            changes.addChange(h, h.getBuriednessProperty());
                            Logger.debug("Changed buriedness: increase by " + increase + " to " + buriedness);
                        }
                    }
                }
            }
        }
    }

    private void processClear(AKClear clear) {
        StandardEntity agent = model.getEntity(clear.getAgentID());
        StandardEntity target = model.getEntity(clear.getTarget());
        if (agent == null) {
            Logger.warn("Rejecting clear command " + clear + ": agent does not exist");
            return;
        }
        if (target == null) {
            Logger.warn("Rejecting clear command " + clear + ": target does not exist");
            return;
        }
        if (!(agent instanceof PoliceForce)) {
            Logger.warn("Rejecting clear command " + clear + ": agent is not a police officer");
            return;
        }
        if (!(target instanceof Road)) {
            Logger.warn("Rejecting clear command " + clear + ": target is not a road");
            return;
        }
        PoliceForce police = (PoliceForce)agent;
        StandardEntity agentPosition = police.getPosition(model);
        if (agentPosition == null) {
            Logger.warn("Rejecting clear command " + clear + ": could not locate agent");
            return;
        }
        if (!police.isHPDefined() || police.getHP() <= 0) {
            Logger.warn("Rejecting clear command " + clear + ": agent is dead");
            return;
        }
        if (police.isBuriednessDefined() && police.getBuriedness() > 0) {
            Logger.warn("Rejecting clear command " + clear + ": agent is buried");
            return;
        }
        Road targetRoad = (Road)target;
        if (!targetRoad.isBlockDefined() || targetRoad.getBlock() <= 0) {
            Logger.warn("Rejecting clear command " + clear + ": road is not blocked");
            return;
        }
        EntityID agentPositionID = police.getPosition();
        if (agentPositionID == null || !agentPositionID.equals(target.getID()) && !agentPositionID.equals(targetRoad.getHead()) && !agentPositionID.equals(targetRoad.getTail())) {
            Logger.warn("Rejecting clear command " + clear + ": agent is not adjacent to target road");
            return;
        }
        // All checks passed
        int block = targetRoad.getBlock();
        targetRoad.setBlock(Math.max(0, block - clearRate));
        changes.addChange(targetRoad, targetRoad.getBlockProperty());
        Logger.debug("Clear: " + clear);
        Logger.debug("Reduced road block from " + block + " to: " + targetRoad.getBlock());
    }

    private void processRescue(AKRescue rescue) {
        StandardEntity agent = model.getEntity(rescue.getAgentID());
        StandardEntity target = model.getEntity(rescue.getTarget());
        if (agent == null) {
            Logger.warn("Rejecting rescue command " + rescue + ": agent does not exist");
            return;
        }
        if (target == null) {
            Logger.warn("Rejecting rescue command " + rescue + ": target does not exist");
            return;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            Logger.warn("Rejecting rescue command " + rescue + ": agent is not an ambulance");
            return;
        }
        if (!(target instanceof Human)) {
            Logger.warn("Rejecting rescue command " + rescue + ": target is not a human");
            return;
        }
        AmbulanceTeam ambulance = (AmbulanceTeam)agent;
        Human targetHuman = (Human)target;
        StandardEntity agentPosition = ambulance.getPosition(model);
        StandardEntity targetPosition = targetHuman.getPosition(model);
        if (agentPosition == null) {
            Logger.warn("Rejecting rescue command " + rescue + ": could not locate agent");
            return;
        }
        if (targetPosition == null) {
            Logger.warn("Rejecting rescue command " + rescue + ": could not locate target");
            return;
        }
        if (!(targetPosition instanceof Building)) {
            Logger.warn("Rejecting rescue command " + rescue + ": target is not in a building");
            return;
        }
        if (!ambulance.isHPDefined() || ambulance.getHP() <= 0) {
            Logger.warn("Rejecting rescue command " + rescue + ": agent is dead");
            return;
        }
        if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
            Logger.warn("Rejecting rescue command " + rescue + ": agent is buried");
            return;
        }
        if (!targetHuman.isBuriednessDefined() || targetHuman.getBuriedness() <= 0) {
            Logger.warn("Rejecting rescue command " + rescue + ": target is not buried");
            return;
        }
        if (!agentPosition.equals(targetPosition)) {
            Logger.warn("Rejecting rescue command " + rescue + ": agent is at a different location to the target");
            return;
        }
        // All checks passed
        int buriedness = targetHuman.getBuriedness();
        targetHuman.setBuriedness(Math.max(0, buriedness - 1));
        changes.addChange(targetHuman, targetHuman.getBuriednessProperty());
        Logger.debug("Rescue: " + rescue);
        Logger.debug("Reduced buriedness from " + buriedness + " to: " + targetHuman.getBuriedness());
    }

    private void processLoad(AKLoad load) {
        StandardEntity agent = model.getEntity(load.getAgentID());
        StandardEntity target = model.getEntity(load.getTarget());
        if (agent == null) {
            Logger.warn("Rejecting load command " + load + ": agent does not exist");
            return;
        }
        if (target == null) {
            Logger.warn("Rejecting load command " + load + ": target does not exist");
            return;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            Logger.warn("Rejecting load command " + load + ": agent is not an ambulance");
            return;
        }
        if (!(target instanceof Civilian)) {
            Logger.warn("Rejecting load command " + load + ": target is not a civilian");
            return;
        }
        AmbulanceTeam ambulance = (AmbulanceTeam)agent;
        Civilian targetCivilian = (Civilian)target;
        StandardEntity agentPosition = ambulance.getPosition(model);
        StandardEntity targetPosition = targetCivilian.getPosition(model);
        EntityID agentID = agent.getID();
        if (agentPosition == null) {
            Logger.warn("Rejecting load command " + load + ": could not locate agent");
            return;
        }
        if (targetPosition == null) {
            Logger.warn("Rejecting load command " + load + ": could not locate target");
            return;
        }
        if (!ambulance.isHPDefined() || ambulance.getHP() <= 0) {
            Logger.warn("Rejecting load command " + load + ": agent is dead");
            return;
        }
        if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
            Logger.warn("Rejecting load command " + load + ": agent is buried");
            return;
        }
        if (targetCivilian.isBuriednessDefined() && targetCivilian.getBuriedness() > 0) {
            Logger.warn("Rejecting load command " + load + ": target is buried");
            return;
        }
        if (!agentPosition.equals(targetPosition)) {
            Logger.warn("Rejecting load command " + load + ": agent is at a different location to the target");
            return;
        }
        // Is there something already loaded?
        for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Civilian c = (Civilian)e;
            if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
                Logger.warn("Rejecting load command " + load + ": agent already has something loaded");
                return;
            }
        }
        // All checks passed
        targetCivilian.setPosition(agentID);
        changes.addChange(targetCivilian, targetCivilian.getPositionProperty());
        Logger.debug("Load: " + load);
        Logger.debug("Ambulance " + agentID + " loaded civilian " + targetCivilian.getID());
    }

    private void processUnload(AKUnload unload) {
        StandardEntity agent = model.getEntity(unload.getAgentID());
        if (agent == null) {
            Logger.warn("Rejecting unload command " + unload + ": agent does not exist");
            return;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            Logger.warn("Rejecting unload command " + unload + ": agent is not an ambulance");
            return;
        }
        EntityID agentID = agent.getID();
        AmbulanceTeam ambulance = (AmbulanceTeam)agent;
        StandardEntity agentPosition = ambulance.getPosition(model);
        if (agentPosition == null) {
            Logger.warn("Rejecting unload command " + unload + ": could not locate agent");
            return;
        }
        if (!ambulance.isHPDefined() && ambulance.getHP() <= 0) {
            Logger.warn("Rejecting unload command " + unload + ": agent is dead");
            return;
        }
        if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
            Logger.warn("Rejecting unload command " + unload + ": agent is buried");
            return;
        }
        // Is there something loaded?
        Civilian target = null;
        for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Civilian c = (Civilian)e;
            if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
                target = c;
                break;
            }
        }
        if (target == null) {
            Logger.warn("Rejecting unload command " + unload + ": agent is not carrying any civilians");
            return;
        }
        // All checks passed
        target.setPosition(ambulance.getPosition());
        changes.addChange(target, target.getPositionProperty());
        Logger.debug("Unload: " + unload);
        Logger.debug("Ambulance " + agentID + " unloaded " + target.getID() + " at " + ambulance.getPosition());
    }

    private void updateHealth() {
        for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                StandardEntityURN.FIRE_BRIGADE,
                                                StandardEntityURN.POLICE_FORCE,
                                                StandardEntityURN.AMBULANCE_TEAM)) {
            Human h = (Human)e;
            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
            int damage = h.isDamageDefined() ? h.getDamage() : 0;
            int hp = h.isHPDefined() ? h.getHP() : 0;
            StandardEntity position = h.getPosition(model);
            if (position instanceof Refuge) {
                if (damage > 0) {
                    h.setDamage(0);
                    changes.addChange(h, h.getDamageProperty());
                }
                continue;
            }
            boolean onFire = (position instanceof Building) && ((Building)position).isOnFire();
            // Increase damage if the agent is buried
            if (buriedness > 0) {
                damage += Math.max(0, (int)(gaussian.nextValue() * buriedness));
            }
            if (onFire) {
                damage += fire;
            }
            // Update HP
            hp = Math.max(0, hp - damage);
            // Update entity
            h.setDamage(damage);
            h.setHP(hp);
            changes.addChange(h, h.getDamageProperty());
            changes.addChange(h, h.getHPProperty());
        }
    }

    private int calculateBuriedness(Human h, Building b) {
        if (!b.isBuildingCodeDefined()) {
            return 0;
        }
        int code = b.getBuildingCode();
        return stats[code].computeBuriedness(b);
    }

    private void writeInfo() {
        StringBuilder builder = new StringBuilder();
        Formatter format = new Formatter(builder);
        format.format("|    Civ ID |     HP | Damage | Buriedness |%n");
        format.format("--------------------------------------------%n");
        for (Entity e : EntityTools.sortedList(model.getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                                       StandardEntityURN.FIRE_BRIGADE,
                                                                       StandardEntityURN.POLICE_FORCE,
                                                                       StandardEntityURN.AMBULANCE_TEAM))) {
            Human h = (Human)e;
            int hp = h.isHPDefined() ? h.getHP() : 0;
            int damage = h.isDamageDefined() ? h.getDamage() : 0;
            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
            if (hp > 0 && (damage > 0 || buriedness > 0)) {
                format.format("| %1$9d | %2$6d | %3$6d | %4$10d |%n", h.getID().getValue(), hp, damage, buriedness);
            }
        }
        format.format("--------------------------------------------%n");
        format.format("|   Road ID |  Block |%n");
        format.format("----------------------%n");
        for (Entity e : EntityTools.sortedList(model.getEntitiesOfType(StandardEntityURN.ROAD))) {
            Road r = (Road)e;
            int block = r.isBlockDefined() ? r.getBlock() : 0;
            if (block > 0) {
                format.format("| %1$9d | %2$6d |%n", r.getID().getValue(), block);
            }
        }
        format.format("---------------------%n");
        Logger.info(builder.toString());
    }

    private static class BuriednessStats {
        private int destroyed;
        private int severe;
        private int moderate;
        private int slight;

        BuriednessStats(int code, Config config) {
            destroyed = config.getIntValue(PREFIX + CODES[code] + BURIEDNESS_SUFFIX + DESTROYED_SUFFIX);
            severe = config.getIntValue(PREFIX + CODES[code] + BURIEDNESS_SUFFIX + SEVERE_SUFFIX);
            moderate = config.getIntValue(PREFIX + CODES[code] + BURIEDNESS_SUFFIX + MODERATE_SUFFIX);
            slight = config.getIntValue(PREFIX + CODES[code] + BURIEDNESS_SUFFIX + SLIGHT_SUFFIX);
        }

        int computeBuriedness(Building b) {
            if (!b.isBrokennessDefined()) {
                return 0;
            }
            int damage = b.getBrokenness();
            if (damage < SLIGHT) {
                return 0;
            }
            if (damage < MODERATE) {
                return slight;
            }
            if (damage < SEVERE) {
                return moderate;
            }
            if (damage < DESTROYED) {
                return severe;
            }
            return destroyed;
        }
    }
}

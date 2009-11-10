package misc;

import rescuecore2.config.Config;
import rescuecore2.components.AbstractSimulator;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.misc.EntityTools;

import rescuecore2.standard.entities.StandardWorldModel;
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

/**
   A simple misc simulator. This simulator handles buriedness, health, loading, unloading and road clearing.
 */
public class MiscSimulator extends AbstractSimulator<StandardEntity> {
    private static final String[] CODES = {"wood", "steel", "concrete"};

    private static final String PREFIX = "misc.";
    private static final String BURIEDNESS_SUFFIX = ".buriedness";
    private static final String SLIGHT_SUFFIX = ".slight";
    private static final String MODERATE_SUFFIX = ".moderate";
    private static final String SEVERE_SUFFIX = ".severe";
    private static final String DESTROYED_SUFFIX = ".destroyed";

    private static final String DAMAGE_RATE_KEY = "misc.damage.rate";
    private static final String DAMAGE_SPREAD_KEY = "misc.damage.spread";
    private static final String DAMAGE_FIRE_KEY = "misc.damage.fire";

    private static final String CLEAR_RATE_KEY = "misc.clear.rate";

    private static final int SLIGHT = 25;
    private static final int MODERATE = 50;
    private static final int SEVERE = 75;
    private static final int DESTROYED = 100;

    private ChangeSet changes;

    private BuriednessStats[] stats;

    private double rate;
    private double spread;
    private int fire;

    private int clearRate;

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
    protected StandardWorldModel createWorldModel() {
        return new StandardWorldModel();
    }

    @Override
    protected void postConnect() {
        stats = new BuriednessStats[CODES.length];
        for (int i = 0; i < CODES.length; ++i) {
            stats[i] = new BuriednessStats(i, config);
        }
        rate = config.getFloatValue(DAMAGE_RATE_KEY);
        spread = config.getFloatValue(DAMAGE_SPREAD_KEY);
        fire = config.getIntValue(DAMAGE_FIRE_KEY);
        clearRate = config.getIntValue(CLEAR_RATE_KEY);
    }

    @Override
    protected void handleCommands(Commands c) {
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
        System.out.println("Time: " + time);
        System.out.println("|    Civ ID |     HP | Damage | Buriedness |");
        System.out.println("--------------------------------------------");
        for (Entity e : EntityTools.sortedList(((StandardWorldModel)model).getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                                                             StandardEntityURN.FIRE_BRIGADE,
                                                                                             StandardEntityURN.POLICE_FORCE,
                                                                                             StandardEntityURN.AMBULANCE_TEAM))) {
            Human h = (Human)e;
            int hp = h.isHPDefined() ? h.getHP() : 0;
            int damage = h.isDamageDefined() ? h.getDamage() : 0;
            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
            if (hp > 0 && (damage > 0 || buriedness > 0)) {
                System.out.printf("| %1$9d | %2$6d | %3$6d | %4$10d |%n", h.getID().getValue(), hp, damage, buriedness);
            }
        }
        System.out.println("--------------------------------------------");
        System.out.println("|   Road ID |  Block |");
        System.out.println("----------------------");
        for (Entity e : EntityTools.sortedList(((StandardWorldModel)model).getEntitiesOfType(StandardEntityURN.ROAD))) {
            Road r = (Road)e;
            int block = r.isBlockDefined() ? r.getBlock() : 0;
            if (block > 0) {
                System.out.printf("| %1$9d | %2$6d |%n", r.getID().getValue(), block);
            }
        }
        System.out.println("---------------------");
        send(new SKUpdate(simulatorID, time, changes));
    }

    @Override
    protected void handleUpdate(Update u) {
        super.handleUpdate(u);
        changes = new ChangeSet();
        // Update buriedness if buildings have collapsed
        for (EntityID id : u.getChangeSet().getChangedEntities()) {
            Entity next = model.getEntity(id);
            if (next instanceof Building) {
                Building b = (Building)next;
                Property brokenness = u.getChangeSet().getChangedProperty(id, StandardPropertyURN.BROKENNESS.name());
                if (brokenness != null) {
                    // Brokenness has changed. Bury any agents inside.
                    //                    System.out.println(b + " is broken. Updating trapped agents");
                    for (Entity e : ((StandardWorldModel)model).getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                                                  StandardEntityURN.FIRE_BRIGADE,
                                                                                  StandardEntityURN.POLICE_FORCE,
                                                                                  StandardEntityURN.AMBULANCE_TEAM)) {
                        Human h = (Human)e;
                        if (h.isPositionDefined() && h.getPosition().equals(b.getID())) {
                            //                            System.out.println("Human in building: " + h);
                            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
                            int increase = calculateBuriedness(h, b);
                            buriedness += increase;
                            h.setBuriedness(buriedness);
                            changes.addChange(h, h.getBuriednessProperty());
                            //                            System.out.println("Changed buriedness: increase by " + increase + " to " + buriedness);
                        }
                    }
                }
            }
        }
    }

    private void processClear(AKClear clear) {
        StandardWorldModel world = (StandardWorldModel)model;
        StandardEntity agent = world.getEntity(clear.getAgentID());
        StandardEntity target = world.getEntity(clear.getTarget());
        if (agent == null) {
            System.out.println("Rejecting clear command " + clear + ": agent does not exist");
            return;
        }
        if (target == null) {
            System.out.println("Rejecting clear command " + clear + ": target does not exist");
            return;
        }
        if (!(agent instanceof PoliceForce)) {
            System.out.println("Rejecting clear command " + clear + ": agent is not a police officer");
            return;
        }
        if (!(target instanceof Road)) {
            System.out.println("Rejecting clear command " + clear + ": target is not a road");
            return;
        }
        PoliceForce police = (PoliceForce)agent;
        StandardEntity agentPosition = police.getPosition(world);
        if (agentPosition == null) {
            System.out.println("Rejecting clear command " + clear + ": could not locate agent");
            return;
        }
        if (!police.isHPDefined() || police.getHP() <= 0) {
            System.out.println("Rejecting clear command " + clear + ": agent is dead");
            return;
        }
        if (police.isBuriednessDefined() && police.getBuriedness() > 0) {
            System.out.println("Rejecting clear command " + clear + ": agent is buried");
            return;
        }
        Road targetRoad = (Road)target;
        if (!targetRoad.isBlockDefined() || targetRoad.getBlock() <= 0) {
            System.out.println("Rejecting clear command " + clear + ": road is not blocked");
            return;
        }
        EntityID agentPositionID = police.getPosition();
        if (agentPositionID == null || !agentPositionID.equals(target.getID()) && !agentPositionID.equals(targetRoad.getHead()) && !agentPositionID.equals(targetRoad.getTail())) {
            System.out.println("Rejecting clear command " + clear + ": agent is not adjacent to target road");
            return;
        }
        // All checks passed
        int block = targetRoad.getBlock();
        targetRoad.setBlock(Math.max(0, block - clearRate));
        changes.addChange(targetRoad, targetRoad.getBlockProperty());
        System.out.println("Clear: " + clear);
        System.out.println("Reduced road block from " + block + " to: " + targetRoad.getBlock());
    }

    private void processRescue(AKRescue rescue) {
        StandardWorldModel world = (StandardWorldModel)model;
        StandardEntity agent = world.getEntity(rescue.getAgentID());
        StandardEntity target = world.getEntity(rescue.getTarget());
        if (agent == null) {
            System.out.println("Rejecting rescue command " + rescue + ": agent does not exist");
            return;
        }
        if (target == null) {
            System.out.println("Rejecting rescue command " + rescue + ": target does not exist");
            return;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            System.out.println("Rejecting rescue command " + rescue + ": agent is not an ambulance");
            return;
        }
        if (!(target instanceof Human)) {
            System.out.println("Rejecting rescue command " + rescue + ": target is not a human");
            return;
        }
        AmbulanceTeam ambulance = (AmbulanceTeam)agent;
        Human targetHuman = (Human)target;
        StandardEntity agentPosition = ambulance.getPosition(world);
        StandardEntity targetPosition = targetHuman.getPosition(world);
        if (agentPosition == null) {
            System.out.println("Rejecting rescue command " + rescue + ": could not locate agent");
            return;
        }
        if (targetPosition == null) {
            System.out.println("Rejecting rescue command " + rescue + ": could not locate target");
            return;
        }
        if (!(targetPosition instanceof Building)) {
            System.out.println("Rejecting rescue command " + rescue + ": target is not in a building");
            return;
        }
        if (!ambulance.isHPDefined() || ambulance.getHP() <= 0) {
            System.out.println("Rejecting rescue command " + rescue + ": agent is dead");
            return;
        }
        if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
            System.out.println("Rejecting rescue command " + rescue + ": agent is buried");
            return;
        }
        if (!targetHuman.isBuriednessDefined() || targetHuman.getBuriedness() <= 0) {
            System.out.println("Rejecting rescue command " + rescue + ": target is not buried");
            return;
        }
        if (!agentPosition.equals(targetPosition)) {
            System.out.println("Rejecting rescue command " + rescue + ": agent is at a different location to the target");
            return;
        }
        // All checks passed
        int buriedness = targetHuman.getBuriedness();
        targetHuman.setBuriedness(Math.max(0, buriedness - 1));
        changes.addChange(targetHuman, targetHuman.getBuriednessProperty());
        System.out.println("Rescue: " + rescue);
        System.out.println("Reduced buriedness from " + buriedness + " to: " + targetHuman.getBuriedness());
    }

    private void processLoad(AKLoad load) {
        StandardWorldModel world = (StandardWorldModel)model;
        StandardEntity agent = world.getEntity(load.getAgentID());
        StandardEntity target = world.getEntity(load.getTarget());
        if (agent == null) {
            System.out.println("Rejecting load command " + load + ": agent does not exist");
            return;
        }
        if (target == null) {
            System.out.println("Rejecting load command " + load + ": target does not exist");
            return;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            System.out.println("Rejecting load command " + load + ": agent is not an ambulance");
            return;
        }
        if (!(target instanceof Civilian)) {
            System.out.println("Rejecting load command " + load + ": target is not a civilian");
            return;
        }
        AmbulanceTeam ambulance = (AmbulanceTeam)agent;
        Civilian targetCivilian = (Civilian)target;
        StandardEntity agentPosition = ambulance.getPosition(world);
        StandardEntity targetPosition = targetCivilian.getPosition(world);
        EntityID agentID = agent.getID();
        if (agentPosition == null) {
            System.out.println("Rejecting load command " + load + ": could not locate agent");
            return;
        }
        if (targetPosition == null) {
            System.out.println("Rejecting load command " + load + ": could not locate target");
            return;
        }
        if (!ambulance.isHPDefined() || ambulance.getHP() <= 0) {
            System.out.println("Rejecting load command " + load + ": agent is dead");
            return;
        }
        if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
            System.out.println("Rejecting load command " + load + ": agent is buried");
            return;
        }
        if (targetCivilian.isBuriednessDefined() && targetCivilian.getBuriedness() > 0) {
            System.out.println("Rejecting load command " + load + ": target is buried");
            return;
        }
        if (!agentPosition.equals(targetPosition)) {
            System.out.println("Rejecting load command " + load + ": agent is at a different location to the target");
            return;
        }
        // Is there something already loaded?
        for (Entity e : ((StandardWorldModel)model).getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Civilian c = (Civilian)e;
            if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
                System.out.println("Rejecting load command " + load + ": agent already has something loaded");
                return;
            }
        }
        // All checks passed
        targetCivilian.setPosition(agentID);
        changes.addChange(targetCivilian, targetCivilian.getPositionProperty());
        System.out.println("Load: " + load);
        System.out.println("Ambulance " + agentID + " loaded civilian " + targetCivilian.getID());
    }

    private void processUnload(AKUnload unload) {
        StandardWorldModel world = (StandardWorldModel)model;
        StandardEntity agent = world.getEntity(unload.getAgentID());
        if (agent == null) {
            System.out.println("Rejecting unload command " + unload + ": agent does not exist");
            return;
        }
        if (!(agent instanceof AmbulanceTeam)) {
            System.out.println("Rejecting unload command " + unload + ": agent is not an ambulance");
            return;
        }
        EntityID agentID = agent.getID();
        AmbulanceTeam ambulance = (AmbulanceTeam)agent;
        StandardEntity agentPosition = ambulance.getPosition(world);
        if (agentPosition == null) {
            System.out.println("Rejecting unload command " + unload + ": could not locate agent");
            return;
        }
        if (!ambulance.isHPDefined() && ambulance.getHP() <= 0) {
            System.out.println("Rejecting unload command " + unload + ": agent is dead");
            return;
        }
        if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
            System.out.println("Rejecting unload command " + unload + ": agent is buried");
            return;
        }
        // Is there something loaded?
        Civilian target = null;
        for (Entity e : ((StandardWorldModel)model).getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            Civilian c = (Civilian)e;
            if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
                target = c;
                break;
            }
        }
        if (target == null) {
            System.out.println("Rejecting unload command " + unload + ": agent is not carrying any civilians");
            return;
        }
        // All checks passed
        target.setPosition(ambulance.getPosition());
        changes.addChange(target, target.getPositionProperty());
        System.out.println("Unload: " + unload);
        System.out.println("Ambulance " + agentID + " unloaded " + target.getID() + " at " + ambulance.getPosition());
    }

    private void updateHealth() {
        for (Entity e : ((StandardWorldModel)model).getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                                      StandardEntityURN.FIRE_BRIGADE,
                                                                      StandardEntityURN.POLICE_FORCE,
                                                                      StandardEntityURN.AMBULANCE_TEAM)) {
            Human h = (Human)e;
            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
            int damage = h.isDamageDefined() ? h.getDamage() : 0;
            int hp = h.isHPDefined() ? h.getHP() : 0;
            StandardEntity position = h.getPosition((StandardWorldModel)model);
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
                damage += Math.max(0, (int)(random.nextGaussian() * spread * rate * buriedness));
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
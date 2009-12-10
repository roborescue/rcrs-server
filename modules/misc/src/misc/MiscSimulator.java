package misc;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;

import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.ChangeSet;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.components.StandardSimulator;

import rescuecore2.GUIComponent;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Formatter;

/**
 * Implementation of the legacy misc simulator.
 * @author Maitreyi Nanjanath
 * @author Cameron Skinner
 */
public class MiscSimulator extends StandardSimulator implements GUIComponent {
    private static final Log LOG = LogFactory.getLog(MiscSimulator.class);

    private Map<EntityID, HumanAttributes> humans;
    private Set<EntityID> newlyBrokenBuildings;

    private MiscParameters parameters;
    private MiscSimulatorGUI gui;

    @Override
    public JComponent getGUIComponent() {
        if (gui == null) {
            gui = new MiscSimulatorGUI();
        }
        return gui;
    }

    @Override
    public String getGUIComponentName() {
        return "Misc simulator";
    }

    @Override
    protected void postConnect() {
        super.postConnect();

        parameters = new MiscParameters(config);
        humans = new HashMap<EntityID, HumanAttributes>();
        newlyBrokenBuildings = new HashSet<EntityID>();

        LOG.info("MiscSimulator connected. World has " + model.getAllEntities().size() + " entities.");
        BuildingChangeListener buildingListener = new BuildingChangeListener();
        //HumanChangeListener humanListener = new HumanChangeListener();
        for (Entity et : model.getAllEntities()) {
            if (et instanceof Building) {
                et.addEntityListener(buildingListener);
            }
            else if (et instanceof Human) {
                //et.addEntityListener(humanListener);
                Human human = (Human)et;
                HumanAttributes ha = new HumanAttributes(human, config);
                humans.put(ha.getID(), ha);
            }
        }
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        super.processCommands(c, changes);

        for (Command com : c.getCommands()) {
            if (checkValidity(com)) {
                if (com instanceof AKRescue) {
                    Human human = (Human)(model.getEntity(((AKRescue)com).getTarget()));
                    handleRescue(human, changes);
                }
            }
        }

        processBrokenBuildings(changes);
        processBurningBuildings(changes);
        updateDamage(changes);
        // Clean up
        newlyBrokenBuildings.clear();
        writeDebugOutput(c.getTime());
        if (gui != null) {
            gui.refresh(humans.values());
        }
    }

    private void processBrokenBuildings(ChangeSet changes) {
        for (HumanAttributes hA : humans.values()) {
            Human human = hA.getHuman();
            EntityID positionID = human.getPosition();
            if (!newlyBrokenBuildings.contains(positionID)) {
                continue;
            }
            // Human is in a newly collapsed building
            // Check for buriedness
            LOG.trace("Checking if human should be buried in broken building");
            Building b = (Building)human.getPosition(model);
            if (parameters.shouldBuryAgent(b)) {
                int buriedness = parameters.getBuriedness(b);
                if (buriedness != 0) {
                    int oldBuriedness = human.isBuriednessDefined() ? human.getBuriedness() : 0;
                    human.setBuriedness(Math.max(oldBuriedness, buriedness));
                    changes.addChange(human, human.getBuriednessProperty());
                    // Check for injury from being buried
                    int damage = parameters.getBuryDamage(b, human);
                    if (damage != 0) {
                        hA.addBuriednessDamage(damage);
                    }
                }
            }
            // Now check for injury from the collapse
            int damage = parameters.getCollapseDamage(b, human);
            if (damage != 0) {
                hA.addCollapseDamage(damage);
            }
        }
    }

    private void processBurningBuildings(ChangeSet changes) {
        for (HumanAttributes hA : humans.values()) {
            Human human = hA.getHuman();
            EntityID positionID = human.getPosition();
            Entity position = human.getPosition(model);
            if (position instanceof Building && ((Building)position).isOnFire()) {
                // Human is in a burning building
                int damage = parameters.getFireDamage((Building)position, human);
                if (damage != 0) {
                    hA.addFireDamage(damage);
                }
            }
        }
    }

    private void writeDebugOutput(int time) {
        StringBuilder builder = new StringBuilder();
        Formatter format = new Formatter(builder);
        format.format("Agents damaged or buried at timestep %1d%n", time);
        format.format("    ID    |   HP   | Damage |   Bury   | Collapse |   Fire   | Buriedness%n");
        for (HumanAttributes ha : humans.values()) {
            Human h = ha.getHuman();
            int hp = h.isHPDefined() ? h.getHP() : 0;
            int damage = ha.getTotalDamage();
            int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
            boolean isAlive = hp > 0;
            boolean hasDamage = damage > 0;
            boolean isBuried = buriedness > 0;
            if ((hasDamage || isBuried) && isAlive) {
                format.format("%1$9d | %2$6d | %3$6d | %4$8.3f | %5$8.3f | %6$8.3f | %7$6d%n",
                              ha.getID().getValue(),
                              hp,
                              damage,
                              ha.getBuriednessDamage(),
                              ha.getCollapseDamage(),
                              ha.getFireDamage(),
                              buriedness);
            }
        }
        LOG.debug(builder.toString());
    }

    private void updateDamage(ChangeSet changes) {
        for (HumanAttributes ha : humans.values()) {
            updateDamage(ha);
            Human h = ha.getHuman();
            int hp = h.isHPDefined() ? h.getHP() : 0;
            int damage = ha.getTotalDamage();

            h.setDamage(damage);
            changes.addChange(ha.getHuman(), ha.getHuman().getDamageProperty());

            // Update HP
            boolean isAlive = hp > 0;
            boolean hasDamage = damage > 0;

            if (isAlive && hasDamage) {
                int newHP = Math.max(0, hp - damage);
                h.setHP(newHP);
                changes.addChange(ha.getHuman(), ha.getHuman().getHPProperty());
            }

            // Treat damage if in a refuge
            if (h.getPosition(model) instanceof Refuge) {
                ha.clearDamage();
                h.setDamage(0);
                changes.addChange(ha.getHuman(), ha.getHuman().getDamageProperty());
            }
        }
    }

    private void updateDamage(HumanAttributes ha) {
        Human h = ha.getHuman();
        if (h.getHP() <= 0) {
            return; // Agent is already dead.
        }
        ha.progressDamage();
    }

    private boolean checkValidity(Command command) {
        Entity e = model.getEntity(command.getAgentID());
        if (e == null) {
            LOG.warn("Received a " + command.getURN() + " command from an unknown agent: " + command.getAgentID());
            return false;
        }
        if (command instanceof AKRescue) {
            return checkRescue((AKRescue)command, e);
        }
        return false;
    }

    private boolean checkRescue(AKRescue rescue, Entity agent) {
        EntityID targetID = rescue.getTarget();
        Entity target = model.getEntity(targetID);
        if (!(agent instanceof AmbulanceTeam)) {
            LOG.warn("Received a rescue command from agent " + agent.getID() + " who is of type " + agent.getURN());
            return false;
        }
        if (target == null) {
            LOG.warn("Received a rescue command from agent " + agent.getID() + " for a non-existant target " + targetID);
            return false;
        }
        if (!(target instanceof Human)) {
            LOG.warn("Received a rescue command from agent " + agent.getID() + " for a non-human target: " + targetID + " is of type " + target.getURN());
            return false;
        }
        Human h = (Human)target;
        AmbulanceTeam at = (AmbulanceTeam)agent;
        if (!h.isBuriednessDefined() || h.getBuriedness() == 0) {
            LOG.warn("Received a rescue command from agent " + agent.getID() + " for a non-buried target " + targetID);
            return false;
        }
        if (!h.isPositionDefined() || !at.isPositionDefined() || !h.getPosition().equals(at.getPosition())) {
            LOG.warn("Received a rescue command from agent " + agent.getID() + " for a non-adjacent target " + targetID);
            return false;
        }
        if (h.getID().equals(at.getID())) {
            LOG.warn("Agent " + agent.getID() + " tried to rescue itself");
            return false;
        }
        return true;
    }

    private void handleRescue(Human target, ChangeSet changes) {
        target.setBuriedness(Math.max(0, target.getBuriedness() - 1));
        changes.addChange(target, target.getBuriednessProperty());
    }

    private class BuildingChangeListener implements EntityListener {
        @Override
        public void propertyChanged(Entity e, Property p, Object oldValue, Object newValue) {
            if (!(e instanceof Building)) {
                return; //we want to only look at buildings
            }
            if (!p.getURN().equals(StandardPropertyURN.BROKENNESS.toString())) {
                // Only care about brokenness changes
                return;
            }
            double old = oldValue == null ? 0 : (Integer)oldValue;
            double next = newValue == null ? 0 : (Integer)newValue;
            if (next > old) {
                newlyBrokenBuildings.add(e.getID());
            }
        }
    }
}


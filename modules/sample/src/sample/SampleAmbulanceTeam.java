package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.standard.messages.AKRescue;

/**
   A sample ambulance team agent.
 */
public class SampleAmbulanceTeam extends AbstractSampleAgent {
    private Collection<StandardEntity> unexploredBuildings;

    @Override
    public String toString() {
        return "Sample ambulance team";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        world.indexClass(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE, StandardEntityURN.BUILDING);
        unexploredBuildings = world.getEntitiesOfType(StandardEntityURN.BUILDING);
    }

    @Override
    protected void think(int time, List<EntityID> changed) {
        updateUnexploredBuildings(changed);
        // Am I transporting a civilian to a refuge?
        if (someoneOnBoard()) {
            // Am I at a refuge?
            if (location() instanceof Refuge) {
                // Unload!
                System.out.println(me() + " unloading");
                send(new AKUnload(getID(), time));
                return;
            }
            else {
                // Move to a refuge
                List<EntityID> path = search.breadthFirstSearch(location(), world.getEntitiesOfType(StandardEntityURN.REFUGE));
                if (path != null) {
                    AKMove move = new AKMove(getID(), time, path);
                    System.out.println(me() + " moving to refuge: " + move);
                    send(move);
                    return;
                }
                else {
                    System.out.println(me() + " couldn't plan a path to refuge!");
                    // What do I do now? Might as well carry on and see if we can dig someone else out.
                }
            }
        }
        // Go through targets (sorted by distance) and check for things we can do
        for (Human next : getTargets()) {
            if (next.getPosition().equals(location().getID())) {
                // Targets in the same place might need rescueing or loading
                if (next.getBuriedness() == 0 && next.getDamage() > 0 && !(location() instanceof Refuge)) {
                    // Load
                    AKLoad load = new AKLoad(getID(), time, next.getID());
                    System.out.println(me() + " loading target " + next);
                    send(load);
                    return;
                }
                if (next.getBuriedness() > 0) {
                    // Rescue
                    AKRescue rescue = new AKRescue(getID(), time, next.getID());
                    System.out.println(me() + " rescueing target " + next);
                    send(rescue);
                    return;
                }
            }
            else {
                // Try to move to the target
                List<EntityID> path = search.breadthFirstSearch(location(), next.getPosition(world));
                if (path != null) {
                    System.out.println(me() + " moving to target " + next);
                    send(new AKMove(getID(), time, path));
                    return;
                }
            }
        }
        // Nothing to do
        List<EntityID> path = search.breadthFirstSearch(location(), unexploredBuildings);
        if (path != null) {
            AKMove move = new AKMove(getID(), time, path);
            System.out.println(me() + " exploring building: " + move);
            send(move);
            return;
        }
        System.out.println(me() + " has nothing to do");
        send(new AKMove(getID(), time, randomWalk()));
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
    }

    private boolean someoneOnBoard() {
        for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM)) {
            if (((Human)next).getPosition().equals(getID())) {
                return true;
            }
        }
        return false;
    }

    private List<Human> getTargets() {
        List<Human> targets = new ArrayList<Human>();
        for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM)) {
            Human h = (Human)next;
            if (h.getHP() > 0 && (h.getBuriedness() > 0 || h.getDamage() > 0)) {
                targets.add(h);
            }
        }
        Collections.sort(targets, new DistanceSorter(location(), world));
        return targets;
    }

    private void updateUnexploredBuildings(List<EntityID> changed) {
        for (EntityID next : changed) {
            StandardEntity e = world.getEntity(next);
            if (e != null) {
                unexploredBuildings.remove(e);
            }
        }
    }
}
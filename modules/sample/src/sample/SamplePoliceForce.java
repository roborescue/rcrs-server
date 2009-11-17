package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKClear;

/**
   A sample police force agent.
 */
public class SamplePoliceForce extends AbstractSampleAgent {
    @Override
    public String toString() {
        return "Sample police force";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        world.indexClass(StandardEntityURN.ROAD);
        search.setIgnoreBlockedRoads(false);
    }

    @Override
    protected void think(int time, Collection<EntityID> changed, Collection<Command> heard) {
        for (Command next : heard) {
            System.out.println(me() + " heard " + next);
        }
        // Am I on a blocked road?
        StandardEntity location = location();
        if (location instanceof Road && ((Road)location).isBlockDefined() && ((Road)location).getBlock() > 0) {
            AKClear clear = new AKClear(getID(), time, location.getID());
            //            System.out.println(me() + " clearing road: " + clear);
            send(clear);
            return;
        }
        // Plan a path to a blocked road
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            AKMove move = new AKMove(getID(), time, path);
            //            System.out.println(me() + " moving to road: " + move);
            send(move);
            return;
        }
        //        System.out.println(me() + " couldn't plan a path to a blocked road.");
        send(new AKMove(getID(), time, randomWalk()));
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Road> getBlockedRoads() {
        Collection<StandardEntity> e = world.getEntitiesOfType(StandardEntityURN.ROAD);
        List<Road> result = new ArrayList<Road>();
        for (StandardEntity next : e) {
            if (next instanceof Road) {
                Road r = (Road)next;
                if (r.isBlockDefined() && r.getBlock() > 0) {
                    result.add(r);
                }
            }
        }
        return result;
    }
}
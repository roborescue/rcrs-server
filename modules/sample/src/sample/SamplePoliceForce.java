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
import rescuecore2.standard.entities.PoliceForce;

/**
   A sample police force agent.
 */
public class SamplePoliceForce extends AbstractSampleAgent<PoliceForce> {
    @Override
    public String toString() {
        return "Sample police force";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.ROAD);
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
            sendClear(time, location.getID());
            return;
        }
        // Plan a path to a blocked road
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            sendMove(time, path);
            return;
        }
        sendMove(time, randomWalk());
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Road> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
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
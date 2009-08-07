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
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.misc.Pair;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A sample police force agent.
 */
public class SamplePoliceForce extends AbstractSampleAgent<PoliceForce> {
    private static final Log LOG = LogFactory.getLog(SamplePoliceForce.class);

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
            LOG.debug(me() + " heard " + next);
        }
        // Am I on a blocked road?
        StandardEntity location = location();
        if (location instanceof Road && ((Road)location).isBlockDefined() && ((Road)location).getBlock() > 0) {
            sendClear(time, location.getID());
            return;
        }
	Pair<Integer, Integer> l = me().getLocation(model);

	//System.err.println(((Area)location).getNearlestBlockade(l.first(), l.second(), world));
	if(location instanceof Area && ((Area)location).getNearlestBlockade(l.first(), l.second(), model)!=null) {
	//if(location instanceof Area && ((Area)location).getBlockadeList().size()>0) {
	    EntityID blockade_id = ((Area)location).getNearlestBlockade(l.first(), l.second(), model);
            AKClear clear = new AKClear(getID(), time, blockade_id);
            //System.out.println(me() + " clear road: " + clear);
            //System.err.println(me() + ":" + location + " clear road: " + clear);
	    List<EntityID> bl = ((Area)location).getNearBlockadeList(model);
	    System.err.println(bl+", clear: "+blockade_id);

            send(clear);
	    return ;
	}

        List<EntityID> path = null;
        // Plan a path to a blocked area

        path = search.breadthFirstSearch(location(), getBlockedAreas());
        if (path != null) {
            AKMove move = new AKMove(getID(), time, path);
            LOG.debug(me() + " moving to road: " + move);
            send(move);
	    return;
        }
        LOG.debug(me() + " couldn't plan a path to a blocked road.");
        sendMove(time, randomWalk());
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    private List<Area> getBlockedAreas() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<Area> result = new ArrayList<Area>();
        for (StandardEntity next : e) {
            if (next instanceof Area) {
                Area a = (Area)next;
                if (a.getBlockadeList().size() > 0) {
                    result.add(a);
                }
            }
        }
        return result;
    }
}
package sample;

import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

/**
   A no-op agent.
 */
public class DummyAgent extends StandardAgent<StandardEntity> {
    @Override
    protected void think(int time, Collection<EntityID> changed, Collection<Command> heard) {
        sendRest(time);
    }

    @Override
    public String[] getRequestedEntityURNs() {
        EnumSet<StandardEntityURN> set = getRequestedEntityURNsEnum();
        String[] result = new String[set.size()];
        int i = 0;
        for (StandardEntityURN next : set) {
            result[i++] = next.name();
        }
        return result;
    }

    private EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE,
                          StandardEntityURN.FIRE_STATION,
                          StandardEntityURN.AMBULANCE_TEAM,
                          StandardEntityURN.AMBULANCE_CENTRE,
                          StandardEntityURN.POLICE_FORCE,
                          StandardEntityURN.POLICE_OFFICE);
    }
}
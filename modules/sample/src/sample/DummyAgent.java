package sample;

import java.util.List;
import java.util.EnumSet;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.components.AbstractAgent;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

/**
   A no-op agent.
 */
public class DummyAgent extends AbstractAgent<StandardEntity> {
    @Override
    protected void think(int time, List<EntityID> changed) {
    }

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        return new DefaultWorldModel<StandardEntity>(StandardEntity.class);
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
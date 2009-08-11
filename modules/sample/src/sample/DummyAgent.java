package sample;

import java.util.List;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.components.AbstractAgent;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityType;

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
    public int[] getRequestedEntityIDs() {
        return new int[] {StandardEntityType.FIRE_BRIGADE.getID(),
                          StandardEntityType.FIRE_STATION.getID(),
                          StandardEntityType.AMBULANCE_TEAM.getID(),
                          StandardEntityType.AMBULANCE_CENTRE.getID(),
                          StandardEntityType.POLICE_FORCE.getID(),
                          StandardEntityType.POLICE_OFFICE.getID()
        };
    }
}
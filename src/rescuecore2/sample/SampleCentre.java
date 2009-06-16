package rescuecore2.sample;

import java.util.List;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.components.AbstractAgent;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityType;

/**
   A sample centre agent.
 */
public class SampleCentre extends AbstractAgent<StandardEntity> {
    @Override
    protected void think(int time, List<EntityID> changed) {
    }

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        return new DefaultWorldModel<StandardEntity>(StandardEntity.class);
    }

    @Override
    protected int[] getRequestedEntityIDs() {
        return new int[] {StandardEntityType.FIRE_STATION.getID(),
                          StandardEntityType.POLICE_OFFICE.getID(),
                          StandardEntityType.AMBULANCE_CENTRE.getID()
        };
    }
}
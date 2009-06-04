package rescuecore2.sample;

import java.util.List;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.EntityID;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.RescueEntityType;

/**
   A no-op agent.
 */
public class DummyAgent extends AbstractAgent<RescueEntity> {
    @Override
    protected void think(int time, List<EntityID> changed) {
    }

    @Override
    protected WorldModel<RescueEntity> createWorldModel() {
        return new DefaultWorldModel<RescueEntity>(RescueEntity.class);
    }

    @Override
    protected int[] getRequestedEntityIDs() {
        return new int[] {RescueEntityType.CIVILIAN.getID(),
                                  RescueEntityType.FIRE_BRIGADE.getID(),
                                  RescueEntityType.FIRE_STATION.getID(),
                                  RescueEntityType.AMBULANCE_TEAM.getID(),
                                  RescueEntityType.AMBULANCE_CENTRE.getID(),
                                  RescueEntityType.POLICE_FORCE.getID(),
                                  RescueEntityType.POLICE_OFFICE.getID()
        };
    }
}
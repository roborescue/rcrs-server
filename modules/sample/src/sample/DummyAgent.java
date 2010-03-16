package sample;

import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

/**
   A no-op agent.
 */
public class DummyAgent extends StandardAgent<StandardEntity> {
    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        sendRest(time);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE,
                          StandardEntityURN.FIRE_STATION,
                          StandardEntityURN.AMBULANCE_TEAM,
                          StandardEntityURN.AMBULANCE_CENTRE,
                          StandardEntityURN.POLICE_FORCE,
                          StandardEntityURN.POLICE_OFFICE);
    }
}

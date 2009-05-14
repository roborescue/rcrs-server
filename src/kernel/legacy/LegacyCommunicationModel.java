package kernel.legacy;

import java.util.Collection;
import java.util.HashSet;

import kernel.CommunicationModel;

import rescuecore2.messages.Message;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;

import rescuecore2.version0.entities.RescueObject;
import rescuecore2.version0.entities.FireBrigade;
import rescuecore2.version0.entities.FireStation;
import rescuecore2.version0.entities.PoliceForce;
import rescuecore2.version0.entities.PoliceOffice;
import rescuecore2.version0.entities.AmbulanceTeam;
import rescuecore2.version0.entities.AmbulanceCentre;
import rescuecore2.version0.messages.AKSay;
import rescuecore2.version0.messages.AKTell;
import rescuecore2.version0.messages.KAHearSay;
import rescuecore2.version0.messages.KAHearTell;

/**
   The legacy communication model: fire brigades talk to fire brigades and the fire station, police to police, ambulance to ambulance and centres talk to centres.
 */
public class LegacyCommunicationModel implements CommunicationModel<RescueObject, IndexedWorldModel> {
    private IndexedWorldModel world;
    private int sayDistance;

    /**
       Construct a LegacyCommunicationModel.
       @param config The kernel configuration.
     */
    public LegacyCommunicationModel(Config config) {
        sayDistance = config.getIntValue("voice");
    }

    @Override
    public void setWorldModel(IndexedWorldModel newWorld) {
        this.world = newWorld;
    }

    @Override
    public Collection<Message> process(RescueObject agent, Collection<Message> agentCommands) {
        //        System.out.println("Looking for messages that " + agent + " can hear: " + agentCommands);
        Collection<Message> result = new HashSet<Message>();
        // Look for SAY messages from entities within range
        // Look for TELL messages from appropriate entities
        for (Message next : agentCommands) {
            if (next instanceof AKSay) {
                AKSay say = (AKSay)next;
                EntityID senderID = say.getAgentID();
                RescueObject sender = world.getEntity(senderID);
                int distance = world.getDistance(agent, sender);
                if (distance <= sayDistance) {
                    System.out.println(agent + " hears say from " + sender);
                    result.add(new KAHearSay(senderID, say.getContent()));
                }
            }
            if (next instanceof AKTell) {
                AKTell tell = (AKTell)next;
                EntityID senderID = tell.getAgentID();
                RescueObject sender = world.getEntity(senderID);
                if (canHear(agent, sender)) {
                    System.out.println(agent + " hears tell from " + sender);
                    result.add(new KAHearTell(senderID, tell.getContent()));
                }
            }
        }
        return result;
    }

    private boolean canHear(RescueObject receiver, RescueObject sender) {
        if (receiver instanceof FireBrigade) {
            return sender instanceof FireBrigade || sender instanceof FireStation;
        }
        if (receiver instanceof FireStation) {
            return sender instanceof FireBrigade
            || sender instanceof FireStation
            || sender instanceof PoliceOffice
            || sender instanceof AmbulanceCentre;
        }
        if (receiver instanceof PoliceForce) {
            return sender instanceof PoliceForce || sender instanceof PoliceOffice;
        }
        if (receiver instanceof PoliceOffice) {
            return sender instanceof PoliceForce
            || sender instanceof FireStation
            || sender instanceof PoliceOffice
            || sender instanceof AmbulanceCentre;
        }
        if (receiver instanceof AmbulanceTeam) {
            return sender instanceof AmbulanceTeam || sender instanceof AmbulanceCentre;
        }
        if (receiver instanceof AmbulanceCentre) {
            return sender instanceof AmbulanceTeam
            || sender instanceof FireStation
            || sender instanceof PoliceOffice
            || sender instanceof AmbulanceCentre;
        }
        return false;
    }
}
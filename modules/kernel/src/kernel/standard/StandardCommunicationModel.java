package kernel.standard;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import kernel.Agent;
import kernel.CommunicationModel;

import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.messages.AKSay;
import rescuecore2.standard.messages.AKTell;
import rescuecore2.standard.messages.KAHearSay;
import rescuecore2.standard.messages.KAHearTell;

/**
   The legacy communication model: fire brigades talk to fire brigades and the fire station, police to police, ambulance to ambulance and centres talk to centres.
 */
public class StandardCommunicationModel implements CommunicationModel {
    private StandardWorldModel world;
    private int sayDistance;

    /**
       Construct a StandardCommunicationModel.
    */
    public StandardCommunicationModel() {
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        world = StandardWorldModel.createStandardWorldModel(model);
        sayDistance = config.getIntValue("standard.say.range");
    }

    @Override
    public String toString() {
        return "Standard communication model";
    }

    @Override
    public Map<Agent, Collection<Message>> process(int time, Collection<Agent> agents, Collection<Command> agentCommands) {
        Map<Agent, Collection<Message>> all = new HashMap<Agent, Collection<Message>>();
        for (Agent agent : agents) {
            //        System.out.println("Looking for messages that " + agent + " can hear: " + agentCommands);
            Collection<Message> result = new HashSet<Message>();
            // Look for SAY messages from entities within range
            // Look for TELL messages from appropriate entities
            for (Command next : agentCommands) {
                if (next instanceof AKSay) {
                    AKSay say = (AKSay)next;
                    EntityID senderID = say.getAgentID();
                    StandardEntity sender = world.getEntity(senderID);
                    int distance = world.getDistance((StandardEntity)agent.getControlledEntity(), sender);
                    if (distance <= sayDistance) {
                        //                    System.out.println(agent + " hears say from " + sender);
                        result.add(new KAHearSay(senderID, say.getContent()));
                    }
                }
                if (next instanceof AKTell) {
                    AKTell tell = (AKTell)next;
                    EntityID senderID = tell.getAgentID();
                    StandardEntity sender = world.getEntity(senderID);
                    if (canHear(agent.getControlledEntity(), sender)) {
                        //                    System.out.println(agent + " hears tell from " + sender);
                        result.add(new KAHearTell(senderID, tell.getContent()));
                    }
                }
            }
            all.put(agent, result);
        }
        return all;
    }

    private boolean canHear(Entity receiver, StandardEntity sender) {
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
package rescuecore2.standard.kernel.comms;

import java.util.Collection;
import java.util.Map;

import kernel.AbstractCommunicationModel;

import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.messages.AKSay;
import rescuecore2.standard.messages.AKTell;

/**
   The legacy communication model: fire brigades talk to fire brigades and the fire station, police to police, ambulance to ambulance and centres talk to centres.
 */
public class StandardCommunicationModel extends AbstractCommunicationModel {
    private static final String SAY_RANGE_KEY = "comms.standard.say.range";
    private static final String PLATOON_MAX_KEY = "comms.standard.platoon.max";
    private static final String MAX_SIZE_KEY = "comms.standard.size.max";

    private StandardWorldModel world;
    private int sayDistance;
    private int maxSize;
    private int platoonMax;
    private int fsMax;
    private int poMax;
    private int acMax;
    private Map<EntityID, Integer> uttered;
    private Map<EntityID, Integer> heard;

    /**
       Construct a StandardCommunicationModel.
    */
    public StandardCommunicationModel() {
        uttered = new LazyMap<EntityID, Integer>() {
            @Override
            public Integer createValue() {
                return 0;
            }
        };
        heard = new LazyMap<EntityID, Integer>() {
            @Override
            public Integer createValue() {
                return 0;
            }
        };
    }

    @Override
    public String toString() {
        return "Standard communication model";
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        super.initialise(config, model);
        world = StandardWorldModel.createStandardWorldModel(model);
        sayDistance = config.getIntValue(SAY_RANGE_KEY);
        maxSize = config.getIntValue(MAX_SIZE_KEY);
        platoonMax = config.getIntValue(PLATOON_MAX_KEY);
        fsMax = world.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE).size() * 2;
        acMax = world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM).size() * 2;
        poMax = world.getEntitiesOfType(StandardEntityURN.POLICE_FORCE).size() * 2;
    }

    @Override
    public void process(int time, Collection<? extends Command> agentCommands) {
        super.process(time, agentCommands);
        uttered.clear();
        heard.clear();
        for (Command next : agentCommands) {
            try {
                if (next instanceof AKSay) {
                    processSay((AKSay)next);
                }
                if (next instanceof AKTell) {
                    processTell((AKTell)next);
                }
            }
            catch (InvalidMessageException e) {
                Logger.warn("Invalid message: " + next, e);
            }
        }
    }

    private void processSay(AKSay say) throws InvalidMessageException {
        EntityID senderID = say.getAgentID();
        StandardEntity sender = world.getEntity(senderID);
        if (!(sender instanceof Human)) {
            throw new InvalidMessageException("Agent " + senderID + " is not a human: " + (sender == null ? "null" : sender.getClass().getName()));
        }
        byte[] data = say.getContent();
        if (data.length > maxSize) {
            throw new InvalidMessageException("Agent " + senderID + " sent an oversize message: " + data.length + " > " + maxSize);
        }
        int count = uttered.get(senderID);
        int max = getMessageMax(sender);
        if (count >= getMessageMax(sender)) {
            throw new InvalidMessageException("Agent " + senderID + " has uttered too many messages: " + count + " >= " + max);
        }
        uttered.put(senderID, count + 1);
        for (StandardEntity receiver : world.getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                               StandardEntityURN.FIRE_BRIGADE,
                                                               StandardEntityURN.FIRE_STATION,
                                                               StandardEntityURN.AMBULANCE_TEAM,
                                                               StandardEntityURN.AMBULANCE_CENTRE,
                                                               StandardEntityURN.POLICE_FORCE,
                                                               StandardEntityURN.POLICE_OFFICE)) {
            int h = heard.get(receiver.getID());
            if (h >= getMessageMax(receiver)) {
                continue;
            }
            int distance = world.getDistance(sender, receiver);
            if (distance <= sayDistance) {
                addHearing(receiver, say);
                heard.put(receiver.getID(), h + 1);
            }
        }
    }

    private void processTell(AKTell tell) throws InvalidMessageException {
        EntityID senderID = tell.getAgentID();
        StandardEntity sender = world.getEntity(senderID);
        if (!(sender instanceof Human)) {
            throw new InvalidMessageException("Agent " + senderID + " is not a human: " + (sender == null ? "null" : sender.getClass().getName()));
        }
        byte[] data = tell.getContent();
        if (data.length > maxSize) {
            throw new InvalidMessageException("Agent " + senderID + " sent an oversize message: " + data.length + " > " + maxSize);
        }
        int count = uttered.get(senderID);
        int max = getMessageMax(sender);
        if (count >= getMessageMax(sender)) {
            throw new InvalidMessageException("Agent " + senderID + " has uttered too many messages: " + count + " >= " + max);
        }
        uttered.put(senderID, count + 1);
        for (StandardEntity receiver : world.getEntitiesOfType(StandardEntityURN.CIVILIAN,
                                                               StandardEntityURN.FIRE_BRIGADE,
                                                               StandardEntityURN.FIRE_STATION,
                                                               StandardEntityURN.AMBULANCE_TEAM,
                                                               StandardEntityURN.AMBULANCE_CENTRE,
                                                               StandardEntityURN.POLICE_FORCE,
                                                               StandardEntityURN.POLICE_OFFICE)) {
            int h = heard.get(receiver.getID());
            if (h >= getMessageMax(receiver)) {
                continue;
            }
            if (canHear(receiver, sender)) {
                addHearing(receiver, tell);
                heard.put(receiver.getID(), h + 1);
            }
        }
    }

    private int getMessageMax(StandardEntity e) {
        if (e instanceof FireStation) {
            return fsMax;
        }
        if (e instanceof PoliceOffice) {
            return poMax;
        }
        if (e instanceof AmbulanceCentre) {
            return acMax;
        }
        if (e instanceof Human) {
            return platoonMax;
        }
        return 0;
    }

    private boolean canHear(StandardEntity receiver, StandardEntity sender) {
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

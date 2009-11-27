package rescuecore2.standard.components;

import rescuecore2.components.AbstractAgent;
import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.standard.messages.AKSubscribe;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.messages.AKSay;
import rescuecore2.standard.messages.AKTell;

import java.util.List;

/**
   Abstract base class for standard agents.
   @param <E> The subclass of StandardEntity that this agent wants to control.
*/
public abstract class StandardAgent<E extends StandardEntity> extends AbstractAgent<StandardWorldModel, E> {
    @Override
    protected StandardWorldModel createWorldModel() {
        return new StandardWorldModel();
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.index(config.getIntValue(StandardWorldModel.MESH_SIZE_KEY, StandardWorldModel.DEFAULT_MESH_SIZE));
    }

    /**
       Send a rest command to the kernel.
       @param time The current time.
    */
    protected void sendRest(int time) {
        send(new AKRest(getID(), time));
    }

    /**
       Send a move command to the kernel.
       @param time The current time.
       @param path The path to send.
    */
    protected void sendMove(int time, List<EntityID> path) {
        send(new AKMove(getID(), time, path));
    }

    /**
       Send an extinguish command to the kernel.
       @param time The current time.
       @param target The target building.
       @param water The amount of water to use.
    */
    protected void sendExtinguish(int time, EntityID target, int water) {
        send(new AKExtinguish(getID(), time, target, water));
    }

    /**
       Send a clear command to the kernel.
       @param time The current time.
       @param target The target road.
    */
    protected void sendClear(int time, EntityID target) {
        send(new AKClear(getID(), time, target));
    }

    /**
       Send a rescue command to the kernel.
       @param time The current time.
       @param target The target human.
    */
    protected void sendRescue(int time, EntityID target) {
        send(new AKRescue(getID(), time, target));
    }

    /**
       Send a load command to the kernel.
       @param time The current time.
       @param target The target human.
    */
    protected void sendLoad(int time, EntityID target) {
        send(new AKLoad(getID(), time, target));
    }

    /**
       Send an unload command to the kernel.
       @param time The current time.
    */
    protected void sendUnload(int time) {
        send(new AKUnload(getID(), time));
    }

    /**
       Send a speak command to the kernel.
       @param time The current time.
       @param channel The channel to speak on.
       @param data The data to send.
    */
    protected void sendSpeak(int time, int channel, byte[] data) {
        send(new AKSpeak(getID(), time, channel, data));
    }

    /**
       Send a subscribe command to the kernel.
       @param time The current time.
       @param channels The channels to subscribe to.
    */
    protected void sendSubscribe(int time, int... channels) {
        send(new AKSubscribe(getID(), time, channels));
    }

    /**
       Send a say command to the kernel.
       @param time The current time.
       @param data The data to send.
    */
    protected void sendSay(int time, byte[] data) {
        send(new AKSay(getID(), time, data));
    }

    /**
       Send a tell command to the kernel.
       @param time The current time.
       @param data The data to send.
    */
    protected void sendTell(int time, byte[] data) {
        send(new AKTell(getID(), time, data));
    }
}
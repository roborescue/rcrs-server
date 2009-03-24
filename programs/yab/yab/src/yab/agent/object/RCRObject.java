// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.*;
import yab.io.*;
import yab.io.object.*;

public abstract class RCRObject {
    protected final BaseRCRObject object;
    public final DisasterSpace world;
    public final int id;

    public RCRObject(int id, BaseRCRObject object,
                     DisasterSpace world) {
        this.id = id;
        this.object = object;
        this.world = world;
    }
    public void setProperty(PropertyElement[] properties) {
        for (int i = 0;  i < properties.length;  i ++)
            setProperty(properties[i].type, properties[i].value);
    }
    public void setProperty(int type, int[] value) {
        object.setProperty(type, value);
    }
    public int hashCode() { return object.hashCode(); }

    private int m_time;
    public int time() { return m_time; }
    public void setTime(int time) { m_time = time; }

    public String toString() { return Util.classBaseName(this)+"(ID:"+id+")"; }

    public static RCRObject produce(int type, int id, DisasterSpace ds) {
        switch (type) {
        case ProtocolConstants.TYPE_WORLD:       return new World(id, ds);
        case ProtocolConstants.TYPE_RIVER:       return new River(id, ds);
        case ProtocolConstants.TYPE_RIVER_NODE:  return new RiverNode(id, ds);
        case ProtocolConstants.TYPE_ROAD:        return new Road(id, ds);
        case ProtocolConstants.TYPE_NODE:        return new Node(id, ds);
        case ProtocolConstants.TYPE_BUILDING:    return new Building(id, ds);
        case ProtocolConstants.TYPE_AMBULANCE_CENTER:
            return new AmbulanceCenter(id, ds);
        case ProtocolConstants.TYPE_FIRE_STATION:return new FireStation(id,ds);
        case ProtocolConstants.TYPE_POLICE_OFFICE:
            return new PoliceOffice(id, ds);
        case ProtocolConstants.TYPE_REFUGE:      return new Refuge(id, ds);
        case ProtocolConstants.TYPE_CIVILIAN:    return new Civilian(id, ds);
        case ProtocolConstants.TYPE_AMBULANCE_TEAM:
            return new AmbulanceTeam(id, ds);
        case ProtocolConstants.TYPE_FIRE_BRIGADE:return new FireBrigade(id,ds);
        case ProtocolConstants.TYPE_POLICE_FORCE:return new PoliceForce(id,ds);
        case ProtocolConstants.TYPE_CAR:         return new Car(id, ds);
        default: throw new Error("illeagle object type" + type);
        }
    }
}

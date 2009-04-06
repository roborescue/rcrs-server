// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

import yab.io.*;

public abstract class BaseRCRObject implements ProtocolConstants {
    public final int id;

    public BaseRCRObject(int id) { this.id = id; }

    public void setProperty(PropertyElement[] properties) {
        for (int i = 0;  i < properties.length;  i ++)
            setProperty(properties[i].type, properties[i].value);
    }
    public void setProperty(int type, int[] value) {
        // do nothing
    }

    public static BaseRCRObject produce(int type, int id) {
        switch (type) {
        default: throw new Error("illeagle object type" + type);
        case TYPE_WORLD:            return new BaseWorld(id);
        case TYPE_RIVER:            return new BaseRiver(id);
        case TYPE_RIVER_NODE:       return new BaseRiverNode(id);
        case TYPE_ROAD:             return new BaseRoad(id);
        case TYPE_NODE:             return new BaseNode(id);
        case TYPE_BUILDING:         return new BaseBuilding(id);
        case TYPE_AMBULANCE_CENTER: return new BaseAmbulanceCenter(id);
        case TYPE_FIRE_STATION:     return new BaseFireStation(id);
        case TYPE_POLICE_OFFICE:    return new BasePoliceOffice(id);
        case TYPE_REFUGE:           return new BaseRefuge(id);
        case TYPE_CIVILIAN:         return new BaseCivilian(id);
        case TYPE_AMBULANCE_TEAM:   return new BaseAmbulanceTeam(id);
        case TYPE_FIRE_BRIGADE:     return new BaseFireBrigade(id);
        case TYPE_POLICE_FORCE:     return new BasePoliceForce(id);
        case TYPE_CAR:              return new BaseCar(id);
        }
    }

    public int hashCode() { return id; }
}

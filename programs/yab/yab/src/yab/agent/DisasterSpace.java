// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.util.*;
import yab.agent.object.*;
import yab.io.ObjectElement;

public class DisasterSpace {
    private final HashMap m_idObjMap = new HashMap();
    public final RealObject self;

    public final ArrayList roads     = new ArrayList();
    public final ArrayList nodes     = new ArrayList();
    public final ArrayList buildings = new ArrayList();
    public final ArrayList refuges   = new ArrayList();
    public final ArrayList humanoids = new ArrayList();
    public final ArrayList civilians = new ArrayList();
    public final ArrayList ambulanceTeams = new ArrayList();
    public final ArrayList fireBrigades   = new ArrayList();
    public final ArrayList policeForces   = new ArrayList();

    public DisasterSpace(int selfId, ObjectElement[] objs) {
        update(objs, Constants.TIME_INITIALIZING_AGENT);
        self = get(selfId);
    }

    private int m_time;
    public int time() { return m_time; }

    public void add(RealObject obj) {
        m_idObjMap.put(new Integer(obj.id), obj);
        if      (obj instanceof Road)     roads    .add(obj);
        else if (obj instanceof Node)     nodes    .add(obj);
        else if (obj instanceof Building) {
            buildings.add(obj);
            if (obj instanceof Refuge) refuges.add(obj);
        }
        else if (obj instanceof Humanoid) {
            humanoids.add(obj);
            if      (obj instanceof Civilian)      civilians.add(obj);
            else if (obj instanceof AmbulanceTeam) ambulanceTeams.add(obj);
            else if (obj instanceof FireBrigade)   fireBrigades.add(obj);
            else if (obj instanceof PoliceForce)   policeForces.add(obj);
        }
    }

    public RealObject get(int id) {
        return (RealObject) m_idObjMap.get(new Integer(id));
    }

    public void update(ObjectElement[] objs, int time) {
        m_time = time;
        for (int i = 0;  i < objs.length;  i ++) {
            RCRObject obj = get(objs[i].id);
            if (obj == null) {
                obj = RCRObject.produce(objs[i].type, objs[i].id, this);
                if (obj instanceof RealObject)
                    add((RealObject) obj);
            }
            obj.setTime(time);
            obj.setProperty(objs[i].properties);
        }
    }
}

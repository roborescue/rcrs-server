// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import java.util.*;
import yab.agent.DisasterSpace;
import yab.io.object.*;

public class Building extends PointObject {
    public Building(int id, BaseBuilding object, DisasterSpace world)
    { super(id, object, world); }
    public Building(int id, DisasterSpace world)
    { super(id, new BaseBuilding(id), world); }
    private BaseBuilding obj() { return (BaseBuilding) object; }

    public int floors()             { return obj().floors(); }
    public int fieryness()          { return obj().fieryness(); }
	public int heat()		{ return obj().heat(); }
    public int buildingCode()       { return obj().buildingCode(); }
    public int buildingAreaGround() { return obj().buildingAreaGround(); }
    public int buildingAreaTotal()  { return obj().buildingAreaTotal(); }
    private MotionlessObject m_entrance = null;
    public MotionlessObject entrance() {
        if (m_entrance == null)
            m_entrance = (MotionlessObject) world.get(obj().entrance());
        return m_entrance;
    }

    private Set m_entrances = null;
    public Collection neighborhood() {
        if (m_entrances == null)
            m_entrances = Collections.singleton(entrance());
        return m_entrances;
    }

    public boolean isUnburned() { return fieryness() == 0; }
    public boolean isBurning() { return 1 <= fieryness() && fieryness() <= 3; }
    public boolean isPutOut() { return 5 <= fieryness() && fieryness() <= 7; }

    /** CAUTION: This method is implemented assuming that the agent
     *  can perceive all fires in the disaster space regardless of how
     *  far them are.
     */
    public int burningTime() { return world.time() - m_ignitedTime ; }
    private int m_ignitedTime = Integer.MAX_VALUE;

    public void setProperty(int type, int[] value) {
        boolean wasUnburned = fieryness() == 0;
        super.setProperty(type, value);
        if (wasUnburned  &&  fieryness() != 0)
            m_ignitedTime = world.time();
    }
}

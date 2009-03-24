// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import java.util.*;
import yab.agent.*;
import yab.agent.object.*;

public class AmbulanceTeamAgent extends AbstractAmbulanceTeamAgent {
    private final Set m_unvisitedBuildings;

    public AmbulanceTeamAgent(InetAddress address, int port) {
        super(address, port);
        m_unvisitedBuildings = new HashSet(world.buildings);
    }

    protected MotionlessObject mlpos() { return self().motionlessPosition(); }

    protected static final Property
        DAMAGE_PRP     = Property.get("Humanoid", "damage"),
        BURIEDNESS_PRP = Property.get("Humanoid", "buriedness"),
        POSITION_PRP   = Property.get("MovingObject", "position"),
        FIERYNESS_PRP  = Property.get("Building", "fieryness");

    protected static final Condition
        IS_NOT_BURNING_CND = FIERYNESS_PRP.lt(1).or(FIERYNESS_PRP.gt(3));

    protected void prepareForAct() {
        super.prepareForAct();
        if (mlpos() instanceof Building)
            m_unvisitedBuildings.remove(mlpos());
    }

    protected void act() throws ActionCommandException {
        if (isLoadingInjured())
            carry();

        List hmns = POSITION_PRP.eq(mlpos()).extract(world.humanoids);
        hmns.remove(self());
        if (!hmns.isEmpty()) {
            rescue(hmns);
            load(hmns);
        }

        searchTheInjured();
    }

    protected void carry() throws ActionCommandException {
        if (mlpos() instanceof Refuge)
            unload();
        else
            move(world.refuges);
    }

    protected void rescue(List hmns) throws ActionCommandException{
        List brd = BURIEDNESS_PRP.gt(0).extract(hmns);
        if (!brd.isEmpty())
            rescue((Humanoid) DAMAGE_PRP.max(brd));
    }

    protected void load(List hmns) throws ActionCommandException{
        List inj = DAMAGE_PRP.gt(0).extract(hmns);
        if (!inj.isEmpty())
            load((Humanoid) DAMAGE_PRP.max(inj));
    }

    protected void searchTheInjured() throws ActionCommandException{
        List bldgs = IS_NOT_BURNING_CND.extract(m_unvisitedBuildings);
        if (!bldgs.isEmpty())
            move(bldgs);
    }

    protected void hearTell(RealObject sender, int channelId, String message) 
	{
	}
}

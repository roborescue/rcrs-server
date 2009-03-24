// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import java.util.*;
import yab.agent.*;
import yab.agent.object.*;

public class FireBrigadeAgent extends AbstractFireBrigadeAgent {
    public FireBrigadeAgent(InetAddress address, int port) {
        super(address, port);
    }

    protected static final Property
        FIERYNESS_PRP    = Property.get("Building", "fieryness"),
        BURNING_TIME_PRP = Property.get("Building", "burningTime"),
        ENTRANCE_PRP     = Property.get("Building", "entrance");
    protected static final int BURNING_TIME_OF_AN_EARLY_FIRE = 3;
    protected static final Condition
        IS_BURNING_CND = FIERYNESS_PRP.gte(1).and(FIERYNESS_PRP.lte(3)),
        IS_EARLY_CND   = BURNING_TIME_PRP.lte(BURNING_TIME_OF_AN_EARLY_FIRE);
    protected final Condition isNearCnd
        = distancePrp.lte(EXTINGUISHABLE_DISTANCE);

    protected void act() throws ActionCommandException {
        reportBlockade();
        //supplyWater();
        Collection fires = IS_BURNING_CND.extract(world.buildings);
        Collection earlyFires = IS_EARLY_CND.extract(fires);
        extinguishOrMove(earlyFires);
        extinguishOrMove(fires);
        moveToRefuges();
    }

    protected void reportBlockade() {
        MotionlessObject pos = self().motionlessPosition();
        if (pos instanceof Road  &&  ((Road) pos).passableLines() == 0)
            tell("clear " + pos.id);
    }

    //protected void supplyWater() throws ActionCommandException {
    //    if (self().waterQuantity() == 0)
    //        moveToRefuges();
    //}

    protected void extinguishOrMove(Collection fires)
        throws ActionCommandException {
        extinguishNearFire(fires);
        moveToEntrances(fires);
    }

    protected void extinguishNearFire(Collection fires)
        throws ActionCommandException {
        Collection nearFires = isNearCnd.extract(fires);
        if (!nearFires.isEmpty())
            extinguish((Building) BURNING_TIME_PRP.min(nearFires));
    }

    protected void moveToEntrances(Collection fires)
        throws ActionCommandException {
        if (!fires.isEmpty())
            move(ENTRANCE_PRP.collect(fires));
    }

    protected void moveToRefuges() throws ActionCommandException {
        if (self().motionlessPosition() instanceof Refuge)
            rest();
        move(world.refuges);
    }

    protected void hearTell(RealObject sender, int channelId, String message) 
	{
	}
}

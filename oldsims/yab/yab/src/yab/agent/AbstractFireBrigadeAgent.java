// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import java.util.*;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.NozzleElement;
import yab.io.ProtocolConstants;

public abstract class AbstractFireBrigadeAgent extends PlatoonAgent {
    protected AbstractFireBrigadeAgent(InetAddress address, int port) {
        super(ProtocolConstants.AGENT_TYPE_FIRE_BRIGADE, address, port);
    }

    protected FireBrigade self() { return (FireBrigade) world.self; }

    /** @param targets each element of the given <code>targets</code>
     *  must be a burning building.
     */
    protected void extinguish(ArrayList targets)
        throws ActionCommandException {
        extinguish((Building[]) targets.toArray(new Building[0]));
    }
    protected void extinguish(Building target) throws ActionCommandException {
        extinguish(new Building[] { target });
    }
    protected void extinguish(Building[] targets)
        throws ActionCommandException {
        MotionlessObject pos = self().motionlessPosition();
        NozzleElement[] nozzles = new NozzleElement[targets.length];
        for (int i = 0;  i < nozzles.length;  i ++) {
            Building fire = targets[i];
            if (self().distance(fire) > EXTINGUISHABLE_DISTANCE)
                throw new Error(self() + " cannot extinguish a far building.");
            nozzles[i] = new NozzleElement(fire.id,
                                           self().direction(fire),
                                           pos.x(),
                                           pos.y(),
                                           EXTINGUISHABLE_QUANTITY
                                           / targets.length);
        }
        extinguish(nozzles);
    }
    protected void extinguish(NozzleElement[] targets)
        throws ActionCommandException {
        socket().akExtinguish(self().id, targets);
        throw new ActionCommandException();
    }
}
